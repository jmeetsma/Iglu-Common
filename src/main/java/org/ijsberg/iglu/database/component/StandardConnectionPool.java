/*
 * Copyright 2011-2013 Jeroen Meetsma - IJsberg
 *
 * This file is part of Iglu.
 *
 * Iglu is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Iglu is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Iglu.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.ijsberg.iglu.database.component;


import org.ijsberg.iglu.configuration.ConfigurationException;
import org.ijsberg.iglu.configuration.Startable;
import org.ijsberg.iglu.exception.ResourceException;
import org.ijsberg.iglu.logging.Level;
import org.ijsberg.iglu.logging.LogEntry;
import org.ijsberg.iglu.scheduling.Pageable;
import org.ijsberg.iglu.util.execution.Executable;
import org.ijsberg.iglu.util.execution.TimeOutException;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.logging.Logger;

/**
 * A simple connection pool implementation that manages a number of reusable connections to a DBMS.
 * The following component properties may be provided:
 * <p/>
 * <ul>
 * <li>dbdriver: the database driver class</li>
 * <li>dburl: the database URL</li>
 * <li>dbusername: a databse user account</li>
 * <li>dbuserpassword: a password for the user account</li>
 * </ul>
 * <p/>
 * <ul>
 * <li>nrof_connections: the exact number of connections desired</li>
 * <ul>
 * or
 * <ul>
 * <li>initial_nrof_connections: the number of connections to start with</li>
 * <li>max_nrof_connections: a maximum number of connections to grow to</li>
 * </ul>
 * <p/>
 * <ul>
 * <li>connection_request_timeout: the maximum time in seconds to wait for distribution of a request</li>
 * <li>astray_connection_timeout: timeout in seconds for a connection that is somehow not released</li>
 * <li>astray_connection_timeout_check_interval: interval in seconds to check for connections gone astray</li>
 * </ul>
 */

public class StandardConnectionPool implements Startable, Pageable, DataSource {

	public static final List<String> isolationLevelStrings = Arrays.asList(new String[]{
			"TRANSACTION_NONE",
			"TRANSACTION_READ_UNCOMMITTED",
			"TRANSACTION_READ_COMMITTED", null,
			"TRANSACTION_REPEATABLE_READ", null, null, null,
			"TRANSACTION_SERIALIZABLE"
	});

	//Connection storage
	private final ArrayList allConnections = new ArrayList();
	private final ArrayList availableConnections = new ArrayList();
	private final ArrayList usedConnections = new ArrayList();

	private String dbUrl;
	private String dbUsername;
	private String dbUserpassword;

	private boolean createReadOnlyConnections;
	private int defaultIsolationLevel = Connection.TRANSACTION_READ_UNCOMMITTED;


	//Pool parameters
	private int initialNrofConnections = 10;
	private int maxNrofConnections = 10;
	private int connectionRequestTimeout = 15;
	private int connectionTimeout = 300;
	private int connectionTimeoutCheck = 3;//min
	private int maxNrOfConcurrentConnectionsCounted;


	//Statistics
	private long nrofRequests;
	private long nrofConnectionsDistributed;
	private long nrofReleased;
	private long nrofReset;
	private long nrofRequestsTimedOut;
	private long nrofQueuedRequests;
	private long nrofStaleCleanedUp;
	private long cumulatedResponseTime;
	private long nrofErrors;
	private String driverInfo;

	//connection tester
	private StandardJdbcProcessor connectionTester;
	private String connectionTestStatement = "select 1 from dual";

	private boolean isStarted = false;

	/**
	 * Default log writer is System.out.
	 *
	 * @see this#setLogWriter(java.io.PrintWriter)
	 */
	private PrintWriter logWriter = new PrintWriter(System.out);

	public StandardConnectionPool() {
	}

	public int getPageIntervalInMinutes() {
		return connectionTimeoutCheck;
	}

	public int getPageOffsetInMinutes() {
		return 0;
	}


	/**
	 *
	 */
	public class ConnectionWrapper implements InvocationHandler {
		private String lastStatement;

		private Connection conn;
		private StandardConnectionPool pool;

