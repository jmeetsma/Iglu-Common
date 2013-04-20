/* =======================================================================
 * Copyright (c) 2003-2010 IJsberg Automatisering BV. All rights reserved.
 * Redistribution and use of this code are permitted provided that the
 * conditions of the Iglu License are met.
 * The license can be found in org.ijsberg.iglu.StandardApplication.java
 * and is also published on http://iglu.ijsberg.org/LICENSE.
 * =======================================================================
 */
package org.ijsberg.iglu.server.database.component;

import org.ijsberg.iglu.logging.Level;
import org.ijsberg.iglu.logging.LogEntry;
import org.ijsberg.iglu.server.database.*;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

/**
 * Implementation of JdbcProcessor containing a minimum of code to
 * carry out specific Jdbc functions. Its main reason for existence
 * is to act as a single point of failure.
 * <p/>
 * StandardJdbcProcessor is able to store ResultSet copies for later use,
 * for instance in a test environment where a database might not be available
 * <p/>
 * StandardJdbcProcessor is able to cache ResultSet copies for common database calls
 * <p/>
 * This class may be extended to form manageable database layers
 */


public class StandardJdbcProcessor implements JdbcProcessor
{
	//TODO exception handling
	//there are categories of exceptions:
	//1. connection trouble
	//2. bind trouble
	//3. bug in stored procedure / database
	//4. database glitch (packages discarded)
	//5. database valid complaint (index key violation)
	//6. result read trouble

	//name of the connectionpool component
	//	private String connMgrComp = "CONNECTION_POOL";


	//maximum number of rows of a result set to log
	private int maxRowLog = 100;

	//source of database connections
	private DataSource dataSource;
	private DataSource dataSourceReadOnly;

	//TODO move this setting to ConnectionPool
	private boolean allowIsolationLevelOverride = false;


	/**
	 */
	public StandardJdbcProcessor()
	{
	}


	/**
	 */
	public StandardJdbcProcessor(DataSource dataSource)
	{
		super();
		this.dataSource = dataSource;
	}


	/**
	 */
	public StandardJdbcProcessor(DataSource dataSource, DataSource dataSourceReadOnly)
	{
		super();
		this.dataSource = dataSource;
		this.dataSourceReadOnly = dataSourceReadOnly;
	}

	
	public void setProperties(Properties properties) {
//		connMgrComp = section.getValue("DataSource", new GenericValue(connMgrComp), "name of the component that provides connections").toString();
		maxRowLog = Integer.valueOf(properties.getProperty("max_row_log", "" + maxRowLog));
		//TODO document this feature
		allowIsolationLevelOverride = Boolean.valueOf(properties.getProperty("allow_isolationlevel_override", "" + allowIsolationLevelOverride));
	}




	public DataSource getDataSource()
	{
		return dataSource;
	}

	////////////////////////////////
	//                            //
	//  PREPARED STATEMENT CALLS  //
	//                            //
	////////////////////////////////

	/**
	 * Executes a prepared statement without input and with default connections,
	 * such as use of a connection that is not read-only.
	 * 
	 * @param statement
	 * @return
	 * @throws SQLException
	 */
	public Object executePreparedStatement(String statement) throws SQLException
	{
		return executePreparedStatement(statement, new StatementInput(), new ConnectionSettings(), false);
	}

	public Object executePreparedStatement(String statement, StatementInput input) throws SQLException
	{
		return executePreparedStatement(statement, input, new ConnectionSettings(), false);
	}

	public Object executePreparedStatement(String statement, StatementInput input, ConnectionSettings settings) throws SQLException
	{
		return executePreparedStatement(statement, input, settings, false);
	}
	
	public Object executePreparedStatement(String statement, StatementInput input, boolean resolveLOBs) throws SQLException
	{
		return executePreparedStatement(statement, input, new ConnectionSettings(), resolveLOBs);
	}

	public Object executePreparedStatement(String statement, ConnectionSettings settings) throws SQLException
	{
		return executePreparedStatement(statement, new StatementInput(), settings, false);
	}

