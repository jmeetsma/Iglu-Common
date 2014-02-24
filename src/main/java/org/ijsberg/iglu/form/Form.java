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

package org.ijsberg.iglu.form;

import org.ijsberg.iglu.configuration.ConfigurationException;
import org.ijsberg.iglu.content.module.StandardTextProvider;
import org.ijsberg.iglu.util.formatting.PatternMatchingSupport;
import org.ijsberg.iglu.util.properties.PropertiesSupport;
import org.ijsberg.iglu.util.time.SafeDateFormat;
import org.ijsberg.iglu.util.types.Converter;

import java.awt.*;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.Properties;
import java.util.Set;

/**
 * label, value, defaultValue, defaultErrorMessage, errorMessage
 */
public class Form extends StandardTextProvider {

	public static final String VALUE = "value";
	public static final String FIELD = "field";
	public static final String FORMAT = "format";
	public static final String TYPE = "type";
	public static final String REGEX = "regex";
	public static final String REQUIRED = "required";
	public static final String REQUIRED_MESSAGE = "required_message";
	public static final String FORMAT_MESSAGE = "format_message";


	public String getOverallValidationMessage() {
		return overallValidationMessage;
	}

	public void setOverallValidationMessage(String overallValidationMessage) {
		this.overallValidationMessage = overallValidationMessage;
	}

	private String overallValidationMessage;


	private Properties allFormsProperties;

	public Form(Properties allFormsProperties, String formKey) {
		super();
		this.allFormsProperties = allFormsProperties;
		copy(PropertiesSupport.getSubsection(allFormsProperties, formKey));
	}


	private void copy(Properties properties) {
		for (Object key : properties.keySet()) {
			defaultTexts.put(key, properties.get(key));
		}
	}

	public Collection getKeys(String subFormKey) {
		return PropertiesSupport.getSubsectionKeys(PropertiesSupport.getSubsection(defaultTexts, subFormKey));
	}


	public void reset() {
		convertedValues = null;
		validationMessages = null;
	}


	public void fillOut(Properties values) {

		//TODO reset
		convertedValues = null;
		validationMessages = null;

		for (Object key : values.keySet()) {
			defaultTexts.put(FIELD + "." + key + "." + VALUE, values.get(key));
		}
	}

	public void setValue(String key, Object value) {
		//TODO reset
		convertedValues = null;
		validationMessages = null;
		defaultTexts.put(FIELD + "." + key + "." + VALUE, value);
	}

	public Object getValue(String key) {
		return defaultTexts.get(FIELD + "." + key + "." + VALUE);
	}

	private Properties convertedValues;
	private Properties validationMessages;


	public boolean validate() {

		convertedValues = new Properties();
		validationMessages = new Properties();

		Properties fieldProperties = PropertiesSupport.getSubsection(defaultTexts, FIELD);

		Set<String> fieldKeys = PropertiesSupport.getSubsectionKeys(fieldProperties);

		for (String fieldKey : fieldKeys) {
			String strValue = fieldProperties.getProperty(fieldKey + "." + VALUE);
			Object objValue = fieldProperties.get(fieldKey + "." + VALUE);

			if (strValue == null || "".equals(strValue)) {
				if (strValue == null && objValue != null) {
					convertedValues.put(fieldKey, objValue);
				} else if ("true".equals(fieldProperties.getProperty(fieldKey + "." + REQUIRED, "false"))) {
					validationMessages.setProperty(fieldKey, getSpText(FIELD + "." + fieldKey, REQUIRED_MESSAGE));
				}
			} else {
				String regex = fieldProperties.getProperty(fieldKey + "." + REGEX);
				if (regex != null && !PatternMatchingSupport.valueMatchesRegularExpression(strValue, regex)) {
					validationMessages.setProperty(fieldKey, getSpText(FIELD + "." + fieldKey, FORMAT_MESSAGE));
				} else {

					String type = fieldProperties.getProperty(fieldKey + "." + TYPE);
					if (type != null) {
						Class clasz = null;
						try {
							clasz = Class.forName(type);
						} catch (ClassNotFoundException e) {
							throw new ConfigurationException("type '" + type + "' can not be found", e);
						}
						if (Date.class.isAssignableFrom(clasz)) {
							String format = fieldProperties.getProperty(fieldKey + "." + FORMAT);
							SafeDateFormat dateFormat = new SafeDateFormat(format);
							try {
								convertedValues.put(fieldKey, dateFormat.parse(strValue));
							} catch (ParseException e) {
								validationMessages.setProperty(fieldKey, getSpText(FIELD + "." + fieldKey, FORMAT_MESSAGE));
								//TODO log
//								throw new ConfigurationException("type '" + type + "' can not be converted from " + strValue +" to " + format, e);
							}
						} else {

							try {
								convertedValues.put(fieldKey, Converter.convertToObject(strValue, clasz));
							} catch (IllegalArgumentException e) {
								validationMessages.setProperty(fieldKey, getSpText(FIELD + "." + fieldKey, FORMAT_MESSAGE));
							}
						}
					} else {
						convertedValues.put(fieldKey, strValue);
					}
				}
			}
		}
		return validationMessages.isEmpty();

	}

	public void invalidate(String fieldKey, String message) {
		if(validationMessages == null) {
			validationMessages = new Properties();
		}
		validationMessages.setProperty(fieldKey, getSpText(FIELD + "." + fieldKey, message));

	}


	public Properties getValues() {

		if (convertedValues == null) {
			if (!validate()) {
				throw new ConfigurationException("trying to retrieve values of invalid form with messages " + validationMessages.toString());
			}
		}
		return convertedValues;
	}


	public String getValueAsString(String key) {

		Object value = defaultTexts.get(FIELD + "." + key + "." + VALUE);
		if (value == null) {
			return defaultText;
		}
		if (value instanceof Date) {
			String format = getText(FIELD + "." + key + "." + FORMAT);
			SafeDateFormat dateFormat = new SafeDateFormat(format);
			return dateFormat.format((Date) value);
		}
		return value.toString();

	}


	public Properties getFormData() {
		return defaultTexts;
	}


	public String getValidationMessage(String key) {

		return validationMessages != null ? validationMessages.getProperty(key, "") : "";
	}

	public Properties getValidationMessages() {
		return validationMessages;
	}


	public String getSpText(String fieldKey, String textKey) {

		String text = defaultTexts.getProperty(fieldKey + "." + textKey);

		while (text == null && fieldKey.contains(".")) {

			fieldKey = fieldKey.substring(0, fieldKey.lastIndexOf(".") - 1);
			text = defaultTexts.getProperty(fieldKey + "." + textKey);
		}
		if (text == null) {
			text = allFormsProperties.getProperty(textKey);
		}
		if (text == null) {
			text = defaultText;
		}
		if (text == null) {
			text = "";
		}

		return text;
	}


}
