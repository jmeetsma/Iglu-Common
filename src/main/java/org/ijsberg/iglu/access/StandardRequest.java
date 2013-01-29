/* =======================================================================
 * Copyright (c) 2003-2010 IJsberg Automatisering BV. All rights reserved.
 * Redistribution and use of this code are permitted provided that the
 * conditions of the Iglu License are met.
 * The license can be found in org.ijsberg.iglu.StandardApplication.java
 * and is also published on http://iglu.ijsberg.org/LICENSE.
 * =======================================================================
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
	//realm of user performing the request
	//private Realm realm;
	//description of internal process in case this is an internal request
	//private String internalProcessDescription;
	//list of requests that are known to be internal requests
	//private HashMap internalRequests;
	//temporary request which is used when
	//1)system tasks must be performed
	//it must be set by a part of the application that got it earlier
	//and uses is it (as sticky bit) to make a user request act as an internal request
	//2)3rd party code must be invoked with less access rights
	//if a request is downgraded to an admin or user request,
	//the application's getRequest method must return the represented request instead of
	//the internal request thus:
	//- providing the subsystem with an actual request that may refer to a realm, session and user
	//- not providing the subsystem with an internal request that can later be used to perform core tasks
	//private Request representedUserRequest;
	//nesting count of request representation
	//private int timesRepresentingInternalRequest;

	//reference to request manager (which created this request)
	private AccessManager accessManager;
	//request stack
	//private ArrayList componentStack = new ArrayList(3);
	//user settings (that may affect response)
	private Properties userSettings;
	//source of personalizable content
//	private PersonalizedContent personalizedContent;
	//indication if request done by administrator
	//private boolean isAdminRequest;
	//id of current thread
//	private int[] threadId = new int[1];
	//reference to application
//	private Application application;
	//attributes during request scope
	private HashMap attributes;
	//forms are stored in a different map to avoid mix-ups with attributes
//	private HashMap forms;
	private String userId;

	/**
	 * Creates request for startup phase.
	 *
	 * @param application
	 * @param internalProcessId
	 */
/*	StandardRequest(Application application, String internalProcessId)
	{
		this.application = application;
		this.internalProcessDescription = internalProcessId;
		this.internalRequests = new HashMap();
		this.threadId[0] = Thread.currentThread().hashCode();
		//make this request trust itself
		internalRequests.put(this, internalProcessId);
	}
*/
	/**
	 * Creates request in startup phase as soon as realms have been created.
	 *
	 * @param realm
	 * @param internalProcessDescription
	 * @param threadId
	 */
	/*public StandardRequest(int threadId, Realm realm, String internalProcessDescription, HashMap internalRequests)
	{
		if (realm == null)
		{
			throw new SecurityException("cannot instantiate request without entryrealm");
		}
		this.threadId[0] = threadId;
		this.realm = realm;
		this.internalProcessDescription = internalProcessDescription;
		this.internalRequests = internalRequests;
		this.application = realm.getApplication();
		//make this request trust itself
		internalRequests.put(this, internalProcessDescription);
	}*/

	/**
	 * Creates request for request manager.
	 *
	 */
	public StandardRequest(/*int threadId, */EntryPoint entryPoint, AccessManager accessManager/*, boolean isAdminRequest, HashMap internalRequests*/)
	{
	/*	if (entryPoint == null)
		{
			throw new SecurityException("cannot instantiate request without entrypoint");
		}
		this.threadId[0] = threadId;
		this.internalRequests = internalRequests;
		this.realm = entryPoint.getRealm();
		this.application = realm.getApplication();
		this.isAdminRequest = isAdminRequest;
//		this.sessionToken = sessionToken;*/
		this.accessManager = accessManager;
		this.entryPoint = entryPoint;
		//store entrylayer as first in stack
	/*	componentStack.add(0, realm.getEntryLayer());*/
	}

	/**
	 * @return
	 */
