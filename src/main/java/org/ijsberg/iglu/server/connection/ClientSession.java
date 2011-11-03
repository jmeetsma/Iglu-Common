package org.ijsberg.iglu.server.connection;

/**
 * Represents a session with a connected client.
 */
public interface ClientSession {

	/**
	 * Sends a message back to the client (asynchronously).
	 *
	 * @param message
	 */
	void sendMessage(String message);

	/**
	 * Closes connection.
	 *
	 */
	void terminate();
}
