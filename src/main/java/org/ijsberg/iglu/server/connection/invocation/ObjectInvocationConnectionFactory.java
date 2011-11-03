package org.ijsberg.iglu.server.connection.invocation;

import org.ijsberg.iglu.server.connection.ConnectionFactory;
import org.ijsberg.iglu.server.connection.socket.ByteStreamReadingConnection;
import org.ijsberg.iglu.server.telnet.TelnetAdapter;

import java.io.IOException;
import java.net.Socket;

/**
 */
public class ObjectInvocationConnectionFactory implements ConnectionFactory {
	private Object invokable;

	public ObjectInvocationConnectionFactory(Object invokable) {
		this.invokable = invokable;
	}

	public ByteStreamReadingConnection createConnection(Socket socket) throws IOException {
		return new ByteStreamReadingConnection(socket, new TelnetAdapter(), new CommandLineObjectInvoker(invokable), 900);
	}

}
