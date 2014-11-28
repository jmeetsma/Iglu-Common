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

package org.ijsberg.iglu.mvc.mapping;


import org.ijsberg.iglu.mvc.RequestDispatcher;

import java.util.Properties;

/**
 * Acts as a placeholder for unparsable mapping lines.
 */
public class UnparsableLine extends MapElement {
	/**
	 * @param argument
	 * @param depth
	 * @param lineNr
	 */
	public UnparsableLine(String argument, int depth, int lineNr) {
		super(argument, depth, lineNr);
	}

	/**
	 * @param handle
	 * @return error message
	 */
	public String check(RequestDispatcher handle) {
		return "unparseable statement " + argument + " at line " + lineNr;
	}

	/**
	 * @param fe
	 * @return true
	 */
	public boolean addFlowElement(MapElement fe) {
		return true;
	}

	/**
	 * Does nothing.
	 *
	 * @param processArray
	 * @param requestProperties
	 * @param dispatcher
	 * @return true
	 * @throws Throwable
	 */
	public boolean processRequest(String[] processArray, Properties requestProperties, RequestDispatcher dispatcher) {
		return true;
	}

	/**
	 * @return
	 */
	public String toString() {
		return "unparseable statement " + argument + " at line " + lineNr;
	}
}
