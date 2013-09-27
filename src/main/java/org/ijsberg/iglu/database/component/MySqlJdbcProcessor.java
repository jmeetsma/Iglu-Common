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
import org.ijsberg.iglu.database.*;
import org.ijsberg.iglu.exception.ResourceException;
import org.ijsberg.iglu.util.reflection.ReflectionSupport;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 */
public class MySqlJdbcProcessor extends StandardJdbcProcessor {
	public MySqlJdbcProcessor() {
	}

	public MySqlJdbcProcessor(DataSource dataSource) {
		super(dataSource);
	}

	public MySqlJdbcProcessor(DataSource dataSource, DataSource dataSourceReadOnly) {
		super(dataSource, dataSourceReadOnly);
	}

	public Object insertDataObjectWithKeyGeneration(DataObject dataObject, String tableName, String columnName, Properties mapping) {
		String[] statement = new String[2];
		StringBuffer insertStatement = new StringBuffer("INSERT INTO " + tableName + " (");
		StringBuffer valuesString = new StringBuffer("VALUES(");

		Properties inputProperties = dataObject.getMapper().toProperties();
		Object[] insertInput = new Object[mapping.size() - 1];
		int count = 0;
		String primaryKeyMemberName = null;
		for (String memberName : mapping.stringPropertyNames()) {
			if (!columnName.equals(mapping.getProperty(memberName))) {
				insertStatement.append(mapping.getProperty(memberName)).append(", ");
				insertInput[count] = inputProperties.get(memberName);
				count++;
				valuesString.append("?,");
			} else {
				primaryKeyMemberName = memberName;
			}
		}
		//delete superfluous ','
		insertStatement.deleteCharAt(insertStatement.length() - 2);
		insertStatement.append(")");

		valuesString.deleteCharAt(valuesString.length() - 1);
		valuesString.append(")");

		statement[0] = insertStatement.toString() + valuesString.toString();
		statement[1] = "SELECT MAX(" + columnName + ") FROM " + tableName;

		ArrayList input = new ArrayList();
		input.add(insertInput);
		input.add(new Object[0]);

		Object[] result;
		try {
			result = this.executeMultiplePreparedStatements(statement, input);
		} catch (SQLException sqle) {
			throw new ResourceException("cannot create record in table " + tableName + " with properties " + inputProperties, sqle);
		}
		ResultSetCopy rs = (ResultSetCopy) result[1];
		rs.next();
		Object returnValue = rs.getObject(1);

		inputProperties.put(primaryKeyMemberName, returnValue);
		dataObject.getMapper().copy(inputProperties);

		return returnValue;
	}

	public void insertDataObject(DataObject dataObject, String tableName, Properties mapping) {
		StringBuffer statement = new StringBuffer("INSERT INTO " + tableName + " (");

		StringBuffer valuesString = new StringBuffer("VALUES(");

		Properties inputProperties = dataObject.getMapper().toProperties();
		Object[] input = new Object[mapping.size()];
		int count = 0;
		for (String memberName : mapping.stringPropertyNames()) {

			statement.append(mapping.getProperty(memberName)).append(", ");
			input[count] = inputProperties.get(memberName);
			valuesString.append("?,");
			count++;
		}
		//delete superfluous ','
		statement.deleteCharAt(statement.length() - 2);
		statement.append(")");

		valuesString.deleteCharAt(valuesString.length() - 1);
		valuesString.append(")");

		try {
			this.executePreparedStatement(statement.toString() + valuesString.toString(), new StatementInput(input));
		} catch (SQLException sqle) {
			throw new ResourceException("cannot create record in table " + tableName + " with properties " + inputProperties, sqle);
		}
	}

	public void updateDataObject(DataObject dataObject, String tableName, String columnName, Properties mapping) {
		StringBuffer statement = new StringBuffer("UPDATE " + tableName + " SET ");

		Properties inputProperties = dataObject.getMapper().toProperties();
		Object[] input = new Object[mapping.size() + 1];
		int count = 0;
		for (String memberName : mapping.stringPropertyNames()) {

			statement.append(mapping.getProperty(memberName)).append("=?, ");
			input[count] = inputProperties.get(memberName);
			count++;
		}
		//delete superfluous ','
		statement.deleteCharAt(statement.length() - 2);
		statement.append(" WHERE " + columnName + "=?");
		input[count] = inputProperties.get(columnName);

		try {
			this.executePreparedStatement(statement.toString(), new StatementInput(input));
		} catch (SQLException sqle) {
			throw new ResourceException("cannot update table " + tableName + " with properties " + inputProperties, sqle);
		}

	}

