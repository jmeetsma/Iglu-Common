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
 * Service that acts as authenticator for realms.
 *

 */
public interface Authenticator
{
	/**
	 * Authenticates a user.
	 *
	 * @param credentials
	 * @return user if authentication is successful, if authentication fails,
	 * an exception must be thrown rather than returning null
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
