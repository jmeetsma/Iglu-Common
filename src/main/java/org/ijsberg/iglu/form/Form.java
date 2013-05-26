package org.ijsberg.iglu.form;

import org.ijsberg.iglu.util.formatting.PatternMatchingSupport;
import org.ijsberg.iglu.util.properties.PropertiesSupport;

import java.util.Properties;

/**
 * label, value, defaultValue, defaultErrorMessage, errorMessage
 */
public class Form extends Properties {

	public static final String LABEL = "label";
	public static final String INVALID = "invalid";
	public static final String VALUE = "value";
	public static final String REGEX = "regex";
	public static final String REQUIRED = "required";
	public static final String WRONG_FORMAT = "wrong_format";
	public static final String EMPTY = "empty";


	private String invalidMessage;
	private String requiredMessage;

	public Form(Properties properties) {
		for(Object key : properties.keySet()) {
			put(key, properties.get(key));
		}
	}




	private void fillOut(Properties values, String valueKey) {
		for(Object key : values.keySet()) {
			put(key + "." + valueKey, values.get(key));
		}

	}


	public boolean validate() {
		boolean isValid = true;
		for(String key : PropertiesSupport.getSubsectionKeys(this)) {
			boolean isFieldValid = true;

			String value = getProperty(key + "." + VALUE);
			if(value == null || "".equals(value)) {
				if("true".equals(getProperty(key + "." + REQUIRED, "false"))) {
					isValid = false;
				}
			} else {
				isValid = PatternMatchingSupport.valueMatchesRegularExpression(getProperty(key + "." + VALUE), getProperty(key + "." + REGEX, ""));

			}
//			setProperty(key + "." + VALID, "" + isValid);


		}
		return true;

	}


}
