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

package org.ijsberg.iglu.server.connection.socket.module;

import org.ijsberg.iglu.configuration.Cluster;
import org.ijsberg.iglu.configuration.ConfigurationException;
import org.ijsberg.iglu.configuration.module.StandardCluster;
import org.ijsberg.iglu.integration.telnetserver.DummyConnectionFactory;
import org.ijsberg.iglu.server.connection.socket.module.dummy.ConnectionFactoryDummy;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.ConnectException;
import java.net.Socket;

import static junit.framework.Assert.*;

/**
 */
public class StandardSocketServerTest {
	private StandardSocketServer socketServer;
	private ConnectionFactoryDummy socketFactory;

	@Before
	public void setUp() {
	}


	@Test
	public void testTelnetServer() throws Exception {

		Cluster cluster = new StandardCluster();
		StandardSocketServer socketServer = new StandardSocketServer();

		try {
			socketServer.start();
			fail("ConfigurationException expected (client socket factory missing)");
		} catch (ConfigurationException expected) {
		}


		Socket socket = null;
		try {
			socket = new Socket("localhost", 17623);
			fail("ConnectException expected");
		} catch (ConnectException expected) {
		}


		DummyConnectionFactory clientSocketFactory = new DummyConnectionFactory();
		socketServer.setConnectionFactory(clientSocketFactory);
		socketServer.start();

		socket = new Socket("localhost", 17623);
		Thread.sleep(100);
		assertTrue(socket.isConnected());


		socket.getOutputStream().write("Anybody there?".getBytes());
		//give async process time to finish
		Thread.sleep(100);
		assertEquals("Anybody there?", clientSocketFactory.getConnections().get(0).getInput());


		Thread.sleep(100);
		socket.close();

		socketServer.stop();
	}


	@After
	public void tearDown() {
	}
}