		private Boolean originalAutoCommit;
		private Boolean currentAutoCommit;
		private String originalCatalog;
		private String currentCatalog;
		private Boolean originalReadOnly;
		private Boolean currentReadOnly;
		private Integer originalTransactionIsolation;
		private Integer currentTransactionIsolation;
		private Map originalTypeMap;
		private Map currentTypeMap;
		private Integer originalHoldability;
		private Integer currentHoldability;

		private long timeout = 60;
		private long creationDate = System.currentTimeMillis();

		/**
		 * @param conn
		 * @param timeout
		 */
		public ConnectionWrapper(Connection conn, long timeout) {
			this.conn = conn;
			this.timeout = timeout;
		}

		/**
		 * @param conn
		 * @param timeout
		 * @param pool
		 */
		private ConnectionWrapper(Connection conn, long timeout, StandardConnectionPool pool) {
			this.conn = conn;
			this.pool = pool;
			this.timeout = timeout;

			try {
				originalAutoCommit = new Boolean(conn.getAutoCommit());
				originalCatalog = conn.getCatalog();
				originalReadOnly = new Boolean(conn.isReadOnly());
				originalTransactionIsolation = new Integer(conn.getTransactionIsolation());
				originalTypeMap = conn.getTypeMap();
				originalHoldability = new Integer(conn.getHoldability());
				setCurrentProperties();
			} catch (SQLException sqle) {
				throw new ResourceException("cannot create ConnectionWrapper with message: " + sqle.getMessage(), sqle);
			}
		}

		private void setCurrentProperties() {
			currentAutoCommit = originalAutoCommit;
			currentCatalog = originalCatalog;
			currentReadOnly = originalReadOnly;
			currentTransactionIsolation = originalTransactionIsolation;
			currentTypeMap = originalTypeMap;
			currentHoldability = originalHoldability;
		}

		/**
		 * @param proxy
		 * @param method
		 * @param args
		 * @return
		 * @throws Throwable
		 */
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			String methodName = method.getName();
			if ("close".equals(methodName)) {
				close();
				return null;
			} else if ("prepareCall".equals(methodName) || "prepareStatement".equals(methodName)) {
				lastStatement = (String) args[0];
			} else if (methodName.startsWith("set")) {
				storeSetValue(methodName, args);
			}
			//TODO this throws a number of exceptions, such as InvocationTargetException,
			//TODO that must be dealt with
			return method.invoke(conn, args);
		}

		/**
		 * @param methodName
		 * @param args
		 * @return
		 * @throws IllegalAccessException
		 * @throws InvocationTargetException
		 */
		private void storeSetValue(String methodName, Object[] args)
				throws IllegalAccessException, InvocationTargetException {
			if ("setAutoCommit".equals(methodName) && !args[0].equals(currentAutoCommit)) {
				currentAutoCommit = (Boolean) args[0];
			} else if ("setReadOnly".equals(methodName) && !args[0].equals(currentReadOnly)) {
				currentReadOnly = (Boolean) args[0];
			} else if ("setCatalog".equals(methodName) && !args[0].equals(currentCatalog)) {
				currentCatalog = (String) args[0];
			} else if ("setTransactionIsolation".equals(methodName) && !args[0].equals(currentTransactionIsolation)) {
				currentTransactionIsolation = (Integer) args[0];
			} else if ("setTypeMap".equals(methodName) && !args[0].equals(currentTypeMap)) {
				currentTypeMap = (Map) args[0];
			} else if ("setHoldability".equals(methodName) && !args[0].equals(currentHoldability)) {
				currentHoldability = (Integer) args[0];
			}
		}


		public boolean isTimedOut() {
			return System.currentTimeMillis() - creationDate > (timeout * 1000);
		}


		public Connection getConnection() {
			return conn;
		}


		public String getLastStatement() {
			return lastStatement;
		}


		//close should get invoked in all cases (finally blocks)
		//but what if it doesn't?
		public void close() throws SQLException {
			lastStatement = null;
			try {
				if (pool == null) {
					conn.close();
				} else {
					//reset connection
					conn.clearWarnings();
					//what is this? maybe we should log warnings...
					//TODO this may throw
					resetConnectionSettings();
					//return connection to pool
					pool.releaseConnection(this);
				}
			} catch (SQLException sqle) {
				System.out.println(new LogEntry(Level.CRITICAL, "Error while closing connection, will attempt to reset...", sqle));
				//only used connections get reset
				pool.replaceConnection(this);
			}
		}

