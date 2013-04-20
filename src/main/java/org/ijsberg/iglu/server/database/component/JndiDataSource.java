/* =======================================================================
 * Copyright (c) 2003-2010 IJsberg Automatisering BV. All rights reserved.
 * Redistribution and use of this code are permitted provided that the
 * conditions of the Iglu License are met.
 * The license can be found in org.ijsberg.iglu.StandardApplication.java
 * and is also published on http://iglu.ijsberg.org/LICENSE.
 * =======================================================================
 */
package org.ijsberg.iglu.server.database.component;


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
public class JndiDataSource implements DataSource
{

	private DataSource dataSource;

	/**
	 *
	 */
	protected void _start()
	{
	}

	/**
	 *
	 */
	protected void _stop()
	{

	}

	/**
	 *
	 */
	public void setProperties(Properties properties)
	{
		String jndiName = properties.getProperty("jndi_name", "java:comp/env/jdbc/myDataSource");
		try
		{
			InitialContext ctx = new InitialContext();
			dataSource = (DataSource) ctx.lookup(jndiName);
		}
		catch (NamingException ne)
		{
			throw new ResourceException("data source " + jndiName + " not found", ne);
		}

	}

	/**
	 * @return
	 * @throws SQLException
	 */
	public Connection getConnection() throws SQLException
	{
		return dataSource.getConnection();
	}

	/**
	 * @param username
	 * @param password
	 * @return a newly created connection
	 * @throws SQLException
	 */
	public Connection getConnection(String username, String password) throws SQLException
	{
		return dataSource.getConnection(username, password);
	}

	/**
	 * @return
	 * @throws SQLException
	 */
	public PrintWriter getLogWriter() throws SQLException
	{
		return dataSource.getLogWriter();
	}

	/**
	 * @param out
	 * @throws SQLException
	 */
	public void setLogWriter(PrintWriter out) throws SQLException
	{
		dataSource.setLogWriter(out);
	}

	/**
	 * @param seconds
	 * @throws SQLException
	 */
	public void setLoginTimeout(int seconds) throws SQLException
	{
		dataSource.setLoginTimeout(seconds);
	}

	/**
	 * @return
	 * @throws SQLException
	 */
	public int getLoginTimeout() throws SQLException
	{
		return dataSource.getLoginTimeout();
	}

//    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException();
        //return dataSource.getParentLogger();
    }


    public Object unwrap(Class clasz) throws SQLException
	{
        if(this.isWrapperFor(clasz))
        {
		    return dataSource;
        }
        throw new SQLException("class '" + getClass().getName() + "' is not a wrapper for '" + clasz.getName() + "'");
	}

	public boolean isWrapperFor(Class clasz)
	{
        return dataSource.getClass().isAssignableFrom(clasz);
	}
}
