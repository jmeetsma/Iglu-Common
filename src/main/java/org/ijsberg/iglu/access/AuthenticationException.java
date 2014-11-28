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

import org.ijsberg.iglu.exception.StateException;

/**
 * This exception should be a checked exception, because it represents an event that
 * may occur under normal circumstances, and must be dealt with.
 * It is however a runtime exception because it only must be dealt with
 * at the presentation level of an application.
 * <p/>
 * Authentication may have failed if credentials are invalid, incompatible or expired,
 * or if a user account is expired or if the user was already authenticated.
 */
public class AuthenticationException extends StateException {
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
	public AuthenticationException() {
		super();
	}

	/**
	 * @param reasonCode one of currently 5 reason codes
	 */
	public AuthenticationException(int reasonCode) {
		//following statement throws reasonable runtime exception if reason code invalid
		super("authentication failed with message: " + reasonDesc[reasonCode]);
		this.reasonCode = reasonCode;
	}

	/**
	 * @param reasonCode one of currently 5 reason codes
	 * @param message
	 */
	public AuthenticationException(int reasonCode, String message) {
		//following statement throws reasonable runtime exception if reason code invalid
		super("authentication failed with message: " + reasonDesc[reasonCode] + "; " + message);
		this.reasonCode = reasonCode;
	}

	/**
	 * @return
	 */
	public int getReasonCode() {
		return reasonCode;
	}
}
