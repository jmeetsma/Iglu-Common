package org.ijsberg.iglu.access;

import org.ijsberg.iglu.StateException;

/**
 * This exception should be a checked exception, because it represents an event that
 * may occur under normal circumstances, and must be dealt with.
 * It is however a runtime exception because it only must be dealt with
 * at the presentation level of an application.
 *
 * Authentication may have failed if credentials are invalid, incompatible or expired,
 * or if a user account is expired or if the user was already authenticated.
 */
public class AuthenticationException extends StateException
{
	public final static int CREDENTIALS_INVALID = 0;
	public final static int CREDENTIALS_INCOMPATIBLE = 1;
	public final static int CREDENTIALS_EXPIRED = 2;
	public final static int ACCOUNT_EXPIRED = 3;
	public final static int USER_ALREADY_AUTHENTICATED = 4;

	public final static String[] reasonDesc = {"credentials invalid", "credentials incompatible",
			"credentials expired", "account expired", "user already authenticated"};

	public int reasonCode;

	/**
	 *
	 */
	public AuthenticationException()
	{
		super();
	}

	/**
	 *
	 * @param reasonCode one of currently 5 reason codes
	 */
	public AuthenticationException(int reasonCode)
	{
		//following statement throws reasonable runtime exception if reason code invalid
		super("authentication failed with message: " + reasonDesc[reasonCode]);
		this.reasonCode = reasonCode;
	}

	/**
	 *
	 * @param reasonCode one of currently 5 reason codes
	 * @param message
	 */
	public AuthenticationException(int reasonCode, String message)
	{
		//following statement throws reasonable runtime exception if reason code invalid
		super("authentication failed with message: " + reasonDesc[reasonCode] + "; " + message);
		this.reasonCode = reasonCode;
	}
	/**
	 *
	 * @return
	 */
	public int getReasonCode()
	{
		return reasonCode;
	}
}
