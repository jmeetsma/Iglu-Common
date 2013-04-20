package org.ijsberg.iglu.mvc.mapping;


import org.ijsberg.iglu.mvc.RequestDispatcher;

import java.util.Properties;

/**
 * Contains the data to perform a redirect to another target.
 * This may be another invocation of the MVC mechanism.
 * <p/>
 * An MVC request must ultimately result in a redirect or a dispatch.
 * A dispatch handles and finishes the request rather than redirecting it.
 *
 * @see Dispatch
 */
//TODO introduce REDIRECT POST PARAMETERS
//TODO introduce REDIRECT PREVIOUS / DISPATCH PREVIOUS
public class Redirect extends MapElement implements ResponseWriter
{
	private String[] additionalArguments;

	/**
	 *
	 * @param command
	 * @param depth
	 * @param lineNr
	 */
	public Redirect(String[] command, int depth, int lineNr)
	{
		super(command[0], depth, lineNr);
		additionalArguments = new String[command.length - 1];
		System.arraycopy(command, 1, additionalArguments, 0, command.length - 1);
	}

	/**
	 *
	 * @param dispatcher
	 * @return
	 */
	public String check(RequestDispatcher dispatcher)
	{
		if (argument == null || "".equals(argument))
		{
			return "missing label";
		}
		return dispatcher.testRedirect(argument, additionalArguments);
//		return null;
	}

	/**
	 * Doesn't do anything, because a redirect marks the end of a request.
	 *
	 * @param fe
	 * @return
	 */
	public boolean addFlowElement(MapElement fe)
	{
		return false;
	}

	/**
	 *
	 * @param processArray
	 * @param requestProperties
	 * @param dispatcher
	 * @return
	 * @throws Throwable
	 */
	public boolean processRequest(String[] processArray, Properties requestProperties, RequestDispatcher dispatcher)
	{
		timesProcessed++;
		//processRequest success is ultimately defined by a successful redirect!!!
		return dispatcher.redirect(argument, additionalArguments/*, switchSecure, switchInsecure, copyParameters*/);
	}

	/**
	 * 
	 * @return
	 */
	public String toString()
	{
		return indent() + "REDIRECT " + getArgument() + " -> " + timesProcessed + "\n";
	}
}