	public Object insertDataObjectWithKeyGeneration(TableConfig config, DataObject dataObject) {
		return insertDataObjectWithKeyGeneration(dataObject, config.getTableName(), config.getOrMapping().getProperty(config.getPkAttrName()), config.getOrMapping());
	}

	public void updateDataObject(TableConfig config, DataObject dataObject) {
		updateDataObject(dataObject, config.getTableName(),
				config.getOrMapping().getProperty(config.getPkAttrName()), config.getOrMapping());
	}

	public <T extends DataObject> T getDataObject(TableConfig config, Object key) {
		return (T)getDataObject(config.getType(),
				config.getOrMapping(), config.getTableName(), config.getOrMapping().getProperty(config.getPkAttrName()), key);
	}


	public <T extends DataObject> T getDataObject(Class<T> type, Properties mapping, String tableName, String columnName, Object key) {

		List<T> dataObjects = getDataObjects(type, mapping, tableName, columnName, key);
		if(dataObjects.size() > 0) {
			return dataObjects.get(0);
		}
		return null;
	}

	public <T extends DataObject> List<T> getDataObjects(Class<T> type, Properties mapping, String tableName, String columnName, Object key) {

		List retval = new ArrayList();
		String sqlStatement = "SELECT * FROM " + tableName + " WHERE " + columnName + "=?";
		try {
			ResultSetCopy rs = (ResultSetCopy) executePreparedStatement(sqlStatement, new StatementInput(new Object[]{key}), new ConnectionSettings().setReadOnly());
			while(rs.next()) {
				T dataObject = createDataObject(type, mapping, rs);
				retval.add(dataObject);
			}
			return retval;
		} catch (SQLException sqle) {
			throw new ResourceException("database call mislukt", sqle);
		}
	}

	public <T extends DataObject> List<T> getDataObjects(TableConfig config) {
		return (List<T>)getDataObjects(config.getType(), config.getOrMapping(), config.getTableName(), null);
	}

	public <T extends DataObject> List<T> getDataObjects(TableConfig config, String sortColumn) {
		return (List<T>)getDataObjects(config.getType(), config.getOrMapping(), config.getTableName(), sortColumn);
	}

	public <T extends DataObject> List<T> getDataObjects(Class<T> type, Properties mapping, String tableName, String sortColumn) {

		List retval = new ArrayList();
		String sqlStatement = "SELECT * FROM " + tableName + (sortColumn != null ? " ORDER BY " + sortColumn : "");
		try {
			ResultSetCopy rs = (ResultSetCopy) executePreparedStatement(sqlStatement, new ConnectionSettings().setReadOnly());
			while(rs.next()) {
				T dataObject = createDataObject(type, mapping, rs);
				retval.add(dataObject);
			}
			return retval;
		} catch (SQLException sqle) {
			throw new ResourceException("database call mislukt", sqle);
		}
	}

	private <T extends DataObject> T createDataObject(Class<T> type, Properties mapping, ResultSetCopy rs) {
		T dataObject = null;
		try {
			dataObject = ReflectionSupport.instantiateClass(type, rs.rowToProperties(mapping), mapping.stringPropertyNames());
		} catch (InstantiationException e) {
			throw new ConfigurationException("class of type " + type + " should be instantiable as data object");
		}
		return dataObject;
	}


	public void insertDataObject(TableConfig config, DataObject dataObject) {
		insertDataObject(dataObject, config.getTableName(), config.getOrMapping());
	}

	public void deleteDataObject(TableConfig config, Object key) {
		deleteDataObject(key, config.getTableName(),
				config.getOrMapping().getProperty(config.getPkAttrName()));
	}

	public void deleteDataObject(Object key, String tableName, String columnName) {

		String sqlStatement = "DELETE FROM " + tableName + " WHERE " + columnName + " = ?";
		try {
			executePreparedStatement(sqlStatement, new StatementInput(new Object[]{key}), new ConnectionSettings().setReadOnly());
		} catch (SQLException sqle) {
			throw new ResourceException("an not delete from table " + tableName + " with statement: " + sqlStatement, sqle);
		}
	}
}
