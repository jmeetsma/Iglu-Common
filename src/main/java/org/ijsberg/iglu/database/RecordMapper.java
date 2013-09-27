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

package org.ijsberg.iglu.database;

import org.ijsberg.iglu.configuration.ConfigurationException;
import org.ijsberg.iglu.mvc.Mapping;
import org.ijsberg.iglu.util.types.Converter;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;

/**
 */
public class RecordMapper<T extends DataObject> {

	private HashMap<String, Field> fields;
	private Set<String> fieldNames;
	protected T dataObject;
	private Properties mapping;
	private String entityName;


	public String getFirstFieldName() {
		return (String)fieldNames.toArray()[0];
	}

	public Object getFirstFieldValue() {
		try {
			return getField(fields.get(getFirstFieldName()));
		} catch (IllegalAccessException e) {
			throw new ConfigurationException("field " + getFirstFieldName() + " not accessible", e);
		}
	}

	public RecordMapper(T dataObject, Properties mapping, String entityName) {
		this.dataObject = dataObject;
		this.fieldNames = mapping.stringPropertyNames();
		fields = getFieldMap(dataObject.getClass(), fieldNames);
		this.mapping = mapping;
		this.entityName = entityName;
	}

	public RecordMapper(T dataObject, HashMap<String, Field> fieldMap) {
		this.dataObject = dataObject;
		this.fieldNames = fieldMap.keySet();
		fields = fieldMap;
	}

	public RecordMapper(T dataObject, Set<String> fieldNames) {
		this.fieldNames = fieldNames;
		this.dataObject = dataObject;
		fields = getFieldMap(dataObject.getClass(), fieldNames);
	}

	private static HashMap<String, Field> getFieldMap(Class dataClass, Set<String> fieldNames) {
		HashMap<String, Field> fieldMap = new HashMap<String, Field>();
		Class tmpClass = dataClass;
		while (tmpClass != null) {
			for (Field field : tmpClass.getDeclaredFields()) {
				if (fieldNames.contains(field.getName()) && !fieldMap.containsKey(field.getName())) {
					fieldMap.put(field.getName(), field);
				}
			}
			tmpClass = tmpClass.getSuperclass();
		}
		if (fieldMap.size() != fieldNames.size()) {
			throw new ConfigurationException("not all fields in " + dataClass + " can be located: found " + fieldMap.keySet() + " != configured " + fieldNames);
		}
		return fieldMap;
	}


	public Properties toProperties() {

		Properties retval = new Properties();
		for (String fieldName : fieldNames) {
			try {
				Field field = fields.get(fieldName);
				Object value = getField(field);
				if (value != null) {
					retval.put(fieldName, value);
				}
			} catch (IllegalAccessException e) {
				throw new ConfigurationException("field " + fieldName + " specified in mapping for class " + dataObject.getClass() + " is not accessible", e);
			}
		}
		return retval;
	}


	public void copy(Properties properties) {

		for (String fieldName : fieldNames) {
			try {
				Field field = fields.get(fieldName);
				Object value = properties.get(fieldName);
				if (value != null) {
					value = Converter.convertToObject(value, field.getType());
				}
				setField(field, value);
			} catch (IllegalAccessException e) {
				throw new ConfigurationException("field " + fieldName + " specified in mapping for class " + dataObject.getClass() + " is not accessible", e);
			}
		}
	}


	/**
	 * Implement in inner class in domain class decorator to ensure access to private and protected fields
	 *
	 * @param field
	 * @param object
	 * @throws IllegalAccessException
	 */
	protected void setField(Field field, Object object) throws IllegalAccessException {
		dataObject.setField(field, object);
	}

	/**
	 * Implement in inner class in domain class decorator to ensure access to private and protected fields
	 *
	 * @param field
	 * @return
	 * @throws IllegalAccessException
	 */
	protected Object getField(Field field) throws IllegalAccessException {
		return dataObject.getField(field);
	}
}
