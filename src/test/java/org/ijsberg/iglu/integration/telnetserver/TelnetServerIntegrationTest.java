package org.ijsberg.iglu.integration.telnetserver;

import org.ijsberg.iglu.Cluster;
import org.ijsberg.iglu.Configuration;
import org.ijsberg.iglu.configuration.StandardCluster;
import org.ijsberg.iglu.configuration.StandardComponent;
import org.ijsberg.iglu.configuration.TestObject;
import org.ijsberg.iglu.server.connection.invocation.ConfigurationInvocationConnectionFactory;
import org.ijsberg.iglu.server.connection.socket.module.StandardSocketServer;
import org.junit.Test;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;


/**
 */
public class TelnetServerIntegrationTest implements Configuration {

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
}
