package org.ijsberg.iglu.mvc.mapping;

import org.ijsberg.iglu.configuration.Assembly;
import org.ijsberg.iglu.configuration.ConfigurationException;
import org.ijsberg.iglu.logging.LogEntry;
import org.ijsberg.iglu.mvc.RequestDispatcher;

import java.util.Iterator;
import java.util.Properties;

/**
 */
public class ResponseWritingInvocation extends Invocation implements ResponseWriter
{
	public ResponseWritingInvocation(Assembly assembly, String command, int depth, int lineNr)
	{
		super(assembly, command, depth, lineNr, false);
	}

	public boolean processRequest(String[] processArray, Properties requestProperties, RequestDispatcher dispatcher) throws Exception
	{
        timesProcessed++;

        //success is defined by a successful redirect
        Object result = null;

        try
        {
            result = dispatcher.respond(command, requestProperties);
			requestProperties.put("result", result);
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
		return true;
	}


}
