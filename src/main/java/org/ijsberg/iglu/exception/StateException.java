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

package org.ijsberg.iglu.exception;

/**
 * Sometimes a request can not be handled, because the state of the
 * part that must handle it, does not permit it.
 * <p/>
 * In some cases such an exceptional situation is not critical and
 * may be handled by the user (interface).
 */
public class StateException extends RuntimeException {
	private Object details;

	/**
	 *
	 */
	public StateException() {
		super();
	}

	/**
	 * @param message
	 */
	public StateException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public StateException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public StateException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 * @param details data needed for analysis
	 */
	public StateException(String message, Object details) {
		super(message);
		this.details = details;
	}

	/**
	 * @param message
	 * @param details data needed for analysis
	 * @param cause
	 */
	public StateException(String message, Object details, Throwable cause) {
		super(message, cause);
		this.details = details;
	}

	/**
	 * @return
	 */
	public Object getDetails() {
		return details;
	}

}
