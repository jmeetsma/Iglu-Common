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
package org.ijsberg.iglu.database;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;


/**
 * Implementations of this interface can handle a variety of calls to relational databases.
 * Primary goal is to hide specific database settings and avoid dealing with connections
 * resultsets etc. for layers containing the functional database interface.
 */
public interface JdbcProcessor {

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
	 * @return the outcome of the execution of the stored procedure, this may be null, any kind of object, or a ResultSetCopy
	 * @throws SQLException if execution fails
	 */
	Object executePreparedStatement(String statement, StatementInput input, ConnectionSettings settings) throws SQLException;


	Object executePreparedStatement(String statement, ConnectionSettings settings) throws SQLException;


	/**
	 * Executes an array of prepared statement in a single transaction (if supported)
	 *
	 * @param statements an array of prepared statement declarations
	 * @param inParams   a collection of arrays of parameters to feed the stored procedure. the size of the array must match the length of the array of stored procedures
	 * @return an array of the outcomes of the execution of the stored procedures, these may be null, any kind of object, or a ResultSetCopy
	 * @throws SQLException if execution fails
	 */
	Object[] executeMultiplePreparedStatements(String[] statements, Collection inParams) throws SQLException;


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
	 * @param statement     declaration of the prepared statement
	 * @param inParamsBatch a collection of arrays of parameters to feed the stored procedure
	 * @param isCallable    must be true if the statement should be regerded as a stored procedure instead of an SQL statment
	 * @return an array of the number of affected rows for each statement
	 * @throws SQLException if execution fails
	 */
	int[] executeBatchPreparedStatement(String statement, Collection inParamsBatch, boolean isCallable) throws SQLException;

	/**
	 * A batch prepared statement is capable of carrying out a batch of updates in one go
	 * The statement or stored procedure must have been constructed in a such a way that it can handle a batch update
	 *
	 * @param statement     declaration of the prepared statement
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
