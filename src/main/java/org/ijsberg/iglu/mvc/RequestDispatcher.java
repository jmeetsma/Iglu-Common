package org.ijsberg.iglu.mvc;

import org.ijsberg.iglu.server.connection.invocation.CommandLine;

import java.io.IOException;
import java.util.Properties;

/**
 * A mvc result exceptionHandler is invoked by the mvc manager when it processes a mvc tree.
 * It performs tasks and writes responses.
 */
public interface RequestDispatcher
{
	/**
	 * Redirects the request to a different target.
	 *
	 * @param target
	 * @param parameters
	 * @return
	 * @throws IOException
	 */
	boolean redirect(String target, String[] parameters/*boolean copyParameter*/);

	/**
	 * Tests if a redirect to specified target will be successful
	 *
	 * @param target
	 * @param parameters
	 * @return
	 */
	String testRedirect(String target, String[] parameters);

	/**
	 * Dispatches the request to a presentation element within the request scope.
	 *
	 * @param target
	 * @param parameters
	 * @return
	 * @throws IOException
	 */
	boolean dispatch(String target, String[] parameters);

	/**
	 * Tests if a dispatch to some target will work.
	 *
	 * @param target
	 * @param parameters
	 * @return
	 */
	String testDispatch(String target, String[] parameters);

	Object handleInvocation(CommandLine commandLine, Properties requestProperties) throws Throwable;



    boolean respond(CommandLine commandLine, Properties requestProperties) throws Throwable;
}
