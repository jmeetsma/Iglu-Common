package org.ijsberg.iglu.integration.telnetserver;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.ijsberg.iglu.server.connection.Connection;
import org.ijsberg.iglu.server.connection.ConnectionFactory;

/**
 */
public class DummyConnectionFactory implements ConnectionFactory {

	List<DummyConnection> connections = new ArrayList();

	@Override
	public Connection createConnection(Socket socket) throws IOException {
		DummyConnection connection = new DummyConnection(socket);
		connections.add(connection);
		return connection;
	}

	public List<DummyConnection> getConnections() {
		return connections;
	}
}
