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

package org.ijsberg.iglu.access;


import java.io.Serializable;

/**
 */
public class SimpleCredentials implements Credentials, Serializable {
	protected String userId;
	protected String password;


	protected SimpleCredentials() {
	}

	public SimpleCredentials(String userId, String password) {
		if (userId == null) {
			throw new IllegalArgumentException("user ID can not be null");
		}
		this.userId = userId;
		this.password = password;
	}

	public String getUserId() {
		return userId;
	}


	/**
	 * @param credentials
	 * @return true if credentials match
	 */
	public boolean equals(Object credentials) {
		if (credentials == null) {
			return false;
		}
		if (credentials instanceof SimpleCredentials) {
			SimpleCredentials simpleCredentials = (SimpleCredentials) credentials;
			return userId.equals(simpleCredentials.userId) &&
					password.equals(simpleCredentials.password);
		}
		return false;
	}


	public int hashCode() {
		int result;
		result = (userId != null ? userId.hashCode() : 0);
		result = 31 * result + (password != null ? password.hashCode() : 0);
		return result;
	}

	public String toString() {
		return userId;
	}

	public String getPassword() {
		return password;
	}
}
