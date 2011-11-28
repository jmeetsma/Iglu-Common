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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

/**
 */
public class StandardSocketServerTest
{
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
		} catch (ConfigurationException expected) {}


		Socket socket = null;
		try {
			socket = new Socket("localhost", 17623);
			fail("ConnectException expected");
		} catch (ConnectException expected) {}


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
