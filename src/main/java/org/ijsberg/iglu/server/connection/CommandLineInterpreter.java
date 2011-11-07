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
package org.ijsberg.iglu.server.connection;


/**
 * Implementations interpret and process data from a command-line adapter.
 *
 */
public interface CommandLineInterpreter extends ClientSession {
	/**
	 * Sets a reference to the adapter that connects to the client connection.
	 * Kicks off the communication with the user.
	 *
	 * @param adapter
	 */
	void initiateSession(CommandLineClientAdapter adapter);

	/**
	 * Is (to be) invoked by an adapter if it's is about to be terminated
	 * and the client connection will be closed.
	 * @param message
	 */
	void onAdapterTermination(String message);

	/**
	 * Interprets, processes and may respond to input from the client connection.
	 *
	 * @param rawInput input from the client connection
	 */
	void processRawInput(byte[] rawInput);


	/**
	 * Interprets, processes and responds to input from the client connection.
	 *
	 * @param commandLine input from the client connection
	 * @return response
	 */
	String processCommandLine(String commandLine);

	/**
	 * Aborts sub process mode and returns to command line interpretation.
	 * This may for instance be invoked when ctr-c is pressed.
	 *
	 */
	void abortSubProcessMode();

	/**
	 * An interpreter may delegate control to some sub process that
	 * handles raw input and output.
	 *
	 * @return true if input and output is handled by a sub process
	 */
	boolean isInSubProcessMode();


	/**
	 * Implementations may provide command-line completion.
	 * Otherwise the input parameter must be returned.
	 *
	 * @param unfinishedCommand
	 * @return a completed command if suitable
	 */
	String completeCommand(String unfinishedCommand);

}


