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

package org.ijsberg.iglu.integration.telnetserver;

import org.ijsberg.iglu.server.connection.Connection;
import org.ijsberg.iglu.server.connection.ConnectionFactory;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

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
