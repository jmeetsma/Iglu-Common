package org.ijsberg.iglu.mvc.mapping;


import org.ijsberg.iglu.mvc.RequestDispatcher;

import java.util.Properties;

/**
 * Dispatches a request to a resource that produces a response
 * based on the (processed) request data.
 */
public class Dispatch extends MapElement implements ResponseWriter
{
	//arguments containing additional process instructions for the dispatcher
	private String[] additionalArguments;

	/**
	 * @param command
	 * @param depth
	 * @param lineNr
	 */
	public Dispatch(String[] command, int depth, int lineNr)
	{
		super(command[0], depth, lineNr);
		additionalArguments = new String[command.length - 1];
		System.arraycopy(command, 1, additionalArguments, 0, command.length - 1);
	}

	/**
	 * @param dispatcher
	 * @return an error message in case request dispatching is not possible
	 */
	public String check(RequestDispatcher dispatcher)
	{
		if (argument == null || "".equals(argument))
		{
			return "missing label";
		}
		return dispatcher.testDispatch(argument, additionalArguments);
	}


	/**
	 * Doesn't do anything, because a dispatch marks the end of a request.
	 *
	 * @param fe
	 * @return false
	 */
	public boolean addFlowElement(MapElement fe)
	{
		return false;
	}

	/**
	 * Dispatches request back to the GUI-specific dispatcher that must generate a response.
	 *
	 * @param processArray
	 * @param requestProperties
	 * @param dispatcher
	 * @return
	 * @throws Throwable
	 */
	public boolean processRequest(String[] processArray, Properties requestProperties, RequestDispatcher dispatcher)// throws Throwable
	{
		timesProcessed++;
		return dispatcher.dispatch(argument, additionalArguments);
	}

	/**
	 * @return a description of this MVC element including the nr of times invoked
	 */
	public String toString()
	{
		return indent() + "DISPATCH " + getArgument() + " -> " + timesProcessed + "\n";
	}
}
