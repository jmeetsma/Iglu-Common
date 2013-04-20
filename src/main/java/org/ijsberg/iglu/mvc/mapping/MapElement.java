/* =======================================================================
 * Copyright (c) 2003-2010 IJsberg Automatisering BV. All rights reserved.
 * Redistribution and use of this code are permitted provided that the
 * conditions of the Iglu License are met.
 * The license can be found in org.ijsberg.iglu.StandardApplication.java
 * and is also published on http://iglu.ijsberg.org/LICENSE.
 * =======================================================================
 */
package org.ijsberg.iglu.mvc.mapping;


import org.ijsberg.iglu.mvc.RequestDispatcher;

import java.util.Properties;


/**
 * Stands for an entry point, exit point, logical step or attribute in MVC processing.
 * Consists of a keyword and an argument.
 */
public abstract class MapElement
{
	private MapElement parent;

	private int depth;
	protected int lineNr;
	protected String argument;
	protected boolean terminated;
	protected int timesProcessed;
	protected ExceptionHandler exceptionHandler;


	/**
	 * @param argument argument indicating how to process the request
	 * @param depth current depth of the mapping tree
	 * @param lineNr current line number in the mapping file
	 */
	public MapElement(String argument, int depth, int lineNr)
	{
		this.argument = argument;
		this.depth = depth;
		this.lineNr = lineNr;
	}

	/**
	 * @return the argument to this MVC statement
	 */
	public String getArgument()
	{
		return argument;
	}

	/**
	 * @return the parent that contains this element
	 */
	public MapElement getParent()
	{
		return parent;
	}

	/**
	 * @return the actual depth in the MVC tree
	 */
	public int getDepth()
	{
		return depth;
	}

	/**
	 *
	 * @param parent
	 */
	public void setParent(MapElement parent)
	{
		this.parent = parent;
	}


	/**
	 * @return the line number in the mapping file that declares this MVC element
	 */
	public int getLineNr()
	{
		return lineNr;
	}


	/**
	 * @return true if this element is not followed by other elements
	 */
	public boolean isTerminated()
	{
		return terminated;
	}

	/**
	 * @return a string containing whitespace characters (TABs) corresponding to the depth of this element in the MVC tree
	 */
	public String indent()
	{
		StringBuffer result = new StringBuffer("	");
		for (int i = 0; i < depth; i++)
		{
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
 	 *
	 * @param processArray
	* @param requestPropertie
	* @param dispatcher
	* @return true if processing leads to a successful redirect
	 * @throws Throwable
	 */
	public abstract boolean processRequest(String[] processArray, Properties requestPropertie, RequestDispatcher dispatcher);

	/**
	 * @return a (mandatory) brief description of the MVCc element
	 */
	public abstract String toString();
}
