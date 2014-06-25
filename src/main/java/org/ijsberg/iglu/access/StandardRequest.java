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

import org.ijsberg.iglu.configuration.ConfigurationException;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Contains data and references needed in request scope.
 */
public class StandardRequest implements Request//, PropertyListener
{
	//nr of times this request is nested
	private int depth;
	//valid or invalid session token
	private String sessionToken;
	//resolved and connected session
	private Session session;
	//point where request started
	private EntryPoint entryPoint;
	private AccessManager accessManager;
	//user settings (that may affect response)
	private Properties userSettings;
	//attributes during request scope
	private HashMap attributes;
	private String userId;


	/**
	 * Creates request for request manager.
	 */
	public StandardRequest(EntryPoint entryPoint, AccessManager accessManager) {
		this.accessManager = accessManager;
		this.entryPoint = entryPoint;
	}

	/**
	 * @param create create session if true
	 * @return created or resolved session
	 * @throws ConfigurationException      a session is to be created, but this request has no reference to a request manager
	 * @throws LoginExpiredException       if a login name was presented by the client and a session could not be resolved with a presented session token
	 * @throws SessionExpiredException     if a session could not be resolved with a presented session token
	 * @throws SessionUnavailableException if a session is simply unavailable
	 */
	public Session getSession(boolean create) {
		if (create && session == null) {
			if (accessManager == null) {
				throw new ConfigurationException("request " + toString() + " has no reference to a request manager that can create a session");
			}
			session = accessManager.createSession(getUserSettings());
			this.sessionToken = session.getToken();
			if (entryPoint != null) {
				entryPoint.onSessionUpdate(this, session);
			}
		}
		if (session == null) {
			//entry point is responsible for resetting client side session data 
			if (userId != null) {
				throw new LoginExpiredException("login expired");
			}
			if (sessionToken != null) {
				throw new SessionExpiredException("session expired");
			}
			throw new SessionUnavailableException("session unavailable");
		}
		return session;
	}

	/**
	 * Destroys current session.
	 *
	 * @see Request#destroySession()
	 */
	public void destroySession() {
		if (entryPoint != null) {
			entryPoint.onSessionDestruction(this, session);
		}
		if (accessManager != null) {
			accessManager.destroyCurrentSession();
		}
		session = null;
	}


	/**
	 * @return request description
	 */
	public String toString() {
		StringBuffer retval = new StringBuffer("request");
		return retval.toString();
	}

	/**
	 * Stores object during the lifespan of the request.
	 * Use with care.
	 *
	 * @param key
	 * @param value
	 */
	public void setAttribute(Object key, Object value) {
		if (attributes == null) {
			attributes = new HashMap(5);
		}
		attributes.put(key, value);
	}

	/**
	 * Retrieves stored object.
	 *
	 * @param key
	 * @return attribute stored under the key or null
	 */
	public Object getAttribute(Object key) {
		if (attributes == null) {
			return null;
		}
		return attributes.get(key);
	}

	/**
	 * @return a copy of the internal attribute map
	 */
	public Map getAttributeMap() {
		if (attributes == null) {
			return new HashMap();
		}
		return new HashMap(attributes);
	}


	/**
	 * Tries to perform login on entry realm.
	 * Creates session if necessary.
	 *
	 * @param credentials
	 * @return
	 * @throws AuthenticationException if login fails for another reason than simply invalid credentials
	 */
	public User login(Credentials credentials) throws AuthenticationException {
		//if a user wants to log in, we assume he's not surprised if
		//  his session was expired (hence the 'true')
		User user = getSession(true).login(credentials);
		entryPoint.onSessionUpdate(this, session);
		return user;
	}


	/**
	 * Tries to perform a logout off the entry realm.
	 */
	public void logout() {
		if (getUser() != null) {
			getSession(false).logout();
			entryPoint.onSessionUpdate(this, session);
		}
	}

	/**
	 * @return the user, if a user for the current entry realm was logged in
	 */
	public User getUser() {
		if (session != null) {
			return session.getUser();
		}
		return null;
	}

	/**
	 * @return
	 */
	public boolean isSessionAvailable() {
		return session != null;
	}

	/**
	 * A session is resolved by a token which is generated
	 * when a session is created and stored client-side.
	 * If a session is destroyed server-side, a client may
	 * look in vain for a session by the stored token.
	 *
	 * @return true if a session token is present, that could not be resolved to an actual session
	 */
	private boolean isSessionMissing() {
		return sessionToken != null && session == null;
	}

	/**
	 * @return
	 */
	public Properties getUserSettings() {
		if (session != null) {
			return session.getUserSettings();
		}
		if (userSettings == null) {
			userSettings = new Properties();
		}
		return userSettings;
	}


	/**
	 * Exports user settings to entry point which in turn may export to client.
	 */
	public void exportUserSettings() {
		if (entryPoint != null) {
			entryPoint.exportUserSettings(this, getUserSettings());
		}
	}


	/**
	 * Tries to get a reference to the session identified by sessionToken
	 *
	 * @param sessionToken
	 * @param userId
	 */
	public Session resolveSession(String sessionToken, String userId) {
		this.sessionToken = sessionToken;
		this.userId = userId;
		if (accessManager != null) {
			this.session = accessManager.getSessionByToken(sessionToken);
		}
		return session;
	}

	@Override
	public int getTimesEntered() {
		return depth;
	}

	public void increaseTimesEntered() {
		depth++;
	}

	public void decreaseTimesEntered() {
		depth--;
	}

}
