/*
 * Copyright 2011 Jeroen Meetsma
 *
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

package org.ijsberg.iglu.server.connection.invocation;


import org.ijsberg.iglu.configuration.Assembly;
import org.ijsberg.iglu.invocation.CommandLineProcessor;
import org.ijsberg.iglu.server.connection.CommandLineClientAdapter;
import org.ijsberg.iglu.server.connection.CommandLineInterpreter;
import org.ijsberg.iglu.util.formatting.PatternMatchingSupport;
import org.ijsberg.iglu.util.misc.StringSupport;


/**
 * Hooks up an adapter directly to an agent so that all its exposed methods may be
 * invoked by command line.
 */
public class CommandLineConfigurationInvoker extends CommandLineProcessor implements CommandLineInterpreter {

	public static final String CSV_PATTERN = "(|([0-9]+|\".*[^\"]\")(,[0-9]+|,\".*[^\"]\")*)";
	public static final String CL_PATTERN = "[A-Za-z_][A-Za-z0-9_]*\\(" + CSV_PATTERN + "\\)";

	private CommandLineClientAdapter adapter;

	/**
	 *
	 * @param line
	 * @return
	 */
	static boolean isCommand(String line) {
		return PatternMatchingSupport.valueMatchesRegularExpression(line, CL_PATTERN);
	}

	static boolean isCSVArray(String line) {
		return PatternMatchingSupport.valueMatchesRegularExpression(line, CSV_PATTERN);
	}


	/**
	 *
	 */
	public CommandLineConfigurationInvoker(Assembly assembly) {
		super(assembly);
	}


	/**
	 * @param adapter
	 */
	public void initiateSession(CommandLineClientAdapter adapter) {
		this.adapter = adapter;
	}

	/**
	 * Releases user request.
	 *
	 * @param string
	 */
	public void onAdapterTermination(String string) {
	}

	/**
	 * Does nothing.
	 *
	 * @param bytes
	 */
	public void processRawInput(byte[] bytes) {
	}

	/**
	 * Processes a client command line.
	 * The response consists of a few lines.
	 * The first line is either 'OK' or an 'ERROR',
	 * followed by the result.toString() or respectively
	 * an error message.
	 * Collections are printed as a series of lines prifixed by dashes.
	 *
	 * @param commandLine
	 * @return
	 */
	public String processCommandLine(String commandLine) {
		StringBuffer response = new StringBuffer();
		try {
				response.append(super.processCommandLine(commandLine));
		} catch (Throwable t) {
			response.append(StringSupport.getStackTrace(t, 5) + "\n");
		}
		return response.toString();
	}

	/**
	 * Does nothing.
	 */
	public void abortSubProcessMode() {
	}

	/**
	 * @return false
	 */
	public boolean isInSubProcessMode() {
		return false;
	}

	/**
	 * @param unfinishedCommand
	 * @return null
	 */
	public String completeCommand(String unfinishedCommand) {
		return null;
	}

	/**
	 * @param message
	 */
	public synchronized void sendMessage(String message) {
		adapter.send(message);
	}

	@Override
	/**
	 *
	 */
	public void terminate() {
		adapter.terminateSession();
	}

}
