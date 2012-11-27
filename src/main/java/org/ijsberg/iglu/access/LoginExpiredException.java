package org.ijsberg.iglu.access;

/**
 */
public class LoginExpiredException extends SessionExpiredException
{
	/**
	 *
	 */
	public LoginExpiredException()
	{
		super();
	}

	/**
	 * @param message
	 */
	public LoginExpiredException(String message)
	{
		super(message);
	}
}
