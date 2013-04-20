package org.ijsberg.iglu.mvc.mapping;


import org.ijsberg.iglu.mvc.RequestDispatcher;

import java.util.Properties;

/**
 * Acts as a placeholder for unparsable mapping lines.
 */
public class UnparsableLine extends MapElement
{
	/**
	 * @param argument
	 * @param depth
	 * @param lineNr
	 */
	public UnparsableLine(String argument, int depth, int lineNr)
	{
		super(argument, depth, lineNr);
	}

	/**
	 * @param handle
	 * @return error message
	 */
	public String check(RequestDispatcher handle)
	{
		return "unparseable statement " + argument + " at line " + lineNr;
	}

	/**
	 * @param fe
	 * @return true
	 */
	public boolean addFlowElement(MapElement fe)
	{
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
	public boolean processRequest(String[] processArray, Properties requestProperties, RequestDispatcher dispatcher)
	{
		return true;
	}

	/**
	 * @return
	 */
	public String toString()
	{
		return "unparseable statement " + argument + " at line " + lineNr;
	}
}
