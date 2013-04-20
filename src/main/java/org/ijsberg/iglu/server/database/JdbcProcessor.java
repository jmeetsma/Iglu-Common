/* =======================================================================
 * Copyright (c) 2003-2010 IJsberg Automatisering BV. All rights reserved.
 * Redistribution and use of this code are permitted provided that the
 * conditions of the Iglu License are met.
 * The license can be found in org.ijsberg.iglu.StandardApplication.java
 * and is also published on http://iglu.ijsberg.org/LICENSE.
 * =======================================================================
 */
package org.ijsberg.iglu.server.database;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;


/**
 * Implementations of this interface can handle a variety of calls to relational databases.
 * Primary goal is to hide specific database settings and avoid dealing with connections
 * resultsets etc. for layers containing the functional database interface.
 */
public interface JdbcProcessor
{

	/**
	 * Executes a prepared statement on a relational database
	 *
	 * @param statement declaration of the prepared statement
	 * @return the outcome of the execution of the stored procedure, this may be null, any kind of object, or a ResultSetCopy
	 * @throws SQLException if execution fails
	 */
	Object executePreparedStatement(String statement) throws SQLException;

	/**
	 * Executes a prepared statement on a relational database
	 *
	 * @param statement declaration of the prepared statement
	 * @return the outcome of the execution of the stored procedure, this may be null, any kind of object, or a ResultSetCopy
	 * @throws SQLException if execution fails
	 */
	Object executePreparedStatement(String statement, StatementInput input) throws SQLException;


	/**
	 * Executes a prepared statement on a relational database
	 *
	 * @param statement declaration of the prepared statement
	 * @param resolveLOBs a flag indicating that LOBs should be read into a String or byte array
	 * @return the outcome of the execution of the stored procedure, this may be null, any kind of object, or a ResultSetCopy
	 * @throws SQLException if execution fails
	 */
	Object executePreparedStatement(String statement, StatementInput input, boolean resolveLOBs) throws SQLException;

	Object executePreparedStatement(String statement, StatementInput input, ConnectionSettings settings) throws SQLException;


	Object executePreparedStatement(String statement, ConnectionSettings settings) throws SQLException;

	Object executePreparedStatement(String statement, ConnectionSettings settings, boolean resolveLOBs) throws SQLException;

	/**
	 * Executes a prepared statement on a relational database
	 *
	 * @param statement declaration of the prepared statement
	 * @param resolveLOBs a flag indicating that LOBs should be read into a String or byte array
	 * @return the outcome of the execution of the stored procedure, this may be null, any kind of object, or a ResultSetCopy
	 * @throws SQLException if execution fails
	 */
	Object executePreparedStatement(String statement, StatementInput input, ConnectionSettings settings, boolean resolveLOBs) throws SQLException;

	/**
	 * Executes an array of prepared statement in a single transaction (if supported)
	 *
	 * @param statements an array of prepared statement declarations
	 * @param inParams a collection of arrays of parameters to feed the stored procedure. the size of the array must match the length of the array of stored procedures
	 * @return an array of the outcomes of the execution of the stored procedures, these may be null, any kind of object, or a ResultSetCopy
	 * @throws SQLException if execution fails
	 */
	Object[] executeMultiplePreparedStatements(String[] statements, Collection inParams) throws SQLException;

	/**
	 * Executes a prepared statement on a relational database
	 *
	 * @param statement		declaration of the prepared statement
	 * @param outParamType	 expected result type e.g. the code for VARCHAR
	 * @param inParams		 an array of parameters to feed the stored procedure
	 * @param specificSqlTypes an array of SQL-types to specify the params parameters
	 * @param hasReturnVal	 a flag indicated if a return value is expected
	 * @param resolveLOBs	  a flag indicating that LOBs should be read into a String or byte array
	 * @param readOnly		 a flag indicating that a read-only connection should be used
	 * @param isCallable	   must be true if the statement should be regerded as a stored procedure instead of an SQL statment
	 * @return the outcome of the execution of the stored procedure, this may be null, any kind of object, or a ResultSetCopy
	 * @throws SQLException if execution fails
	 */
//	Object executePreparedStatement(String statement, int outParamType, Object[] inParams, int[] specificSqlTypes, boolean hasReturnVal, boolean resolveLOBs, boolean readOnly, boolean isCallable) throws SQLException;

//	Object executePreparedStatement(String statement, StatementInput input, ConnectionSettings settings, boolean resolveLOBs, boolean isCallable) throws SQLException;

	/**
	 * Executes a prepared statement on a relational database
	 *
	 * @param statement declaration of the prepared statement
	 * @param resolveLOBs a flag indicating that LOBs should be read into a String or byte array
	 * @param isCallable must be true if the statement should be regerded as a stored procedure instead of an SQL statment
	 * @param connection the database connection to use
	 * @return the outcome of the execution of the stored procedure, this may be null, any kind of object, or a ResultSetCopy
	 * @throws SQLException if execution fails
	 */
//	Object executePreparedStatement(String statement, int outParamType, Object[] inParams, int[] specificSqlTypes, boolean hasReturnVal, boolean resolveLOBs, boolean readOnly, boolean isCallable, Connection connection) throws SQLException;

	Object executePreparedStatement(String statement, StatementInput input, boolean resolveLOBs, boolean isCallable, Connection connection) throws SQLException;

	/**
	 * Executes a prepared statement on a relational database
	 *
	 * @param callableStatement callable statement (prepared, parameters not yet bound)
	 * @return the outcome of the execution of the stored procedure, this may be null, any kind of object, or a ResultSetCopy
	 * @throws SQLException if execution fails
	 */
	Object executeStoredProcedure(CallableStatement callableStatement, StatementInput input) throws SQLException;

	/**
	 * Executes a prepared statement on a relational database
	 *
	 * @param preparedStatement prepared statement (prepared, parameters not yet bound)
	 * @return the outcome of the execution of the stored procedure, this may be null, any kind of object, or a ResultSetCopy
	 * @throws SQLException if execution fails
	 */
	Object executePreparedStatement(PreparedStatement preparedStatement, StatementInput input) throws SQLException;

	/**
	 * A batch prepared statement is capable of carrying out a batch of updates in one go
	 * The statement or stored procedure must have been constructed in a such a way that it can handle a batch update
	 *
	 * @param statement declaration of the prepared statement
	 * @param inParamsBatch a collection of arrays of parameters to feed the stored procedure
	 * @param isCallable must be true if the statement should be regerded as a stored procedure instead of an SQL statment
	 * @return an array of the number of affected rows for each statement
	 * @throws SQLException if execution fails
	 */
	int[] executeBatchPreparedStatement(String statement, Collection inParamsBatch, boolean isCallable) throws SQLException;

	/**
	 * A batch prepared statement is capable of carrying out a batch of updates in one go
	 * The statement or stored procedure must have been constructed in a such a way that it can handle a batch update
	 *
	 * @param statement declaration of the prepared statement
	 * @param inParamsBatch a collection of arrays of parameters to feed the stored procedure
	 * @return the number of affected rows
	 * @throws SQLException if execution fails
	 */
	int[] executeBatchPreparedStatement(String statement, Collection inParamsBatch) throws SQLException;

	/**
	 * @param sqlQuery SQL query statement
	 * @return a copy of the result set
	 * @throws SQLException if execution fails
	 */
	ResultSetCopy executeQuery(String sqlQuery) throws SQLException;

	/**
	 * @param sqlUpdate SQL update statement
	 * @return the number of affected rows
	 * @throws SQLException if execution fails
	 */
	int executeUpdate(String sqlUpdate) throws SQLException;
}
