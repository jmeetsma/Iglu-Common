package org.ijsberg.iglu.server.database;

import org.ijsberg.iglu.configuration.ConfigurationException;
import org.ijsberg.iglu.util.types.Converter;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;

/**
 */
public abstract class RecordMapper<T> {

	private HashMap<String, Field> fields;
	private Set<String> fieldNames;
	protected T dataObject;

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
		while(dataClass != null) {
			for(Field field : dataClass.getDeclaredFields()) {
				if(fieldNames.contains(field.getName()) && !fieldMap.containsKey(field.getName())) {
					fieldMap.put(field.getName(), field);
				}
			}
			dataClass = dataClass.getSuperclass();
		}
		if(fieldMap.size() != fieldNames.size()) {
			throw new ConfigurationException("not all fields in " + dataClass + " can be located: " + fieldMap.keySet() + " != " + fieldNames);
		}
		return  fieldMap;
	}


	public Properties toProperties() {

		Properties retval = new Properties();
		for(String fieldName : fieldNames) {
			try {
				Field field = fields.get(fieldName);
				Object value = getField(field);
				if(value != null) {
					retval.put(fieldName, value);
				}
			} catch (IllegalAccessException e) {
				throw new ConfigurationException("field " + fieldName + " specified in mapping for class " + dataObject.getClass() + " is not accessible", e);
			}
		}
		return retval;
	}


	public void copy(Properties properties) {

		for(String fieldName : fieldNames) {
			try {
				Field field = fields.get(fieldName);
				Object value = properties.get(fieldName);
				if(value != null) {
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
	protected abstract void setField(Field field, Object object) throws IllegalAccessException;

	/**
	 * Implement in inner class in domain class decorator to ensure access to private and protected fields
	 *
	 * @param field
	 * @return
	 * @throws IllegalAccessException
	 */
	protected abstract Object getField(Field field) throws IllegalAccessException;
}