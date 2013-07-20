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


/**
 * Service that acts as authenticator for realms.
 */
public interface Authenticator {
	/**
	 * Authenticates a user.
	 *
	 * @param credentials
	 * @return user if authentication is successful, if authentication fails,
	 *         an exception must be thrown rather than returning null
	 * @throws AuthenticationException if authentication fails for any reason
	 */
	User authenticate(Credentials credentials) throws AuthenticationException;

	/**
	 * To be invoked if a user can't login because his credentials are expired.
	 *
	 * @param expiredCredentials
	 * @param newCredentials
	 * @return user if authentication is successful
	 * @throws AuthenticationException
	 */
	User authenticate(Credentials expiredCredentials, Credentials newCredentials) throws AuthenticationException;

}
