/*
 * Copyright 2011-2014 Jeroen Meetsma - IJsberg Automatisering BV
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


import org.ijsberg.iglu.exception.ResourceException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Acts as a placeholder for a data source which is obtained by a JNDI lookup.
 */
public class JndiDataSource implements DataSource {

	private DataSource dataSource;


	/**
	 *
	 */
	public void setProperties(Properties properties) {
		String jndiName = properties.getProperty("jndi_name", "java:comp/env/jdbc/myDataSource");
		try {
			InitialContext ctx = new InitialContext();
			dataSource = (DataSource) ctx.lookup(jndiName);
		} catch (NamingException ne) {
			throw new ResourceException("data source " + jndiName + " not found", ne);
		}

	}

	/**
	 * @return
	 * @throws SQLException
	 */
	public Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}

	/**
	 * @param username
	 * @param password
	 * @return a newly created connection
	 * @throws SQLException
	 */
	public Connection getConnection(String username, String password) throws SQLException {
		return dataSource.getConnection(username, password);
	}

	/**
	 * @return
	 * @throws SQLException
	 */
	public PrintWriter getLogWriter() throws SQLException {
		return dataSource.getLogWriter();
	}

	/**
	 * @param out
	 * @throws SQLException
	 */
	public void setLogWriter(PrintWriter out) throws SQLException {
		dataSource.setLogWriter(out);
	}

	/**
	 * @param seconds
	 * @throws SQLException
	 */
	public void setLoginTimeout(int seconds) throws SQLException {
		dataSource.setLoginTimeout(seconds);
	}

	/**
	 * @return
	 * @throws SQLException
	 */
	public int getLoginTimeout() throws SQLException {
		return dataSource.getLoginTimeout();
	}

	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		throw new SQLFeatureNotSupportedException();
	}


	public Object unwrap(Class clasz) throws SQLException {
		if (this.isWrapperFor(clasz)) {
			return dataSource;
		}
		throw new SQLException("class '" + getClass().getName() + "' is not a wrapper for '" + clasz.getName() + "'");
	}


	public boolean isWrapperFor(Class clasz) {
		return dataSource.getClass().isAssignableFrom(clasz);
	}
}
