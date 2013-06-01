package org.ijsberg.iglu.content;

import java.util.Properties;

/**
 * Provides personalized content that is to be used in user interfaces.
 * <p/>
 * Content may be divided into multiple categories, such as
 * personalized texts, layout, images etc.
 * <p/>
 * Initially one category with one set of texts in one language is enough.
 * These texts should be stored as section in the configuration.
 * If domain-specific texts are retrieved via a content source, it's
 * easy to extend the use of an application to different user groups,
 * without having to change any code.
 *
 */
public interface TextProvider {
	/**
	 * Provides personalized texts (from the default content category).
	 * Note: if nothing is found, the key itself is returned.
	 * Use TODO if you want to test if a text exists.
	 *
	 * @param key message key
	 * @return
	 */
	String getText(String key);


	/**
	 * Provides personalized messages (from the default content category).
	 * Arguments, such as strings, dates or numbers, may be passed
	 * to fill in gaps in the message.
	 *
	 * @param key
	 * @param args
	 * @return
	 */
	String getText(String key, Object[] args);

	/**
	 * Provides personalized messages (from the default content category).
	 * Arguments, such as strings, dates or numbers, may be passed
	 * to fill in the gaps in the message.
	 * How these gaps are specified is up to the implementation.
	 *
	 * @param key
	 * @param args
	 * @param defaultText
	 * @return
	 */
	String getText(String key, Object[] args, String defaultText);


	/**
	 * Provides personalized texts from a specific content category.
	 *
	 * @param categoryKey
	 * @param key message key
	 * @return
	 */
	String getText(String categoryKey, String key);

	String getText(String categoryKey, String key, String defaultText);

	Properties getTextCategory(String categoryName);

	/**
	 * Provides personalized messages from a specific content category.
	 * Arguments, such as strings, dates or numbers, may be passed
	 * to fill in gaps in the message.
	 * How these gaps are specified is up to the implementation.
	 *
	 * @param categoryKey
	 * @param key
	 * @param args
	 * @return
	 */
	String getText(String categoryKey, String key, Object[] args);

	/**
	 * Provides personalized messages from a specific content category.
	 * Arguments, such as strings, dates or numbers, may be passed
	 * to fill in gaps in the message.
	 * How these gaps are specified is up to the implementation.
	 *
	 * @param categoryKey
	 * @param key
	 * @param args
	 * @param defaultText
	 * @return
	 */
	String getText(String categoryKey, String key, Object[] args, String defaultText);


	/**
	 * @param resourceCategoryName
	 * @return the configuration section that acts as the default source of content for the specified category
	 */
	Properties getDefaultSection(String resourceCategoryName);
}