		private void resetConnectionSettings()
				throws SQLException {
			try {
				if (!currentAutoCommit.equals(originalAutoCommit)) {
					conn.setAutoCommit(originalAutoCommit.booleanValue());
					currentAutoCommit = originalAutoCommit;
				}
				if (!currentCatalog.equals(originalCatalog)) {
					conn.setCatalog(originalCatalog);
					currentCatalog = originalCatalog;
				}
				if (!currentReadOnly.equals(originalReadOnly)) {
					conn.setReadOnly(originalReadOnly.booleanValue());
					currentReadOnly = originalReadOnly;
				}
				if (!currentTransactionIsolation.equals(originalTransactionIsolation)) {
					conn.setTransactionIsolation(originalTransactionIsolation.intValue());
					currentTransactionIsolation = originalTransactionIsolation;
				}
				if (!currentTypeMap.equals(originalTypeMap)) {
					conn.setTypeMap(originalTypeMap);
					currentTypeMap = originalTypeMap;
				}
				if (!currentHoldability.equals(originalHoldability)) {
					conn.setHoldability(originalHoldability.intValue());
					currentHoldability = originalHoldability;
				}
			} catch (RuntimeException re) {
				//connections are a 3rd party implementations
				//
				System.out.println(new LogEntry(Level.CRITICAL, "reset of connection settings failed " + re, re));
				throw new SQLException("reset of connection settings failed");
			}
		}


		public boolean isClosed() throws SQLException {
			return conn.isClosed();
		}