	public Object executePreparedStatement(String statement, ConnectionSettings settings, boolean resolveLOBs) throws SQLException
	{
		return executePreparedStatement(statement, new StatementInput(), settings, resolveLOBs);
	}//	public Object executePreparedStatement(String statement, int outParamType, Object[] inParams, int[] specificSqlTypes, boolean hasReturnVal, boolean resolveLOBs, boolean readOnly, boolean isCallable) throws SQLException

	////////////////////////////////////////
	//                                    //
	//  GENERIC PREPARED STATEMENT CALLS  //
	//                                    //
	////////////////////////////////////////

	public Object[] executeMultiplePreparedStatements(String[] statements, Collection inParams) throws SQLException
	{
		Object[] result;
		if (statements.length == inParams.size())
		{
			result = new Object[inParams.size()];
			Connection conn = null;

			conn = dataSource.getConnection();
			conn.setAutoCommit(false);
			
			try
			{
				Iterator inParamsIt = inParams.iterator();
				int count = 0;
				while (inParamsIt.hasNext())
				{
					Object[] params = (Object[]) inParamsIt.next();
					result[count] = executePreparedStatement(statements[count], new StatementInput(params), false, false, conn);
					count++;
				}
				conn.commit();
			}
			catch (SQLException e)
			{
				throw e;
			}
			finally
			{
				if (conn != null)
				{
					conn.close();
				}
			}
		}
		else
		{
			throw new SQLException("number of arguments doesn't match");
		}
		return result;
	}


	public Object executePreparedStatement(String statement, StatementInput input, ConnectionSettings settings, boolean resolveLOBs) throws SQLException
	{
		//TODO check if this component and connectionmanager are active!!! and connection != null
		//TODO move getConnection to separate method
		//TODO dataSource not necessary when PLAYing

		Connection conn = null;

		if (settings.readOnly && dataSourceReadOnly != null)
		{
			conn = dataSourceReadOnly.getConnection();
		}
		else
		{
			conn = dataSource.getConnection();
		}
		if (allowIsolationLevelOverride)
		{
			//TODO connections seem to go stale or something and throw NullPointerExc here
			//TODO nullpointer here
			conn.setTransactionIsolation(settings.isolationLevel);
		}
		try
		{
			return executePreparedStatement(statement, input, resolveLOBs, settings.invokeStoredProcedure, conn);
		}
		finally
		{
			if (conn != null)
			{
				conn.close();
			}
		}
	}


	//	public Object executePreparedStatement(String statement, int outParamType, Object[] inParams, int[] specificSqlTypes, boolean hasReturnVal, boolean resolveLOBs, boolean readOnly, boolean isCallable, Connection conn) throws SQLException
	public Object executePreparedStatement(String statement, StatementInput input, boolean resolveLOBs, boolean isCallable, Connection conn) throws SQLException
	{
		Object result = null;
		ResultSet rs = null;
		StringBuffer statementDescription = new StringBuffer(statement);

		for (int i = 0; i < (input.params.length); i++)
		{
			if (input.params[i] == null)
			{
				if (input.sqlTypes != null)
				{
					statementDescription.append("[null(" + input.sqlTypes[i] + ":resolved)]");
				}
				else
				{
					input.params[i] = new JdbcNullObject(Types.VARCHAR);
					statementDescription.append("[null(VARCHAR:guessed)]");
				}
			}
			else if (input.params[i] instanceof JdbcNullObject)
			{
				statementDescription.append("[null(marked by JdbcNullObject)]");
			}
			else
			{
				statementDescription.append("[" + input.params[i] + ']');
			}
		}

		PreparedStatement ps = null;

		try
		{
			System.out.println(new LogEntry("executing:" + statementDescription + " using " + (conn.isReadOnly() ? "READ ONLY " : "") + "connection " + conn));

			if (isCallable)
			{
				ps = conn.prepareCall(statement);
//				StandardEventTimer.startTimingEvent(deviceId, statement);
				result = executeStoredProcedure((CallableStatement) ps, input);
//				StandardEventTimer.stopTimingEvent();
			}
			else
			{
				ps = conn.prepareStatement(statement);
//				StandardEventTimer.startTimingEvent(deviceId, statement);
				result = executePreparedStatement(ps, input);
//				StandardEventTimer.stopTimingEvent();
			}
			if (!input.returnsVoid)
			{
				if (result instanceof ResultSet)
				{
					rs = (ResultSet) result;
					result = new ResultSetCopy(rs, maxRowLog, resolveLOBs);
					rs.close();
				}
				System.out.println(new LogEntry("InvocationResult of database call: " + result));
			}
			System.out.println(new LogEntry(result != null ? " (return type: " + result.getClass().getName() + ')' : ""));
			return result;
		}
		catch (SQLException sqle)
		{
//some SQL errors occur due to:
// -communication problems -> reset connection(pool) / retry
// -deadlock or other internal database problems - retry
// -error in SQL - forward exception
// -database constraint - forward exception
//TODO load a list of known exceptions and how to handle them
			//TODO resetconnection must immediately return the freshly made connection
			//reset connection, try again, that's it (no list)
//			StandardEventTimer.abortTimingEvent(sqle.getMessage() + '(' + statementDescription + ')');
			System.out.println(new LogEntry(sqle));
			throw new SQLException(sqle.getMessage() + '(' + statementDescription + ')');
		}
		catch (IOException ioe)
		{
//			StandardEventTimer.abortTimingEvent("failed to read LOB with message: " + ioe.getMessage() + '(' + statement + ')');
			System.out.println(new LogEntry("failed to read LOB", ioe));
			throw new SQLException("failed to read LOB with message: " + ioe.getMessage() + '(' + statement + ')');
		}
		finally
		{
			if (ps != null)
			{
//				try
				{
					ps.close();
				}
//				catch (IOException e)
				{
				}
			}
/*			if (conn != null)
			{
				conn.terminateSession();
			}
*/
		}
	}


