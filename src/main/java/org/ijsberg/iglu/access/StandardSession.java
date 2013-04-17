/* =======================================================================
 * Copyright (c) 2003-2010 IJsberg Automatisering BV. All rights reserved.
 * Redistribution and use of this code are permitted provided that the
 * conditions of the Iglu License are met.
 * The license can be found in org.ijsberg.iglu.StandardApplication.java
 * and is also published on http://iglu.ijsberg.org/LICENSE.
 * =======================================================================
 */
package org.ijsberg.iglu.access;

import org.ijsberg.iglu.configuration.Component;
import org.ijsberg.iglu.exception.ResourceException;
import org.ijsberg.iglu.util.io.ReceiverQueue;
import org.ijsberg.iglu.util.misc.KeyGenerator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.TreeMap;

/**
 * A sessiom contains stateful data and access in the form of agents.
 * <p/>
 * A session may contain receivers and transceivers which can be used to
 * communicate messages (asynchronously), such as dedicated log messages.
 *
 * @see AccessManager
 */
public final class StandardSession implements Serializable, Session//, PropertyListener
{
	private String token;
	private Date creationTime = new Date();
	//http-session style attributes
	private final TreeMap attributes = new TreeMap();
	//default settings, possibly merged with settings
	//whenever a session is created, the StandardRequestManager adds its default settings to the created session
	private Properties userSettings;
	//collection of session objects for each realm
	private HashMap<String, Component> agentComponents = new HashMap();
	//tme last accessed, needed for timeout determination
	private long lastAccessedTime = System.currentTimeMillis();
	//expiration timeout in milliseconds
	private long expirationTimeout;//does not expire by default
	//it's possible that a session is resolved via different entry Realms
	//so a logged in user is stored by a realm id
//	private HashMap usersByRealmId = new HashMap();
	private User user;

//	private Application application;

	private HashMap forms;

	private AccessManager accessManager;

	/**
	 * Constructs a normal user session.
	 *
	 * @param expirationTimeout expiration timeout in seconds, 0 or less means that session will not expire
	 */
	public StandardSession(AccessManager accessManager, long expirationTimeout, Properties defaultUserSettings)
	{
		//create an id that's unique and difficult to guess
		token = KeyGenerator.generateKey();
//		this.application = application;
		//store as millis
		this.expirationTimeout = expirationTimeout * 1000;
		this.accessManager = accessManager;
		//copy settings
//		userSettings = new GenericPropertyBundle("user settings");
//		userSettings.merge(defaultUserSettings);
//		userSettings.setListener(this);
		userSettings = defaultUserSettings;
	}

	/**
	 * @return session token used to identify a session over a number of requests
	 */
	public String getToken()
	{
		return token;
	}


	/**
	 * @return
	 */
	public Date getCreationTime()
	{
		return creationTime;
	}


	/**
	 * Receivers are used to receive (a stream of) messages.
	 * Receivers are stored as regular attributes on the session.
	 *
	 * @return a list of receivers by filtering this session's attributes
	 * @see ReceiverQueue
	 */
	public List getReceivers()
	{
		ArrayList result = new ArrayList();
		synchronized (attributes)
		{
			Iterator i = attributes.values().iterator();
			while (i.hasNext())
			{
				Object o = i.next();
				if (o instanceof ReceiverQueue)
				{
					result.add(o);
				}
			}
		}
		return result;
	}


	/**
	 * @return
	 */
	public Properties getUserSettings()
	{
		return userSettings;
	}

	/**
	 * Merges settings with the existing settings.
	 *
	 */
/*	public void addSettings(GenericPropertyBundle settings)
	{
		this.userSettings.merge(settings);
	}*/


	@Override
	public <T> Component getAgent(String id) {

		if(agentComponents.containsKey(id)) {
			return agentComponents.get(id);
		}
		return createAgent(id);
	}

	private <T> Component createAgent(String id) {
		Component agent = accessManager.createAgent(id);
		agentComponents.put(id, agent);
		return agent;
	}



	/**
	 * @return a user logged in to the current realm
	 */
	public User getUser()
	{
		return user;
	}

	/**
	 * Authenticates a user for a certain realm based on credentials.
	 *
	 * @param credentials
	 * @return transient user if authentication succeeded or null if it didn't
	 * @throws SecurityException if user already logged in
	 * @throws AuthenticationException if some user action is required
	 */
	public User login(Credentials credentials) throws SecurityException
	{
		//check if user is logged in already
		User loggedInUser = getUser();
		if (loggedInUser != null)
		{
			logout();
			//throw new SecurityException("user already logged in as '" + loggedInUser.getId() + "'");
		}

		User user = accessManager.authenticate(credentials);
		if(user != null)
		{
			this.user = user;
			userSettings = user.getSettings();
		}

		return user;
	}


	/**
	 * Performs destruction of agents and closes message receivers.
	 */
	public void onDestruction()
	{
		//Iterator i = new ArrayList(agents.values()).iterator();

		for (Component component : agentComponents.values()) {
			accessManager.removeAgent(component);
		}
		//close receivers
	/*	Iterator receiverIterator = getReceivers().iterator();
		while (receiverIterator.hasNext())
		{
			ReceiverQueue r = (ReceiverQueue) receiverIterator.next();
			r.close();
		}*/
		attributes.clear();
	}

	/**
	 * Performs logout of user in the current realm.
	 *
	 */
	public void logout()
	{
		user = null;
		//Object removed = usersByRealmId.remove(realmId);
	}

	/**
	 * Renews the last-accessed-time and prolongs the life of this session.
	 */
	public void updateLastAccessedTime()
	{
		lastAccessedTime = System.currentTimeMillis();
	}

	/**
	 * @return true if this session should be considered expired based on the last-accessed-time and timeout value
	 */
	public boolean isExpired()
	{
		return lastAccessedTime > 0 && lastAccessedTime + expirationTimeout < System.currentTimeMillis();
	}


	/**
	 * @return session description
	 */
	public String toString()
	{
		StringBuffer result = new StringBuffer(creationTime.toString());
	/*	result.append(' ' + token);
		Iterator i = agents.values().iterator();
		while (i.hasNext())
		{
			Object so = i.next();
			result.append(' ' + so.getClass().getName() + ':' + so);
		}*/
		return result.toString();
	}



	/**
	 * Returns an existing and possibly filled out form obtained from a session
	 * or internal attributes. If no form exists, a new form is created and
	 * stored on the session.
	 *
	 * @param formId
	 * @return
	 */
	/*public Form getForm(String formId)
	{
		Form retval = null;
		if (forms != null)
		{
			retval = (Form) forms.get(formId);
		}
		if (retval == null)
		{
			retval = new StandardForm(formId, StandardPersonalizedContent.getPersonalizedContent(getUserSettings(), application));
		}
		putForm(formId, retval);

		return retval;
	}*/

	/**
	 * Stores a form on the session.
	 *
	 * @param formId
	 * @param form
	 */
/*	public void putForm(Object formId, Form form)
	{
		if(forms == null)
		{
			forms = new HashMap();
		}
		forms.put(formId, form);
	}*/

}
