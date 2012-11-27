package org.ijsberg.iglu.access;

/**
 * Implementations carry credentials by which a user can be identified.
 */
public interface Credentials
{
	/**
	 * @return user ID
	 */
	String getUserId();

	/**
	 * @see Object#equals(Object)
	 *
	 */
	boolean equals(Object credentials);

	/**
	 * @see Object#hashCode()
	 */
	public int hashCode();

}
