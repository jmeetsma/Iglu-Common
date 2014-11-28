/*
 * Copyright 2011-2014 Jeroen Meetsma - IJsberg Automatisering BV
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
package org.ijsberg.iglu.server.connection.socket;


import org.ijsberg.iglu.logging.Level;
import org.ijsberg.iglu.logging.LogEntry;
import org.ijsberg.iglu.server.connection.CommandLineClientAdapter;
import org.ijsberg.iglu.server.connection.CommandLineInterpreter;
import org.ijsberg.iglu.server.connection.Connection;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * This class embeds the socket that is connected to a client.
 * It facilitates communication between a technology specific
 * adapter and a remote client.
 */
public class ByteStreamReadingConnection implements Connection, Runnable {
	private DataInputStream is;
	private OutputStream os;
	private Socket socket;

	private String closeMessage;

	private Thread t;

	private int timeout;//s
	private long lastUpdate = System.currentTimeMillis();

	private CommandLineClientAdapter adapter;//adapts a technical protocol
	private CommandLineInterpreter interpreter;//interprets incoming data according to a higher level protocol


	/**
	 * Invoked by connection factory to spawn a client connection for a server.
	 *
	 * @param socket      client socket which is connected to a tcp client
	 * @param adapter     command-line adapter
	 * @param interpreter command-line interpreter
	 * @param timeout     time out in seconds when idle
	 * @see org.ijsberg.iglu.server.connection.ConnectionFactory
	 */
	public ByteStreamReadingConnection(Socket socket, CommandLineClientAdapter adapter, CommandLineInterpreter interpreter, int timeout) throws IOException {
		this.socket = socket;
		this.adapter = adapter;
		this.interpreter = interpreter;
		this.timeout = timeout;

		is = new DataInputStream(socket.getInputStream());
		os = socket.getOutputStream();

		t = new Thread(this);
		t.start();
	}


	/**
	 * Contains the loop that processes incoming bytes.
	 */
	public void run() {

		try {
			//initiate session within same thread
			adapter.initiateSession(this, os, interpreter);

			while (processingIncomingBytes()) {
			}
		} catch (InterruptedException ie) {
			adapter.onConnectionClose(closeMessage);
		} catch (IOException ioe) {
			System.out.println(new LogEntry("", ioe));
		} finally {
			try {
				if (!socket.isClosed()) {
					socket.close();
				}
			} catch (IOException ioe) {
				System.out.println(new LogEntry(Level.CRITICAL, "unable to close connection", ioe));
			}
		}
	}

	private boolean processingIncomingBytes() throws IOException, InterruptedException {
		int avail = is.available();
		if (avail > 0) {
			lastUpdate = System.currentTimeMillis();
			byte[] bytes = new byte[avail];
			is.read(bytes, 0, avail);
			adapter.receive(bytes);
		} else {
			Thread.sleep(25);
		}
		if ((timeout > 0) && ((System.currentTimeMillis() - lastUpdate) > (timeout * 1000))) {
			close("connection timed out after " + timeout + " seconds");
			return false;
		}
		return true;
	}


	/**
	 * Sends a message to the telnet client.
	 *
	 * @param message
	 */
	public void send(byte[] message) {
		try {
			os.write(message);
		} catch (IOException ioe) {
			System.out.println(new LogEntry("closing connection to " + socket.getInetAddress() + " due to IO exception " + ioe.getMessage(), ioe));
			close("closing connection to due to IO exception");
		}
	}


	@Override
	/**
	 * Stops processing incoming data and closes the connection.
	 * @param message
	 */
	public void close(String message) {
		closeMessage = message;
		close();
	}

	@Override
	/**
	 * Stops processing incoming data and closes the connection.
	 * @param message
	 */
	public void close() {
		t.interrupt();
		try {
			if (!socket.isClosed()) {
				socket.close();
			}
		} catch (IOException ioe) {
			System.out.println(new LogEntry(Level.CRITICAL, "IOException occurred while closing connection to " + socket.getInetAddress() + " with message: " + ioe.getMessage(), ioe));
		}
	}

	@Override
	/**
	 *
	 */
	public boolean isClosed() {
		return socket.isClosed();
	}
}
