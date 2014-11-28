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
	protected String defaultText = null;

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


	public boolean containsText(String key) {
		String text = defaultTexts.getProperty(key);
		return text != null;
	}

	public String getText(String key) {
		String text = defaultTexts.getProperty(key);
		if (text == null) {
			text = defaultText;
		}
		return text;
	}

	public String getText(String key, Object[] args) {
		String text = getText(key);
		if (text != null) {
			return insertArgs(text, args);
		}
		return null;
	}

	private String insertArgs(String text, Object[] args) {
		StringBuffer textBuffer = new StringBuffer(text);
		for (Object arg : args) {
			StringSupport.replaceFirst(textBuffer, substitutionString, arg.toString());
		}
		return textBuffer.toString();
	}

	public String getText(String key, Object[] args, String defaultText) {
		throw new UnsupportedOperationException("please implement me");
	}

	public boolean containsText(String categoryKey, String key) {
		String text = defaultTexts.getProperty(key);
		Properties texts = textsByCategory.get(categoryKey);
		if (texts != null) {
			return texts.getProperty(key) != null;
		}
		return false;
	}

	public String getText(String categoryKey, String key) {
		Properties texts = textsByCategory.get(categoryKey);
		String text = null;
		if (texts != null) {
			text = texts.getProperty(key);
		}
		if (text == null) {
			//get from default section
			text = getText(key);
		}
		return text;
	}

	public String getText(String categoryKey, String key,
						  String defaultText) {
		Properties texts = textsByCategory.get(categoryKey);
		String text = null;
		if (texts != null) {
			text = texts.getProperty(key);
		}
		if (text == null) {
			text = defaultText;
		}
		return text;
	}

	public Properties getTextCategory(String categoryName) {
		return textsByCategory.get(categoryName);
	}

	public String getText(String categoryKey, String key, Object[] args) {
		String text = getText(categoryKey, key);
		if (text != null) {
			return insertArgs(text, args);
		}
		return null;
	}

	public String getText(String categoryKey, String key, Object[] args, String defaultText) {
		String text = getText(categoryKey, key, defaultText);
		if (text != null) {
			return insertArgs(text, args);
		}
		return null;
	}

	public Properties getDefaultSection(String categoryKey) {
		return textsByCategory.get(categoryKey);
	}
}
