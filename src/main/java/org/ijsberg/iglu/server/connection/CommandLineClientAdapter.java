package org.ijsberg.iglu.server.connection;


import java.io.IOException;
import java.io.OutputStream;

/**
 * Implementations translate communication between client socket and interpreter.
 * Adapters have knowledge of low level protocols such as the telnet protocol.
 */
public interface CommandLineClientAdapter {
	/**
	 * Starts a communication session between remote client and application.
	 * Implementations that act as entry point may bind the
	 * current thread to a request here.
	 *
	 * @param connection
	 * @param os
	 * @param interpreter
	 * @throws IOException
	 */
	void initiateSession(Connection connection, OutputStream os, CommandLineInterpreter interpreter) throws IOException;

	/**
	 * Processes incoming bytes.
	 *
	 * @param bytes
	 * @throws IOException
	 */
	void receive(byte[] bytes) throws IOException;

	/**
	 * Disables echo of incoming bytes.
	 */
	void disableEcho();

	/**
	 * Enables echo of incoming bytes.
	 */
	void enableEcho();

	/**
	 * Sends a string as a series of bytes back across the line.
	 *
	 * @param message
	 */
	void send(String message);

	/**
	 * Sends back a series of bytes.
	 *
	 * @param message
	 */
	void send(byte[] message);

	/**
	 * Tries to produce a beep sound in the remote client.
	 * (This is actually the most important function of the Iglu framework.)
	 */
	void beep();

	/**
	 * Is (to be) invoked by the client connection when it is about to close.
	 * Implementations may in turn notify a connected interpreter.
	 *
	 * @param message system message or returned interpreter response
	 * @see CommandLineInterpreter#onAdapterTermination(String)
	 */
	void onConnectionClose(String message);

	/**
	 * Invoked by the interpreter.
	 * Terminates communication session.
	 * Implementations are responsible of closing the connection as well.
	 *
	 */
	void terminateSession();

}
