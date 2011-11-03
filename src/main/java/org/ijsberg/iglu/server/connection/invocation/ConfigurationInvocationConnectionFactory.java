package org.ijsberg.iglu.server.connection.invocation;

import org.ijsberg.iglu.Configuration;
import org.ijsberg.iglu.server.connection.Connection;
import org.ijsberg.iglu.server.connection.ConnectionFactory;
import org.ijsberg.iglu.server.connection.socket.*;
import org.ijsberg.iglu.server.telnet.TelnetAdapter;

import java.io.IOException;
import java.net.Socket;

/**
 */
public class ConfigurationInvocationConnectionFactory implements ConnectionFactory
{
	private Configuration configuration;

	public ConfigurationInvocationConnectionFactory(Configuration configuration) {
		this.configuration = configuration;
	}

	public Connection createConnection(Socket socket) throws IOException {
		return new ByteStreamReadingConnection(socket, new TelnetAdapter(), new CommandLineConfigurationInvoker(configuration), 900);
	}
}