		public String toString() {
			return "SQL connection: creation date " + new Date(creationDate) + ", last statement:" + lastStatement;
		}

	}


	public Object obtainConnectionRepeated() throws InterruptedException {
		ConnectionWrapper conn;
		long tries = 1;

		while (tries < 100) {
			conn = obtainConnection();
			if (conn != null) {
				System.out.println(new LogEntry("Connection obtained in " + tries + " attempts"));
				return conn;
			} else {
				tries++;
				Thread.sleep(10);
			}
		}
		return null;
	}


	/**
	 * @param dbUrl
	 * @param username
	 * @param password
	 * @return a database connection from the configured driver
	 * @throws SQLException
	 */
	private ConnectionWrapper createConnectionWrapper(String dbUrl, String username, String password) throws SQLException {
		return new ConnectionWrapper(
				createConnection(dbUrl, username, password), connectionTimeout, this);
	}


	/**
	 * @param dbUrl
	 * @param username
	 * @param password
	 * @return a database connection from the configured driver
	 * @throws SQLException
	 */
	private Connection createConnection(String dbUrl, String username, String password) throws SQLException {
		Connection conn = DriverManager.getConnection(dbUrl, username, password);
		if (conn == null) {
			nrofErrors++;
			throw new SQLException("failed to create a connection to database URL " + dbUrl + " using account " + dbUsername);
		}
		if (createReadOnlyConnections) {
			conn.setReadOnly(true);
		}
		return conn;
	}

	/**
	 * @return A connection to a database
	 * @throws SQLException in case a connection can not be obtained
	 */
	public Connection getConnection() throws SQLException {

		if (!isStarted()) {
			throw new SQLException("data source is not active");
		}

		nrofRequests++;
		long start = System.currentTimeMillis();

		ConnectionWrapper conn = obtainConnection();

		if (conn == null) {
			System.out.println(new LogEntry("Connection not directly obtained"));
			nrofQueuedRequests++;

			//create an executable which keeps trying to obtain a connection
			Executable exec = new Executable() {
				protected Object execute() throws Throwable {
					return obtainConnectionRepeated();
				}
			};
			try {
				//execute the connection obtainer within a maximum period of time
				conn = (ConnectionWrapper) exec.executeTimed(connectionRequestTimeout * 1000);
				nrofConnectionsDistributed++;
			} catch (TimeOutException e) {
				nrofRequestsTimedOut++;
				throw new SQLException("obtaining connection timed out");
			} catch (Throwable t) {
				this.nrofErrors++;
				System.out.println(new LogEntry(Level.CRITICAL, "failed to obtain connection", t));
				throw new SQLException("obtaining connection failed with message: " + t.getMessage());
			}
		}

		cumulatedResponseTime += System.currentTimeMillis() - start;
		nrofConnectionsDistributed++;

		return (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(),
				new Class[]{Connection.class},
				conn);

	}


	/**
	 * To be invoked by ConnectionWrapper and StandardConnectionPool only only
	 *
	 * @return A database connection (wrapped in a class that implements Connection as well)
	 */
	ConnectionWrapper obtainConnection() {
		synchronized (allConnections) {
			//fails if connection pool not started
			if (!availableConnections.isEmpty()) {
				//retrieve and remove first connection from list
				//  since released connections are added to the back, connections will rotate
				ConnectionWrapper connWrap = (ConnectionWrapper) availableConnections.remove(0);
				usedConnections.add(connWrap);
				if (usedConnections.size() > maxNrOfConcurrentConnectionsCounted) {
					maxNrOfConcurrentConnectionsCounted = usedConnections.size();
				}
				return connWrap;
			} else {
				//create one if max not reached
				//this also restores lost connections
				if ((allConnections.size() < initialNrofConnections) || (allConnections.size() < maxNrofConnections)) {
					try {
						ConnectionWrapper connWrap = createConnectionWrapper(dbUrl, dbUsername, dbUserpassword);

						allConnections.add(connWrap);
						usedConnections.add(connWrap);

						System.out.println(new LogEntry("Creating new Connection, now " + allConnections.size() + " in use"));

						return connWrap;
					} catch (SQLException sqle) {
						//happens if db unreachable
						//connection will be lost for the time being
						nrofErrors++;
						System.out.println(new LogEntry("Cannot create new connection to " + dbUrl + " unreachable or useless connection settings", sqle));
						throw new ResourceException("Cannot create new connection to " + dbUrl + " unreachable or useless connection settings", sqle);
					}
				} else {
					System.out.println(new LogEntry("maximum nr of connections (" + maxNrofConnections + ") reached while Application demands another"));
				}
				return null;
			}
		}
	}

	/**
	 * Closes the connection and replaces it in the pool
	 * To be invoked by ConnectionWrapper and StandardConnectionPool only only
	 *
	 * @param connWrap
	 */
	void replaceConnection(ConnectionWrapper connWrap) {
		synchronized (allConnections) {
			try {
				if (usedConnections.remove(connWrap)) {
					nrofReset++;

					//the object was indeed locked
					allConnections.remove(connWrap);
					//IMPORTANT does this result in an SQL error which breaks off the connection?
					connWrap.getConnection().close();
					System.out.println(new LogEntry(Level.CRITICAL, "Connection reset" + (connWrap.getLastStatement() != null ? " while executing '" + connWrap.getLastStatement() + '\'' : "")));
					//lets create a fresh successor
					ConnectionWrapper newConnWrap = createConnectionWrapper(dbUrl, dbUsername, dbUserpassword);
					availableConnections.add(newConnWrap);
					allConnections.add(newConnWrap);
				} else {
					nrofErrors++;
					System.out.println(new LogEntry(Level.CRITICAL, "error in connection pool: attempt to close a Connection that does not exist in pool"));
				}
			} catch (SQLException sqle) {
				//happens if db unreachable
				//connection will be lost for the time being
				nrofErrors++;
				System.out.println(new LogEntry(Level.CRITICAL, "can not create new connection to " + dbUrl + " unreachable or useless connection settings", sqle));
			}
			try {
				if (!connWrap.isClosed()) {
					connWrap.getConnection().close();
				}
			} catch (SQLException sqle) {
				nrofErrors++;
				System.out.println(new LogEntry(Level.CRITICAL, "", sqle));
			}
		}
	}


	void releaseConnection(ConnectionWrapper connWrap) {
		synchronized (allConnections) {
			try {
				if (!connWrap.isClosed()) {
					if (usedConnections.remove(connWrap)) {
						//the object was indeed locked
						//so make it available again
						nrofReleased++;
						//add released connection to the end of the list
						availableConnections.add(connWrap);
					} else {
						nrofErrors++;
						System.out.println(new LogEntry(Level.VERBOSE, "Error in connection pool: attempt made to release database connection which is not in use", "connection has probably been closed (and released) before"));
					}
				} else {
					//Connection might be useless
					//Connection will be removed by cleanup
				}
			} catch (SQLException sqle) {
				nrofErrors++;
				System.out.println(new LogEntry(sqle));
			}
		}
	}


	/**
	 * @return A text report to inform application administrators
	 */
	public String getReport() {
		StringBuffer sb = new StringBuffer("");
		sb.append("Total connections: " + allConnections.size() + "\n");
		sb.append("Nr available: " + availableConnections.size() + "\n");
		sb.append("Nr used: " + usedConnections.size() + "\n");
		sb.append("\n");
		sb.append("Nr of requests: " + nrofRequests + "\n");
		sb.append("Nr of requests queued: " + nrofQueuedRequests + "\n");
		sb.append("Nr of requests timed out: " + nrofRequestsTimedOut + "\n");
		sb.append("Nr of connections distributed: " + nrofConnectionsDistributed + "\n");
		sb.append("Nr of connections released: " + nrofReleased + "\n");
		sb.append("Nr of connections reset: " + nrofReset + "\n");
		sb.append("Nr of astray connections cleaned up: " + nrofStaleCleanedUp + "\n");
		sb.append("Nr of errors: " + nrofErrors + "\n");
		if (nrofConnectionsDistributed > 0) {
			sb.append("Average response time: " + (float) cumulatedResponseTime / (float) nrofConnectionsDistributed + " ms\n");
		}
		if (driverInfo != null) {
			sb.append(driverInfo);
		}
		return sb.toString();
	}

	/**
	 * @throws ConfigurationException
	 */
	public void start() {
		allConnections.clear();
		availableConnections.clear();
		usedConnections.clear();
		try {
			for (int i = 0; i < initialNrofConnections; i++) {
				ConnectionWrapper connWrap = createConnectionWrapper(dbUrl, dbUsername, dbUserpassword);
				availableConnections.add(connWrap);
				allConnections.add(connWrap);
			}
		} catch (SQLException sqle) {
			System.out.println(new LogEntry(Level.CRITICAL, "unable to start connection pool, SQLException: " + sqle.getMessage(), sqle));
			throw new ConfigurationException("connection pool is not able to initialize connections", sqle);
		}

		connectionTester = new StandardJdbcProcessor(this);
		isStarted = true;
	}


	//TODO make this safe for connections that are in use to make a reset safe

	/**
	 * Stops the component and closes all connections
	 */
	public void stop() {
		isStarted = false;
		Iterator i = allConnections.iterator();
		while (i.hasNext()) {
			ConnectionWrapper connWrap = (ConnectionWrapper) i.next();
			try {
				connWrap.getConnection().close();
			} catch (SQLException e) {
				nrofErrors++;
				System.out.println(new LogEntry(e));
			}
		}

		allConnections.clear();
		availableConnections.clear();
		usedConnections.clear();
	}


	/**
	 * Initializes the connection pool
	 *
	 * @throws ConfigurationException
	 */
	public void setProperties(Properties properties) throws ConfigurationException {
		String dbDriver = properties.getProperty("dbdriver").toString();
		if (dbDriver == null) {
			throw new ConfigurationException("please specify dbDriver for connectionpool");
		}

		dbUrl = properties.getProperty("dburl").toString();
		dbUsername = properties.getProperty("dbusername").toString();
		dbUserpassword = properties.getProperty("dbuserpassword").toString();

		if (properties.getProperty("nrof_connections") != null) {
			initialNrofConnections = Integer.valueOf(properties.getProperty("nrof_connections"));
			maxNrofConnections = Integer.valueOf(properties.getProperty("nrof_connections"));
		} else {
			initialNrofConnections = Integer.valueOf(properties.getProperty("initial_nrof_connections", "" + initialNrofConnections));
			maxNrofConnections = Integer.valueOf(properties.getProperty("max_nrof_connections", "" + maxNrofConnections));
			//set default option
			//properties.getProperty("nrof_connections", new GenericValue(initialNrofConnections)).toInteger().intValue();
		}
		connectionRequestTimeout = Integer.valueOf(properties.getProperty("connection_request_timeout", "" + connectionRequestTimeout));
		connectionTimeout = Integer.valueOf(properties.getProperty("connection_timeout", "" + connectionTimeout));
		connectionTimeoutCheck = Integer.valueOf(properties.getProperty("connection_timeout_check_interval", "" + connectionTimeoutCheck));

		connectionTestStatement = properties.getProperty("connection_test_statement", connectionTestStatement).toString();

		createReadOnlyConnections = Boolean.valueOf(properties.getProperty("create_readonly_connections", "" + createReadOnlyConnections));
		defaultIsolationLevel = isolationLevelStrings.indexOf(properties.getProperty("default_isolaton_level", "" + defaultIsolationLevel));

		try {
			Class.forName(dbDriver);
			Enumeration drivers = DriverManager.getDrivers();

			driverInfo = "";
			int count = 0;
			while (drivers.hasMoreElements()) {
				count++;
				Driver driver = (Driver) drivers.nextElement();
				driverInfo += "driver " + count + ": " + driver.getMajorVersion() + '.' + driver.getMinorVersion() + "\n";
			}
		} catch (ClassNotFoundException cnfe) {
			throw new ConfigurationException("ERROR: Driver \"" + dbDriver + "\" not found...");
		}
		resetStatistics();
	}

	private void resetStatistics() {
		nrofRequests = 0;
		nrofConnectionsDistributed = 0;
		nrofReleased = 0;
		nrofReset = 0;
		nrofRequestsTimedOut = 0;
		long nrofRequestsFailed = 0;// ??
		nrofQueuedRequests = 0;
		nrofStaleCleanedUp = 0;
		cumulatedResponseTime = 0;
		nrofErrors = 0;
	}


	/**
	 * Cleanup routine that is to be called regularly
	 */
	public void onPageEvent(long l) {
		System.out.println(new LogEntry("connection pool cleanup run"));
		cleanUpHangingConnections();
		testConnection();
	}

	/**
	 * Tests proper working of 1 connection.
	 * Since connections are rotated, this also functions as a keep-alive mechanism.
	 */
	private void testConnection() {
		System.out.println(new LogEntry("performing connection test"));
		try {
			connectionTester.executePreparedStatement(connectionTestStatement);
		} catch (SQLException e) {
			System.out.println(new LogEntry(Level.CRITICAL, "connection test failed with message: " + e.getMessage(), e));
		}
	}

	private void cleanUpHangingConnections() {
		ArrayList deadConnections = new ArrayList();
		synchronized (allConnections) {
			if (isStarted() && !usedConnections.isEmpty()) {
				Iterator i = usedConnections.iterator();
				while (i.hasNext()) {
					ConnectionWrapper connWrap = (ConnectionWrapper) i.next();
					if (connWrap.isTimedOut()) {
						deadConnections.add(connWrap);
					}
				}
				i = deadConnections.iterator();
				while (i.hasNext()) {
					ConnectionWrapper connWrap = (ConnectionWrapper) i.next();
					nrofStaleCleanedUp++;
					System.out.println(new LogEntry(Level.CRITICAL, "Connection kept occupied for more than " + connectionTimeout + " s", connWrap.toString()));
					replaceConnection(connWrap);
				}
			}
		}
	}


	/**
	 * @param username
	 * @param password
	 * @return a newly created connection
	 * @throws SQLException
	 */
	public Connection getConnection(String username, String password) throws SQLException {
		return createConnection(dbUrl, username, password);
	}


	public PrintWriter getLogWriter() {
		return logWriter;
	}


	public void setLogWriter(PrintWriter out) {
		logWriter = out;
	}

	public <T> T unwrap(Class<T> clasz) throws SQLException {
		if (this.isWrapperFor(clasz)) {
			return (T) this;
		}
		throw new SQLException("class '" + getClass().getName() + "' is not a wrapper for '" + clasz.getName() + "'");
	}

	public boolean isWrapperFor(Class<?> clasz) throws SQLException {
		return this.getClass().isAssignableFrom(clasz);
	}


	@Override
	public int getLoginTimeout() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		throw new SQLFeatureNotSupportedException();
	}


	@Override
	public void setLoginTimeout(int arg0) throws SQLException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isStarted() {
		return isStarted;
	}


}
