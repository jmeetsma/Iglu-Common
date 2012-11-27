package org.ijsberg.iglu.access;

/**
 * Represents a role a user may assume.
 */
public interface Role
{
	/**
	 * @return role ID
	 */
	String getId();

	/**
	 * @return a description meant for administrators
	 */
	String getDescription();
}
