/* =======================================================================
 * Copyright (c) 2003-2010 IJsberg Automatisering BV. All rights reserved.
 * Redistribution and use of this code are permitted provided that the
 * conditions of the Iglu License are met.
 * The license can be found in org.ijsberg.iglu.StandardApplication.java
 * and is also published on http://iglu.ijsberg.org/LICENSE.
 * =======================================================================
 */
package org.ijsberg.iglu.access;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * Is the transient counterpart of an account.
 * It makes the account settings available.
 * A user object can be obtained
 * by logging on to a session with proper credentials.
 * <p/>
 * When a user logs in to a realm, this object is created and stored
 * in a session.
 *

 * @see StandardSession
 */
public class BasicUser implements User
{
	//a direct reference to settings in account
	private Properties settings;

	private String userId;
	private HashMap roles = new HashMap();

	/**
	 * @param userId
	 * @param roles
	 * @param settings
	 */
	public BasicUser(String userId, List<Role> roles, Properties settings)
	{
		this.userId = userId;
		this.settings = settings;

		Iterator i = roles.iterator();
		while (i.hasNext())
		{
			Object o = i.next();
			Role role;
			String roleId;
			role = (Role) o;
			roleId = role.getId();
			this.roles.put(roleId, role);
		}
	}

	public BasicUser(String userId, Properties settings)
	{
		this.userId = userId;
		this.settings = settings;
	}

	/**
	 * @param userId
	 */
	public BasicUser(String userId)
	{
		this.userId = userId;
	}

	/**
	 * @return
	 */
	public final String getId()
	{
		return userId;
	}


	/**
	 * @return the user id
	 */
	public String toString()
	{
		return userId;
	}


	/**
	 * @return
	 */
	public Properties getSettings()
	{
		return settings;
	}

	/**
	 * @param roleId id of the rule a user must have,
	 * a '*' makes the method return 'true',
	 * which can be used to check wether a user is at least authenticated
	 * @return true if a user has, or fullfills the role
	 * @see User#hasRole(String)
	 */
	public boolean hasRole(String roleId)
	{
		if("*".equals(roleId))
		{
			return true;
		}
		return roles.keySet().contains(roleId);
	}

	/**
	 * @param roleId
	 * @return
	 * @see User#getRoles()
	 */
	public Role getRole(String roleId)
	{
		return (Role) roles.get(roleId);
	}

	/**
	 * @return
	 * @see User#getRoles()
	 */
	public Collection getRoles()
	{
		return roles.values();
	}

	/**
	 *
	 * @return false
	 */
	public boolean isAccountBlocked()
	{
		return false;
	}
}
