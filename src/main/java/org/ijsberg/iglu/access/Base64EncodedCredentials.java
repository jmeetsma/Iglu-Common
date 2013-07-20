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
package org.ijsberg.iglu.access;

import org.ijsberg.iglu.util.misc.EncodingSupport;


/**
 * Wraps base 64 encoded encodedCredentials.
 * Base 64 encoded encodedCredentials are passed by a browser
 * when BASIC authentication is performed.
 */
public class Base64EncodedCredentials extends SimpleCredentials {
	private String encodedCredentials;

	/**
	 * @param encodedCredentials
	 */
	public Base64EncodedCredentials(String encodedCredentials) {
		if (encodedCredentials == null || "".equals(encodedCredentials)) {
			throw new IllegalArgumentException("credentials can not be empty");
		}
		this.encodedCredentials = encodedCredentials;
		String decodedCredentials = new String(EncodingSupport.decodeBase64(encodedCredentials));
		int colonIndex = decodedCredentials.indexOf(':');
		if (colonIndex == -1) {
			throw new IllegalArgumentException("wrong format of encoded credentials: colon separator not found");
		}
		userId = decodedCredentials.substring(0, colonIndex);
		password = decodedCredentials.substring(colonIndex + 1, decodedCredentials.length());
	}

	/**
	 * @param userId
	 * @param password
	 */
	public Base64EncodedCredentials(String userId, String password) {
		super(userId, password);
		this.encodedCredentials = EncodingSupport.encodeBase64((userId + ':' + password).getBytes(), 0);
	}

	/**
	 * @return
	 */
	public String getEncodedCredentials() {
		return encodedCredentials;
	}


	/**
	 * @return encoded credentials and the user id
	 */
	public String toString() {
		return encodedCredentials + ' ' + userId;
	}


}
