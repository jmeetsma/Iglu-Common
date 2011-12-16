package org.ijsberg.iglu.configuration.module;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.PrintStream;

import org.ijsberg.iglu.configuration.ConfigurationException;
import org.ijsberg.iglu.logging.DummyOutputStream;
import org.junit.Test;


public class ServerEnvironmentTest {

	@Test
	public void testInitialize() throws Exception {
		ServerEnvironment env;// = new ServerEnvironment();
		try {
			env = new ServerEnvironment();
			fail("ArrayIndexOutOfBoundsException expected");
		} catch (ArrayIndexOutOfBoundsException expected) {}
		try {
			env = new ServerEnvironment("org.iglu.BogusClass");
			fail("InstantiationException expected");
		} catch (InstantiationException expected) {}
		
		env = new ServerEnvironment("org.ijsberg.iglu.sample.configuration.TestAssembly");
		assertNotNull(env.getAssembly());
		
		try {
			env = new ServerEnvironment("org.ijsberg.iglu.sample.configuration.TestAssembly", "-xcl");
			fail("ConfigurationException expected");
		} catch (ConfigurationException expected) {}

		env = new ServerEnvironment("org.ijsberg.iglu.sample.configuration.TestAssembly", "-xcl", System.getProperty("java.class.path"));
		assertNotNull(env.getAssembly());

		assertEquals("org.ijsberg.iglu.configuration.classloading.ExtendedClassPathClassLoader", env.getAssembly().getClass().getClassLoader().getClass().getName());
		

	}
	
	@Test
	public void testMain() throws Exception {
		DummyOutputStream dummyStream = new DummyOutputStream();
		System.setOut(new PrintStream(dummyStream));
		ServerEnvironment.main(new String[0]);
		assertTrue(dummyStream.getLastOutput().startsWith("Usage:"));
	}

}
