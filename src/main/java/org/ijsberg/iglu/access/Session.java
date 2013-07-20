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

import org.ijsberg.iglu.configuration.Component;

import java.util.Properties;

/**
 * Contains stateful data that is preserved over a series of requests.
 */
public interface Session {
	/**
	 * For each request, a reference to a user's session must be retrieved.
	 * The session is identified by a token that is stored on the client.
	 *
	 * @return the token that identifies the session
	 */
	String getToken();

	/**
	 * Performs authentication and stores the user if authentication was successful.
	 * Implementations may define their own credential types.
	 *
	 * @param credentials user credentials such as an object containing username and password
	 * @return a successfully authenticated user
	 */
	User login(/*String realmId, */Credentials credentials);

	/**
	 * Removes a user who was previously logged in.
	 */
	void logout();

	/**
	 * @return the currently logged in user
	 */
	User getUser();

	/**
	 * Creates an agent if none exists.
	 *
	 * @param agentId ID to identify an agent within the session
	 * @return an agent identified by the given agentId
	 */
//	Agent getAgent(String agentId);

	/**
	 * Shuts agent down and removes its reference if the agent exists.
	 * The shutdown method of the agent may call this method as well,
	 * so implementations must prevent an endless loop.
	 *
	 * @param agentId ID to identify an agent within the session
	 * @return an agent identified by the given agentId if it exists
	 */
//	Agent destroyAgent(String agentId);

	/**
	 * @return a set of properties defining the user's settings
	 */
	Properties getUserSettings();

	/**
	 * Returns an empty or partially or fully completed form that exists during the course
	 * of a session.
	 *
	 * @param formId form ID
	 * @return a form identified by formId
	 */
//	Form getForm(String formId);

	/**
	 * Stores a form on the session.
	 */
//	void putForm(Object formId, Form form);

	<T> Component getAgent(String id);

}
