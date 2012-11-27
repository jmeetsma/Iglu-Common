/* =======================================================================
 * Copyright (c) 2003-2010 IJsberg Automatisering BV. All rights reserved.
 * Redistribution and use of this code are permitted provided that the
 * conditions of the Iglu License are met.
 * The license can be found in org.ijsberg.iglu.StandardApplication.java
 * and is also published on http://iglu.ijsberg.org/LICENSE.
 * =======================================================================
 */
package org.ijsberg.iglu.access;


/**
 */
public class SessionExpiredException extends SessionUnavailableException
{
	/**
	 *
	 */
	public SessionExpiredException()
	{
		super();
	}

	/**
	 * @param message
	 */
	public SessionExpiredException(String message)
	{
		super(message);
	}
}
