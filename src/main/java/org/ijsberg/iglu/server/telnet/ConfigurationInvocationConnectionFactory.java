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

package org.ijsberg.iglu.server.telnet;

import org.ijsberg.iglu.configuration.Assembly;
import org.ijsberg.iglu.server.connection.Connection;
import org.ijsberg.iglu.server.connection.ConnectionFactory;
import org.ijsberg.iglu.server.connection.socket.ByteStreamReadingConnection;
import org.ijsberg.iglu.server.invocation.CommandLineConfigurationInvoker;

import java.io.IOException;
import java.net.Socket;

/**
 */
public class ConfigurationInvocationConnectionFactory implements ConnectionFactory {
	private Assembly assembly;

	public ConfigurationInvocationConnectionFactory(Assembly assembly) {
		this.assembly = assembly;
	}

	public Connection createConnection(Socket socket) throws IOException {
		return new ByteStreamReadingConnection(socket, new TelnetAdapter(), new CommandLineConfigurationInvoker(assembly), 900);
	}
}
