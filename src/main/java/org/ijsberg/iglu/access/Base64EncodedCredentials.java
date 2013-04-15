/* =======================================================================
 * Copyright (c) 2003-2010 IJsberg Automatisering BV. All rights reserved.
 * Redistribution and use of this code are permitted provided that the
 * conditions of the Iglu License are met.
 * The license can be found in org.ijsberg.iglu.StandardApplication.java
 * and is also published on http://iglu.ijsberg.org/LICENSE.
 * =======================================================================
 */
package org.ijsberg.iglu.access;

import org.ijsberg.iglu.util.misc.EncodingSupport;


/**
 * Wraps base 64 encoded encodedCredentials.
 * Base 64 encoded encodedCredentials are passed by a browser
 * when BASIC authentication is performed.
 *
 */
public class Base64EncodedCredentials extends SimpleCredentials
{
	private String encodedCredentials;

	/**
	 * @param encodedCredentials
	 */
	public Base64EncodedCredentials(String encodedCredentials)
	{
		if (encodedCredentials == null || "".equals(encodedCredentials))
		{
			throw new IllegalArgumentException("credentials can not be empty");
		}
		this.encodedCredentials = encodedCredentials;
		String decodedCredentials = new String(EncodingSupport.decodeBase64(encodedCredentials));
		int colonIndex = decodedCredentials.indexOf(':');
		if (colonIndex == -1)
		{
			throw new IllegalArgumentException("wrong format of encoded credentials: colon separator not found");
		}
		userId = decodedCredentials.substring(0, colonIndex);
		password = decodedCredentials.substring(colonIndex + 1, decodedCredentials.length());
	}

	/**
	 * @param userId
	 * @param password
	 */
	public Base64EncodedCredentials(String userId, String password)
	{
		super(userId, password);
		this.encodedCredentials = EncodingSupport.encodeBase64((userId + ':' + password).getBytes(), 0);
	}

	/**
	 * @return
	 */
	public String getEncodedCredentials()
	{
		return encodedCredentials;
	}


	/**
	 * @return encoded credentials and the user id
	 */
	public String toString()
	{
		return encodedCredentials + ' ' + userId;
	}



}
