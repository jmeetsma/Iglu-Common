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

import org.ijsberg.iglu.exception.ResourceException;
import org.ijsberg.iglu.server.connection.Connection;
import org.ijsberg.iglu.util.misc.StringSupport;

import java.io.IOException;
import java.net.Socket;

/**
 */
public class DummyConnection implements Connection {

	private Socket socket;

	public DummyConnection(Socket socket) {
		this.socket = socket;
	}

	public String getInput() throws IOException {
		return StringSupport.absorbInputStream(socket.getInputStream());
	}

	@Override
	public void close(String message) {
		close();
	}

	@Override
	public void close() {
		try {
			socket.close();
		} catch (IOException e) {
			throw new ResourceException(e);
		}
	}

	@Override
	public boolean isClosed() {
		return socket.isClosed();
	}
}
