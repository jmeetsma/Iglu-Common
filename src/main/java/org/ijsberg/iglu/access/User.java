package org.ijsberg.iglu.access;

import java.util.Collection;
import java.util.Properties;

/**
 * Stands for an (authenticated) application user.
 * Is the transient representation of a user account.
 */
public interface User
{
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
	 *
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
	 *
	 * @return true if the user account is marked blocked 
	 */
	boolean isAccountBlocked();
}
