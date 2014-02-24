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

import java.util.Collection;
import java.util.Properties;

/**
 * Stands for an (authenticated) application user.
 * Is the transient representation of a user account.
 */
public interface User {
	/**
	 * @return user id
	 */
	String getId();

	/**
	 * Returns properties used for personalization
	 * such as language.
	 *
	 * @return user settings or preferences
	 */
	Properties getSettings();

	/**
	 * @param roleId role ID
	 * @return true if a user has, or fulfills the role
	 */
	boolean hasRole(String roleId);

	/**
	 * @param roleId role ID
	 * @return a role by the given ID
	 */
	Role getRole(String roleId);

	/**
	 * @return all roles fulfilled by the user
	 */
	Collection<Role> getRoles();

	/**
	 * @return true if the user account is marked blocked
	 */
	boolean isAccountBlocked();

	UserGroup getGroup();
}