	public Object executeStoredProcedure(CallableStatement callableStatement, StatementInput input) throws SQLException
	{
		int paramCountCorrection = 1;
		if (!input.returnsVoid)
		{
			callableStatement.registerOutParameter(1, input.returnValType);
			paramCountCorrection = 2;
		}
		bindParameters(callableStatement, input, paramCountCorrection);
		callableStatement.execute();
		Object result = null;
		if (!input.returnsVoid)
		{
			result = callableStatement.getObject(1);
		}
		return result;
	}


	public Object executePreparedStatement(PreparedStatement preparedStatement, StatementInput input) throws SQLException
	{
		int paramCountCorrection = 1;
		bindParameters(preparedStatement, input, paramCountCorrection);
		Object result = null;
		preparedStatement.execute();

		result = preparedStatement.getResultSet();
		if (result == null)
		{
			result = new Integer(preparedStatement.getUpdateCount());
		}
		return result;
	}


	private static void bindParameters(PreparedStatement preparedStatement, StatementInput input, int paramCountCorrection) throws SQLException
	{
		boolean errorOccurred = false;

		for (int i = 0; i < (input.params.length); i++)
		{
			try
			{
				if (input.params[i] == null)
				{
					if (input.sqlTypes != null)
					{
						//find out an approprate datatype
						preparedStatement.setNull(i + paramCountCorrection, input.sqlTypes[i]);
					}
					else
					{
						preparedStatement.setNull(i + paramCountCorrection, Types.BIGINT);
					}
				}
				else if (input.params[i] instanceof JdbcNullObject)
				{
					preparedStatement.setNull(i + paramCountCorrection, ((JdbcNullObject) input.params[i]).getType());
				}
				else
				{
					preparedStatement.setObject(i + paramCountCorrection, input.params[i]);
				}
			}
			catch (SQLException e)
			{
				System.out.println(new LogEntry("Error while binding object (" + i + ':' + input.params[i] + ") to prepared statement", e));
				errorOccurred = true;
			}
		}
		if (errorOccurred)
		{
			throw new SQLException("Errors occured while processing prepared statement");
		}
	}

	//////////////////////////////////////
	//                                  //
	//  BATCH PREPARED STATEMENT CALLS  //
	//                                  //
	//////////////////////////////////////

	public int[] executeBatchPreparedStatement(String statement, Collection inParamsBatch) throws SQLException
	{
		return executeBatchPreparedStatement(statement, inParamsBatch, true);
	}

