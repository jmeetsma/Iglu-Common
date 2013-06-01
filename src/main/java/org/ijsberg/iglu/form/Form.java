package org.ijsberg.iglu.form;

import org.ijsberg.iglu.content.module.StandardTextProvider;
import org.ijsberg.iglu.util.formatting.PatternMatchingSupport;
import org.ijsberg.iglu.util.properties.PropertiesSupport;

import java.util.Collection;
import java.util.Properties;

/**
 * label, value, defaultValue, defaultErrorMessage, errorMessage
 */
public class Form extends StandardTextProvider {

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
		super();
		copy(properties);
	}


	private void copy(Properties properties) {
		for(Object key : properties.keySet()) {
			defaultTexts.put(key, properties.get(key));
		}
	}

	public Collection getKeys(String subFormKey) {
		return PropertiesSupport.getSubsectionKeys(PropertiesSupport.getSubsection(defaultTexts, subFormKey));
	}


	public void fillOut(Properties values, String valueKey) {
		for(Object key : values.keySet()) {
			defaultTexts.put(key + "." + valueKey, values.get(key));
		}

	}

	public void fillOut(String fieldKey, Properties values, String valueKey) {
		System.out.println(defaultTexts);
		for(Object key : values.keySet()) {
			defaultTexts.put(fieldKey + "." + key + "." + valueKey, values.get(key));
		}
		System.out.println(defaultTexts);

	}

	public boolean validate() {
		boolean isValid = true;
		for(String key : PropertiesSupport.getSubsectionKeys(defaultTexts)) {
			boolean isFieldValid = true;

			String value = defaultTexts.getProperty(key + "." + VALUE);
			if(value == null || "".equals(value)) {
				if("true".equals(defaultTexts.getProperty(key + "." + REQUIRED, "false"))) {
					isValid = false;
				}
			} else {
				isValid = PatternMatchingSupport.valueMatchesRegularExpression(defaultTexts.getProperty(key + "." + VALUE), defaultTexts.getProperty(key + "." + REGEX, ""));

			}
//			setProperty(key + "." + VALID, "" + isValid);


		}
		return true;

	}




}
