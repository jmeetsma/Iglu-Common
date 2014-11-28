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

import java.util.*;

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
public class BasicUser implements User {
	//a direct reference to settings in account
	private Properties settings;

	private String userId;
	private HashMap roles = new HashMap();
	private UserGroup group;

	/**
	 * @param userId
	 * @param roles
	 * @param settings
	 */
	public BasicUser(String userId, List<Role> roles, UserGroup group, Properties settings) {
		this.userId = userId;
		this.settings = settings;

		Iterator i = roles.iterator();
		while (i.hasNext()) {
			Object o = i.next();
			Role role;
			String roleId;
			role = (Role) o;
			roleId = role.getId();
			this.roles.put(roleId, role);
		}
		this.group = group;
	}

	public BasicUser(String userId, Properties settings) {
		this.userId = userId;
		this.settings = settings;
	}

	/**
	 * @param userId
	 */
	public BasicUser(String userId) {
		this.userId = userId;
	}

	/**
	 * @return
	 */
	public final String getId() {
		return userId;
	}


	/**
	 * @return the user id
	 */
	public String toString() {
		return userId;
	}


	/**
	 * @return
	 */
	public Properties getSettings() {
		return settings;
	}

	/**
	 * @param roleId id of the rule a user must have,
	 *               a '*' makes the method return 'true',
	 *               which can be used to check wether a user is at least authenticated
	 * @return true if a user has, or fullfills the role
	 * @see User#hasRole(String)
	 */
	public boolean hasRole(String roleId) {
		if ("*".equals(roleId)) {
			return true;
		}
		return roles.keySet().contains(roleId);
	}

	/**
	 * @param roleId
	 * @return
	 * @see User#getRoles()
	 */
	public Role getRole(String roleId) {
		return (Role) roles.get(roleId);
	}

	/**
	 * @return
	 * @see User#getRoles()
	 */
	public Collection getRoles() {
		return roles.values();
	}

	/**
	 * @return false
	 */
	public boolean isAccountBlocked() {
		return false;
	}

	@Override
	public UserGroup getGroup() {
		return group;
	}

	public void setGroup(UserGroup group) {
		this.group = group;
	}

}