	/**
	 * A batch prepared statement is capable of carrying out a batch of updates in one go
	 * The statement or stored procedure must have been constructed in a such a way that it can handle a batch update
	 *
	 * @param statement declaration of the prepared statement
	 * @param inParamsBatch a collection of arrays of parameters to feed the stored procedure
	 * @param isCallable must be true if the statement should be regarded as a stored procedure instead of an SQL statment
	 * @return an array containing the number of affected rows per statement
	 * @throws SQLException if execution fails
	 */
	public int[] executeBatchPreparedStatement(String statement, Collection inParamsBatch, boolean isCallable) throws SQLException
	{
		Connection conn = null;
//		boolean errorOccurred = false;
		PreparedStatement ps = null;

		try
		{
			//read only connections not supported
			conn = dataSource.getConnection();
			conn.setAutoCommit(false);


			if (isCallable)
			{
				ps = conn.prepareCall(statement);
			}
			else
			{
				ps = conn.prepareStatement(statement);
			}

//			ps = conn.prepareCall(statement);

			Iterator x = inParamsBatch.iterator();

			System.out.println(new LogEntry("Executing:" + statement + " for " + inParamsBatch.size() + " input rows"));

			while (x.hasNext())
			{
				Object[] inParams = (Object[]) x.next();

				for (int i = 0; i < (inParams.length); i++)
				{
					try
					{
						if (inParams[i] instanceof JdbcNullObject)
						{
							ps.setNull(i + 1, ((JdbcNullObject) inParams[i]).getType());
						}
						else
						{
							ps.setObject(i + 1, inParams[i]);
						}
					}
					catch (SQLException e)
					{
						System.out.println(new LogEntry(e));
						throw new SQLException("Error while binding object (nr " + i + ": [" + inParams[i] + "]) to stored procedure.", "SP:" + statement + "\n" + "OBJECT:" + inParams[i]);
//						errorOccurred = true;
					}
				}
/*				if (errorOccurred)
				{
					throw new SQLException("Errors occured while processing callable statement (" + statement + "), view previous exceptions for details...");
				}
*/
				ps.addBatch();
			}
			int[] i = ps.executeBatch();
			//TODO what if execution wasn't successful for all statements
			conn.commit();
			return i;
		}
		catch (SQLException e)
		{
			StringBuffer sb = new StringBuffer(e.getMessage() + '(' + statement + ")\n\n");
			Iterator x = inParamsBatch.iterator();
			while (x.hasNext())
			{
				sb.append('{');
				Object[] inParams = (Object[]) x.next();
				for (int i = 0; i < inParams.length; i++)
				{
					sb.append((i != 0 ? ", " : "") + (inParams[i] instanceof String ? "'" : "") + inParams[i] + (inParams[i] instanceof String ? "'" : ""));
				}
				sb.append("}\n");
			}
			System.out.println(new LogEntry(Level.CRITICAL, "batch processing of " + statement + " failed", sb));
			throw new SQLException(e.getMessage() + '(' + statement + ')');
		}
		finally
		{
			if (ps != null)
			{
				ps.close();
			}
			if (conn != null)
			{
				conn.close();
			}
		}
	}

	///////////////
	//           //
	//  QUERIES  //
	//           //
	///////////////


	public ResultSetCopy executeQuery(String query) throws SQLException
	{
		System.out.println(new LogEntry("executing: " + query));

		ResultSetCopy result = null;

		//time execution time
		Connection conn;
		//use readonly connection if available
		if (dataSourceReadOnly != null)
		{
			conn = dataSourceReadOnly.getConnection();
		}
		else
		{
			conn = dataSource.getConnection();
		}
		Statement s = conn.createStatement();
		ResultSet rs = s.executeQuery(query);
		result = new ResultSetCopy(rs, maxRowLog);
		rs.close();
		conn.close();
		System.out.println(new LogEntry("", result));
		return result;
	}

	///////////////
	//           //
	//  UPDATES  //
	//           //
	///////////////

	public int executeUpdate(String update) throws SQLException
	{
		System.out.println(new LogEntry("executing: " + update));

		//time execution time
		Connection conn = dataSource.getConnection();
		Statement s = conn.createStatement();
		int result = s.executeUpdate(update);
		conn.close();
		System.out.println(new LogEntry(result + " rows affected"));
		return result;
	}


}
