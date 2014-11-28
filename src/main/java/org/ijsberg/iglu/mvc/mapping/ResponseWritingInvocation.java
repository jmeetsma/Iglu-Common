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

import org.ijsberg.iglu.configuration.Assembly;
import org.ijsberg.iglu.logging.LogEntry;
import org.ijsberg.iglu.mvc.RequestDispatcher;

import java.util.Properties;

/**
 */
public class ResponseWritingInvocation extends Invocation implements ResponseWriter {
	public ResponseWritingInvocation(Assembly assembly, String command, int depth, int lineNr) {
		super(assembly, command, depth, lineNr, false);
	}

	public boolean processRequest(String[] processArray, Properties requestProperties, RequestDispatcher dispatcher) throws Exception {
		timesProcessed++;

		//success is defined by a successful redirect
		Object result = null;

		try {
			result = dispatcher.respond(command, requestProperties);
			requestProperties.put("result", result);
		} catch (Exception e) {
			return handleException(processArray, requestProperties, dispatcher, e);
		} catch (Throwable t) {
			System.out.println(new LogEntry("exception occurred in mvc invocation", t));
			throw new RuntimeException("unable to invoke assembly", t);
			//TODO provide string explaining invocationType
/*			throw new ConfigurationException("unable to invoke module '" + moduleId + "' with command-ID '"
					+ commandIdentifier + "' and parameters ("
					+ CollectionSupport.format(requestProperties, ",") + ") for arguments ("
					+ ArraySupport.format(arguments, ",") + ") invocation type=" + invocationType + "", t);
*/
		}
		return true;
	}


}