/*	public Application getApplication()
	{
		return application;
	}*/

	/**
	 * @param create create session if true
	 * @return created or resolved session
	 * @throws ConfigurationException a session is to be created, but this request has no reference to a request manager
	 * @throws LoginExpiredException if a login name was presented by the client and a session could not be resolved with a presented session token
	 * @throws SessionExpiredException if a session could not be resolved with a presented session token
	 * @throws SessionUnavailableException if a session is simply unavailable
	 */
	public Session getSession(boolean create)
	{
		if (create && session == null)
		{
			if(accessManager == null)
			{
				throw new ConfigurationException("request " + toString() + " has no reference to a request manager that can create a session");
			}
			session = accessManager.createSession(getUserSettings());
			this.sessionToken = session.getToken();
			if (entryPoint != null)
			{
				entryPoint.onSessionUpdate(this, session);
			}
		}
		if (session == null)
		{
			//entry point is responsible for resetting client side session data 
			if (userId != null)
			{
				throw new LoginExpiredException("login expired");
			}
			if (sessionToken != null)
			{
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
	public void destroySession()
	{
		if (entryPoint != null)
		{
			entryPoint.onSessionDestruction(this, session);
		}
		if (accessManager != null)
		{
			accessManager.destroyCurrentSession();
		}
		session = null;
	}


	/**
	 * @return request description
	 */
	public String toString()
	{
		StringBuffer retval = new StringBuffer("request");
/*		if (internalProcessDescription != null)
		{
			retval.append(' ' + internalProcessDescription);
			if (representedUserRequest != null)
			{
				retval.append(" acting as '" + representedUserRequest.toString() + '\'');
			}
		}
		else
		{
			retval.append(" user request " + (session != null ? '(' + session.toString() + ") " : ""));
			if (this.timesRepresentingInternalRequest > 0)
			{
				retval.append(" acting as internal request");
			}
		}
//		retval.append(" bound to thread(s) " + CollectionSupport.format(threadId, ", "));*/

		return retval.toString();
	}

	//TODO reconsider use of attributes, since it may lead to less comprehensive code 
	/**
	 * Stores object during the lifespan of the request.
	 *
	 * @param key
	 * @param value
	 */
	public void setAttribute(Object key, Object value)
	{
		if (attributes == null)
		{
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
	public Object getAttribute(Object key)
	{
		if (attributes == null)
		{
			return null;
		}
		return attributes.get(key);
	}

	/**
	 *
	 * @return a copy of the internal attribute map
	 */
	public Map getAttributeMap()
	{
		if(attributes == null)
		{
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
	public User login(Credentials credentials) throws AuthenticationException
	{
		//if a user wants to log in, we assume he's not surprised if
		//  his session was expired (hence the 'true')
		User user = getSession(true).login(credentials);
		entryPoint.onSessionUpdate(this, session);
		return user;
	}


	/**
	 * Tries to perform a logout off the entry realm.
	 */
	public void logout()
	{
		if (getUser() != null)
		{
			getSession(false).logout();
			entryPoint.onSessionUpdate(this, session);
		}
	}

	/**
	 * @return the user, if a user for the current entry realm was logged in
	 */
	public User getUser()
	{
		if (session != null)
		{
			return session.getUser();
		}
		return null;
	}

	/**
	 *
	 * @return
	 */
	public boolean isSessionAvailable()
	{
		return session != null;
	}

	/**
	 * A session is resolved by a token which is generated
	 * when a session is created and stored client-side.
	 * If a session is destroyed server-side, a client may
	 * look in vain for a session by the stored token.
	 * @return true if a session token is present, that could not be resolved to an actual session
	 */
	private boolean isSessionMissing()
	{
		return sessionToken != null && session == null;
	}

	/**
	 * @return
	 */
	public Properties getUserSettings()
	{
		if (session != null)
		{
			return session.getUserSettings();
		}
		if (userSettings == null)
		{
			userSettings = new Properties();
			
			//create clone of default settings for this realm
/*			GenericPropertyBundle requestScopeUserSettings = new GenericPropertyBundle(realm.getDefaultUserSettings());
			if (entryPoint != null)
			{
				entryPoint.importUserSettings(this, requestScopeUserSettings);
			}
			requestScopeUserSettings.setListener(this);
			userSettings = requestScopeUserSettings;*/
		}
		return userSettings;
	}


	/**
	 * Exports user settings to entry point which in turn may export to client.
	 */
	public void exportUserSettings()
	{
		if (entryPoint != null)
		{
			entryPoint.exportUserSettings(this, getUserSettings());
		}
	}




	/**
	 * Returns an existing and possibly filled out form obtained from a session
	 * or internal attributes. If no form exists, a new form is created and
	 * stored on the session or request.
	 *
	 * @param formId
	 * @return
	 */
/*	public Form getForm(String formId)
	{
		Form retval = null;
		if (session != null)
		{
			retval = session.getForm(formId);
		}
		if (retval == null && forms != null)
		{
			retval = (Form) forms.get(formId);
		}
		if (retval == null)
		{
			retval = new StandardForm(formId, StandardPersonalizedContent.getPersonalizedContent(getUserSettings(), application));
		}
		if (session != null)
		{
			session.putForm(formId, retval);
		}
		else
		{
			if(forms == null)
			{
				forms = new HashMap();
			}
			forms.put(formId, retval);
		}
		return retval;
	}
*/
	/**
	 * Stores a form on the request or the session if it exists.
	 *
	 * @param formId
	 * @param form
	 */
/*	public void putForm(Object formId, Form form)
	{
		if (session != null)
		{
			session.putForm(formId, form);
		}
		else
		{
			if(forms == null)
			{
				forms = new HashMap();
			}
			forms.put(formId, form);
		}
	}
*/
	/**
	 * @return the request represented in case this request is a downgraded internal request
	 */
/*	public Request getRepresentedRequest()
	{
		return representedUserRequest;
	}*/

	/**
	 *
	 * @param currentThreadId
	 * @return
	 */
/*	private boolean isBoundToThread(int currentThreadId)
	{
		if(threadId[0] == currentThreadId)
		{
			return true;
		}
		for(int i = 1; i < threadId.length; i++)
		{
			if(threadId[i] == currentThreadId)
			{
				return true;
			}
		}
		return false;
	}
*/
	/**
	 *
	 * @return
	 */
/*	public boolean isBoundToCurrentThread()
	{
		return isBoundToThread(Thread.currentThread().hashCode());
	}*/

	/**
	 * Tries to get a reference to the session identified by sessionToken
	 *
	 * @param sessionToken
	 * @param userId
	 */
	public Session resolveSession(String sessionToken, String userId)
	{
		this.sessionToken = sessionToken;
		this.userId = userId;
		if(accessManager != null)
		{
			this.session = accessManager.getSessionByToken(sessionToken);
		}
		return session;
	}

	@Override
	public int getTimesEntered() {
		// TODO Auto-generated method stub
		return depth;
	}
	
	public void increaseTimesEntered(/*Request internalRequest*/)
	{
//		if (internalRequests.containsKey(internalRequest))
		{
			depth++;
		}
	}
	public void decreaseTimesEntered(/*Request internalRequest*/)
	{
//		if (internalRequests.containsKey(internalRequest))
		{
			depth--;
		}
	}


}
