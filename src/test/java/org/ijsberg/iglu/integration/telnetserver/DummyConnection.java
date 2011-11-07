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
		}
		catch (IOException e) {
			throw new ResourceException(e);
		}
	}

	@Override
	public boolean isClosed() {
		return socket.isClosed();
	}
}
