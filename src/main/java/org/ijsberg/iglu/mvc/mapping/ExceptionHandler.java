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

package org.ijsberg.iglu.mvc.mapping;

import org.ijsberg.iglu.util.misc.StringSupport;

import java.util.Collection;

/**
 * Marks the start of a path that is followed if an invocation throws an exception.
 */
public class ExceptionHandler extends InvocationResultExpression {
	private Collection caughtExceptions;

	/**
	 * @param value
	 * @param depth
	 * @param lineNr
	 */
	public ExceptionHandler(String value, int depth, int lineNr) {
		super(value, org.ijsberg.iglu.mvc.mapping.InvocationResultExpression.EQ, depth, lineNr);
		caughtExceptions = StringSupport.split(value, " ,");
	}


	/**
	 * @return
	 */
	public String toString() {
		return "exceptionHandler of: " + getArgument();
	}

	/**
	 * @param t
	 * @return true if the throwable encountered is listed
	 */
	public Throwable doesCatch(Throwable t) {
		while (t != null) {
			if (caughtExceptions.contains(t.getClass().getName()) || caughtExceptions.contains(t.getClass().getSimpleName())) {
				return t;
			}
			t = t.getCause();
		}
		return null;
	}
}
