/*
 * Copyright 2011 Jeroen Meetsma
 *
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
package org.ijsberg.iglu.server.connection.socket.module;

import org.ijsberg.iglu.ConfigurationException;
import org.ijsberg.iglu.logging.Level;
import org.ijsberg.iglu.logging.LogEntry;
import org.ijsberg.iglu.runtime.Startable;
import org.ijsberg.iglu.server.connection.Connection;
import org.ijsberg.iglu.server.connection.ConnectionFactory;
import org.ijsberg.iglu.server.connection.socket.SocketServer;
import org.ijsberg.iglu.util.collection.CollectionSupport;
import org.ijsberg.iglu.util.types.Converter;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Properties;

/**
 * This service allows clients to start a connection to the application (server).
 * As soon as a socket connection is established,
 * it is embedded in a Connection object.
 */
public class StandardSocketServer implements Runnable, SocketServer, Startable {

	private Thread serverThread;
	private ServerSocket server;
	private ConnectionFactory connectionFactory;
	private final HashSet<Connection> connectedClients = new HashSet<Connection>();

	//settings
	private int port = 17623;
	private boolean keepAlive;
	private int soTimeout;//ms
	private int timeout = 900;//s
	private boolean soLingerActive;
	private int soLingerTime = 10;//s


	/**
	 *
	 * @param connectionFactory
	 */
	public void setConnectionFactory(ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}


	/**
	 * @return a list of information of connected clients separated by a carriage return and newline character
	 */
	public String getReport() {
		StringBuffer sb = new StringBuffer("Connected clients:\n");
		if (connectedClients.isEmpty()) {
			sb.append("none...");
		}
		else {
			sb.append(CollectionSupport.format(new ArrayList<Connection>(connectedClients), "\n"));
		}
		return sb.toString();
	}



	/**
	 * Starts the service. Instantiates a server socket and starts a thread that
	 * handles incoming client connections.
	 */
	public void start() throws ConfigurationException {
		if(connectionFactory == null) {
			throw new ConfigurationException("can not start without client socket factory");
		}
		try {
			server = new ServerSocket(port);
		}
		catch (IOException e) {
			System.out.println(new LogEntry(e.getMessage(), e));
			throw new ConfigurationException("Cannot start server at port " + port, e);
		}
		serverThread = new Thread(this);
		serverThread.start();
	}


	/**
	 * Contains the loop that waits for and handles incoming connections.
	 */
	public void run() {

		try {
			while (true) {
				//server thread blocks until a client connects
				Socket socket = server.accept();

				System.out.println(new LogEntry("client ("  + socket.getInetAddress().getHostAddress() + ") attempts to connect..."));
				Connection c = establishConnection(socket);
				updateConnectedClients(c);
			}
		}
		catch (IOException ioe) {
			System.out.println(new LogEntry("server forced to stop with message " + ioe.getClass().getName() + ' ' + ioe.getMessage()));
		}
		catch (Throwable t) {
			System.out.println(new LogEntry(Level.CRITICAL, "server forced to stop with message " + t.getClass().getName() + ' ' + t.getMessage(), t));
		}
	}

	private void updateConnectedClients(Connection c) {
		synchronized(connectedClients) {
			connectedClients.add(c);
			//a little housekeeping
			for (Connection client : new HashSet<Connection>(connectedClients)) {
				if(client.isClosed()) {
					connectedClients.remove(client);
				}
			}
		}
	}

	private Connection establishConnection(Socket socket) throws IOException {
		//configure client socket
		socket.setSoLinger(soLingerActive, soLingerTime);
		socket.setSoTimeout(soTimeout);//apparently this affects br.readLine
		socket.setKeepAlive(keepAlive);

		//create client process which handles all I/O
		Connection c = connectionFactory.createConnection(socket);
		System.out.println(new LogEntry("client connection established"));
		return c;
	}


	/**
	 */
	public boolean isStarted() {
		return (serverThread != null);
	}

	/**
	 * Stops this component. Closes the server socket, which stops the incoming connection exceptionHandler as well.
	 */
	public void stop() {
		if (serverThread != null) {
			try {
				//close and force IOException in server process
				server.close();
				//server thread will end as well
			}
			catch (IOException ioe) {
				System.out.println(new LogEntry(Level.CRITICAL, ioe.getMessage(), ioe));
			}
		}

		//close client connections as well
		for (Connection client : new HashSet<Connection>(connectedClients)) {
			if(!client.isClosed()) {
				client.close("server shut down...");
			}
		}
	}


	/**
	 * Initializes this component.
	 * Properties:
	 * <ul>
	 * <li>port: tcp/ip port to connect to (default: 8888)</li>
	 * <li>keep_alive: keep alive value for client socket (default: false)</li>
	 * <li>so_timeout: so timeout value for client socket(default: 0)</li>
	 * <li>so_linger_active: so linger active value for client socket (default: false)</li>
	 * <li>so_linger_time: so linger value for client socket (default: 10)</li>
	 * <li>timeout: client time out value in seconds, 0 means no time out (default: 900)</li>
	 * </ul>
	 *
	 * @throws ConfigurationException if the interpreter class cannot be instantiated
	 */
	public void setProperties(Properties properties) throws ConfigurationException {

		port = Converter.convertToInteger(properties.getProperty("port", "" + port));
		keepAlive = Converter.convertToBoolean(properties.getProperty("keep_alive", "" + keepAlive));
		soTimeout = Converter.convertToInteger(properties.getProperty("so_timeout", "" + soTimeout));//ms
		timeout = Converter.convertToInteger(properties.getProperty("timeout", "" + timeout));//s
		soLingerActive = Converter.convertToBoolean(properties.getProperty("so_linger_active", "" + soLingerActive));
		soLingerTime = Converter.convertToInteger(properties.getProperty("so_linger_time", "" + soLingerTime));//s
	}
}
