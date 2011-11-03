package org.ijsberg.iglu.server.telnet.test;

import org.ijsberg.iglu.server.connection.invocation.ObjectInvocationConnectionFactory;
import org.ijsberg.iglu.server.connection.socket.module.StandardSocketServer;

/**
 * Starts a telnet server on port 17623.
 * The server gives telnet clients direct access to public methods of a test object.
 */
public class TelnetTestServer {

	private void initialize() {

		StandardSocketServer socketServer = new StandardSocketServer();
		ObjectInvocationConnectionFactory connectionFactory = new ObjectInvocationConnectionFactory(new TestObject());
		socketServer.setConnectionFactory(connectionFactory);
		socketServer.start();
	}

	public static void main(String[] args) {
		TelnetTestServer server = new TelnetTestServer();
		server.initialize();
	}
}
