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


import org.ijsberg.iglu.logging.LogEntry;
import org.ijsberg.iglu.mvc.RequestDispatcher;

import java.util.Properties;


/**
 * Stands for an entry point, exit point, logical step or attribute in MVC processing.
 * Consists of a keyword and an argument.
 */
public abstract class MapElement {
	private MapElement parent;

	private int depth;
	protected int lineNr;
	protected String argument;
	protected boolean terminated;
	protected int timesProcessed;
	protected ExceptionHandler exceptionHandler;


	/**
	 * @param argument argument indicating how to process the request
	 * @param depth    current depth of the mapping tree
	 * @param lineNr   current line number in the mapping file
	 */
	public MapElement(String argument, int depth, int lineNr) {
		this.argument = argument;
		this.depth = depth;
		this.lineNr = lineNr;
	}

	/**
	 * @return the argument to this MVC statement
	 */
	public String getArgument() {
		return argument;
	}

	/**
	 * @return the parent that contains this element
	 */
	public MapElement getParent() {
		return parent;
	}

	/**
	 * @return the actual depth in the MVC tree
	 */
	public int getDepth() {
		return depth;
	}

	/**
	 * @param parent
	 */
	public void setParent(MapElement parent) {
		this.parent = parent;
	}


	/**
	 * @return the line number in the mapping file that declares this MVC element
	 */
	public int getLineNr() {
		return lineNr;
	}


	/**
	 * @return true if this element is not followed by other elements
	 */
	public boolean isTerminated() {
		return terminated;
	}

	/**
	 * @return a string containing whitespace characters (TABs) corresponding to the depth of this element in the MVC tree
	 */
	public String indent() {
		StringBuffer result = new StringBuffer("	");
		for (int i = 0; i < depth; i++) {
			result.append('\t');
		}
		return result.toString();
	}

	/**
	 * @param dispatcher
	 * @return an (error) message in case this MVC element is not suitable for processing
	 */
	public abstract String check(RequestDispatcher dispatcher);

	/**
	 * Adds a subelement, such as a <emph>possible result</emph> to a defined <emph>method invocation</emph>.
	 *
	 * @param fe
	 * @return
	 */
	public abstract boolean addFlowElement(MapElement fe);

	/**
	 * Processes this MVC element as well as (resulting) subelements.
	 *
	 * @param processArray
	 * @param requestPropertie
	 * @param dispatcher
	 * @return true if processing leads to a successful redirect
	 * @throws Throwable
	 */
	public abstract boolean processRequest(String[] processArray, Properties requestPropertie, RequestDispatcher dispatcher) throws Exception;

	/**
	 * @return a (mandatory) brief description of the MVCc element
	 */
	public abstract String toString();

	protected boolean handleException(String[] processArray, Properties requestProperties, RequestDispatcher dispatcher, Exception e) throws Exception {
		Throwable caught = null;
		if (exceptionHandler != null && (caught = exceptionHandler.doesCatch(e)) != null) {
			requestProperties.put("exception", caught);
			System.out.println(new LogEntry("exception occurred that will be handled by mvc exception handler", e));
			return exceptionHandler.processRequest(processArray, requestProperties, dispatcher);
		}
		throw e;
	}
}
