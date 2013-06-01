/* =======================================================================
 * Copyright (c) 2003-2010 IJsberg Automatisering BV. All rights reserved.
 * Redistribution and use of this code are permitted provided that the
 * conditions of the Iglu License are met.
 * The license can be found in org.ijsberg.iglu.StandardApplication.java
 * and is also published on http://iglu.ijsberg.org/LICENSE.
 * =======================================================================
 */
package org.ijsberg.iglu.content.module;

import org.ijsberg.iglu.content.TextProvider;
import org.ijsberg.iglu.util.misc.StringSupport;

import java.util.HashMap;
import java.util.Properties;

/**
 */
public class StandardTextProvider implements TextProvider {

	//language is kept out of the equation

	private HashMap<String, Properties> textsByCategory = new HashMap<String, Properties>();
	protected Properties defaultTexts;
	private String substitutionString = "[*]";
	private String defaultText = null;

	protected StandardTextProvider() {
		this.defaultTexts = new Properties();
	}

	public StandardTextProvider(Properties defaultProperties) {
		this.defaultTexts = defaultProperties;
	}

	public StandardTextProvider addTexts(String category, Properties texts) {
		textsByCategory.put(category, texts);
		return this;
	}

	public StandardTextProvider setSubstitutionString(String substitutionString) {
		this.substitutionString = substitutionString;
		return this;
	}

	public StandardTextProvider setDefaultText(String defaultText) {
		this.defaultText = defaultText;
		return this;
	}


	public String getText(String key) {
		String text = defaultTexts.getProperty(key);
		if(text == null) {
			text = defaultText;
		}
		return text;
	}

	public String getText(String key, Object[] args) {
		String text = getText(key);
		if(text != null) {
			return insertArgs(text, args);
		}
		return null;
	}

	private String insertArgs(String text, Object[] args) {
		StringBuffer textBuffer = new StringBuffer(text);
		for(Object arg : args) {
			StringSupport.replaceFirst(textBuffer, substitutionString, arg.toString());
		}
		return textBuffer.toString();
	}

	public String getText(String key, Object[] args, String defaultText) {
		throw new UnsupportedOperationException("please implement me");
	}

	public String getText(String categoryKey, String key) {
		Properties texts = textsByCategory.get(categoryKey);
		String text = null;
		if(texts != null) {
			text = texts.getProperty(key);
		}
		if(text == null) {
			//get from default section
			text = getText(key);
		}
		return text;
	}

	public String getText(String categoryKey, String key,
                          String defaultText) {
		Properties texts = textsByCategory.get(categoryKey);
		String text = null;
		if(texts != null) {
			text = texts.getProperty(key);
		}
		if(text == null) {
			text = defaultText;
		}
		return text;
	}

	public Properties getTextCategory(String categoryName) {
		return textsByCategory.get(categoryName);
	}

	public String getText(String categoryKey, String key, Object[] args) {
		String text = getText(categoryKey, key);
		if(text != null) {
			return insertArgs(text, args);
		}
		return null;
	}

	public String getText(String categoryKey, String key, Object[] args, String defaultText) {
		String text = getText(categoryKey, key, defaultText);
		if(text != null) {
			return insertArgs(text, args);
		}
		return null;
	}

	//TODO rename
	public Properties getDefaultSection(String categoryKey) {
		return textsByCategory.get(categoryKey);
	}
}
