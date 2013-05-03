package org.ijsberg.iglu.mvc.mapping;

import org.ijsberg.iglu.configuration.Assembly;
import org.ijsberg.iglu.configuration.ConfigurationException;
import org.ijsberg.iglu.logging.LogEntry;
import org.ijsberg.iglu.mvc.RequestDispatcher;
import org.ijsberg.iglu.server.connection.invocation.CommandLine;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;


/**
 * Definition of the invocation of a module method.
 */
public class Invocation extends MapElement
{
//	private String commandIdentifier;
//	private String[] arguments;
	private ArrayList results = new ArrayList();

	protected String formId;
//	protected String moduleId;


	private static final int INVOKE_UNDECIDED = 0;
	private static final int INVOKE_EMPTY = 1;
	private static final int INVOKE_WITH_PROPERTIES = 2;
	private static final int INVOKE_WITH_FORM = 3;
	private static final int INVOKE_CUSTOMIZED = 4;
	private int invocationType;

	//TODO it should be the mapper or the dispatcher that have this logic
	protected Assembly assembly;
	protected CommandLine command;

	//private Component module;


	/**
	 *
	 * @param assembly
	 * @param taskName
	 * @param depth
	 * @param lineNr
	 * @param async
	 */
	public Invocation(Assembly assembly, String taskName, int depth, int lineNr, boolean async)
	{
		super(taskName, depth, lineNr);

		this.assembly = assembly;
		command = new CommandLine(taskName);
		if(command.getArguments().length == 1) {
			formId = command.getArguments()[0].toString();
		}

 /*
		assembly.getClusters().get(command.getClusterId()).getInternalComponents().get(command.getComponentId()).get
				invoke(methodName, arguments);
		assembly.getClusters().get(command.getClusterId()).
*/
/*
		String[] command = disectCommandLine(taskName);

		commandIdentifier = command[0];

		//TODO this is also part of console code
		if (commandIdentifier.indexOf('.') != -1)
		{
			moduleId = commandIdentifier.substring(0, commandIdentifier.lastIndexOf('.')).trim();
			commandIdentifier = commandIdentifier.substring(commandIdentifier.lastIndexOf('.') + 1);
		}
		if (command.length > 1)
		{
			formId = command[1].trim();
		}

		if (command.length > 1)
		{
			arguments = new String[command.length - 1];
			System.arraycopy(command, 1, arguments, 0, command.length - 1);
		}
		else
		{
			arguments = new String[0];
		}        */
		boolean async1 = async;
	}

	/**
	 * @param dispatcher
	 * @return
	 */
	public String check(RequestDispatcher dispatcher)
	{
//		if (command.getMethodName() == null)
		{
//			return "method not be determined from invocation '" + this.argument + "'";
		}
		//whether a agent exists can only be determined if a request is
		//  done by a regular user
		return null;
	}

	/**
	 * Adds invocation results and exception handlers.
	 *
	 * @param fe
	 * @return
	 */
	public boolean addFlowElement(MapElement fe)
	{
		if (fe instanceof ExceptionHandler)
		{
			exceptionHandler = (ExceptionHandler) fe;
			return true;
		}
		else if (fe instanceof InvocationResultExpression)
		{
			if (results == null)
			{
				results = new ArrayList();
			}
			results.add(fe);
			return true;
		}
		return false;
	}

	/**
	 *
	 * @param processArray
	 * @param requestProperties
	 * @param dispatcher
	 * @return
	 * @throws ConfigurationException
	 */
	public boolean processRequest(String[] processArray, Properties requestProperties, RequestDispatcher dispatcher)
			throws Exception
	{
		timesProcessed++;

		//success is defined by a successful redirect
		Object result = null;

		try
		{
			result = dispatcher.handleInvocation(command, requestProperties);
			if(result != null) {
				requestProperties.put("result", result);
			}
		}
		catch (Exception e)
		{
			return handleException(processArray, requestProperties, dispatcher, e);
		}
		catch (Throwable t)
		{
			System.out.println(new LogEntry("exception occurred in mvc invocation", t));
			throw new RuntimeException("unable to invoke assembly", t);
			//TODO provide string explaining invocationType
/*			throw new ConfigurationException("unable to invoke module '" + moduleId + "' with command-ID '"
					+ commandIdentifier + "' and parameters ("
					+ CollectionSupport.format(requestProperties, ",") + ") for arguments ("
					+ ArraySupport.format(arguments, ",") + ") invocation type=" + invocationType + "", t);
*/		}

		Iterator i = results.iterator();

		while (i.hasNext())
		{
			InvocationResultExpression r = (InvocationResultExpression) i.next();

			if (r.isMatch(result))
			{
				return r.processRequest(processArray, requestProperties, dispatcher);
			}
		}
		return false;
	}


	/**
	 * Determines which method how to invoke on which module
	 * and performs the invocation using the request properties as input.
	 *
	 * @param requestProperties
	 * @return
	 * @throws ConfigurationException
	 */
//	private Object invoke(Properties requestProperties) throws Throwable





	/**
	 * @return
	 */
	public String toString()
	{
		StringBuffer result = new StringBuffer(indent() + "INVOKE " + argument + " -> " + timesProcessed + "\n");
		if (results != null)
		{
			Iterator i = results.iterator();
			while (i.hasNext())
			{
				MapElement fe = (MapElement) i.next();
				result.append(fe.toString());
			}
		}
		return result.toString();
	}


	/**
	 * Expects a command and 0 or more arguments like so:
	 * command(argument1,argument2,argument3)
	 *
	 * @param line Command line
	 * @return String list, #0 is the command, the rest are arguments
	 */
/*	public static String[] disectCommandLine(String line)
	{
		return (String[]) StringSupport.split(line, "() ,;", false, false, false, "\"").toArray(new String[0]);
	}
*/
}
