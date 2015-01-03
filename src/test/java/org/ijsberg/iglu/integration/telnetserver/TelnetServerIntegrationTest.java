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

import org.ijsberg.iglu.configuration.Cluster;
import org.ijsberg.iglu.configuration.module.BasicAssembly;
import org.ijsberg.iglu.configuration.module.StandardCluster;
import org.ijsberg.iglu.configuration.module.StandardComponent;
import org.ijsberg.iglu.sample.configuration.TestObject;
import org.ijsberg.iglu.server.connection.socket.module.StandardSocketServer;
import org.ijsberg.iglu.server.telnet.ConfigurationInvocationConnectionFactory;
import org.junit.Test;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;


/**
 */
public class TelnetServerIntegrationTest extends BasicAssembly {

	private HashMap<String, Cluster> clusterMap = new HashMap<String, Cluster>();
	private TestObject testObject;

	@Test
	public void testTelnetServer() throws Exception {

		initializeAssembly();

		StandardSocketServer socketServer = new StandardSocketServer();

		ConfigurationInvocationConnectionFactory connectionFactory = new ConfigurationInvocationConnectionFactory(this);
		socketServer.setConnectionFactory(connectionFactory);
		socketServer.start();

		Socket socket = new Socket("localhost", 17623);
		Thread.sleep(250);
		assertTrue(socket.isConnected());

		socket.getOutputStream().write("myCluster.myModule.setPrefix(\"Hello \")\r".getBytes());
		//give async process time to finish
		Thread.sleep(250);

		assertEquals("Hello world!", testObject.getMessage("world!"));

		Thread.sleep(100);
		socket.close();

		socketServer.stop();
	}

	private void initializeAssembly() {
		Cluster cluster = new StandardCluster();
		testObject = new TestObject("Hi ");
		cluster.connect("myModule", new StandardComponent(testObject));
		clusterMap.put("myCluster", cluster);
	}

	@Override
	public Map<String, Cluster> getClusters() {
		return clusterMap;
	}

	@Override
	public Cluster getCoreCluster() {
		return null;
	}

	@Override
	public void initialize(String[] args) {
	}
}
