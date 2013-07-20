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

package org.ijsberg.iglu.mvc;

import org.ijsberg.iglu.server.invocation.CommandLine;

import java.io.IOException;
import java.util.Properties;

/**
 * A mvc result exceptionHandler is invoked by the mvc manager when it processes a mvc tree.
 * It performs tasks and writes responses.
 */
public interface RequestDispatcher {
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
