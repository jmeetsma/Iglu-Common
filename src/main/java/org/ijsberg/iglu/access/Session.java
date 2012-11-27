package org.ijsberg.iglu.access;

import java.util.Properties;

/**
 * Contains stateful data that is preserved over a series of requests.
 */
public interface Session
{
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
	 * @param realmId ID of the realm the user belongs to
	 * @param credentials user credentials such as an object containing username and password
	 * @return a successfully authenticated user
	 */
	User login(/*String realmId, */Credentials credentials);

	/**
	 * Removes a user who was previously logged in.
	 *
	 * @param realmId id of the realm the user belongs to
	 */
	void logout(/*String realmId*/);

	/**
	 * @param realmId ID of the realm the user belongs to
	 * @return the currently logged in user
	 */
	User getUser(/*String realmId*/);

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
	 *
	 * @param formId form ID
	 * @param form form to be completed
	 */
//	void putForm(Object formId, Form form);

}
