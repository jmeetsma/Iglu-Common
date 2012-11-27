package org.ijsberg.iglu.access;


import java.io.Serializable;

/**
 */
public class SimpleCredentials implements Credentials, Serializable
{
	protected String userId;
	protected String password;


	protected SimpleCredentials()
	{
	}

	public SimpleCredentials(String userId, String password)
	{
		if (userId == null)
		{
			throw new IllegalArgumentException("user ID can not be null");
		}
		this.userId = userId;
		this.password = password;
	}

	public String getUserId()
	{
		return userId;
	}


	/**
	 * @param credentials
	 * @return true if credentials match
	 */
	public boolean equals(Object credentials)
	{
		if(credentials == null)
		{
			return false;
		}
		if (credentials instanceof SimpleCredentials)
		{
			SimpleCredentials simpleCredentials = (SimpleCredentials) credentials;
			return userId.equals(simpleCredentials.userId) &&
					password.equals(simpleCredentials.password);
		}
		return false;
	}


	public int hashCode()
	{
		int result;
		result = (userId != null ? userId.hashCode() : 0);
		result = 31 * result + (password != null ? password.hashCode() : 0);
		return result;
	}

	public String toString()
	{
		return userId;
	}
}
