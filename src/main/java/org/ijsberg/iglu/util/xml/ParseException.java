/* =======================================================================
 * Copyright (c) 2003-2010 IJsberg Automatisering BV. All rights reserved.
 * Redistribution and use of this code are permitted provided that the
 * conditions of the Iglu License are met.
 * The license can be found in org.ijsberg.iglu.StandardApplication.java
 * and is also published on http://iglu.ijsberg.org/LICENSE.
 * =======================================================================
 */
package org.ijsberg.iglu.util.xml;


/**
 * This exception is thrown if a text can not be parsed (due to bad syntax)
 */
public class ParseException extends Exception
{
	/**
	 *
	 */
	public ParseException()
	{
		super();
	}

	/**
	 * @param message
	 */
	public ParseException(String message)
	{
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ParseException(String message, Throwable cause)
	{
		super(message, cause);
	}
}


















