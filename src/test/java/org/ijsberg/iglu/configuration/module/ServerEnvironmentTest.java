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

package org.ijsberg.iglu.configuration.module;

import org.ijsberg.iglu.configuration.ConfigurationException;
import org.ijsberg.iglu.logging.DummyOutputStream;
import org.junit.Test;

import java.io.PrintStream;

import static org.junit.Assert.*;


public class ServerEnvironmentTest {

	@Test
	public void testInitialize() throws Exception {
		ServerEnvironment env;// = new ServerEnvironment();
		try {
			env = new ServerEnvironment();
			fail("ConfigurationException expected");
		} catch (ConfigurationException expected) {
		}
		try {
			env = new ServerEnvironment("org.iglu.BogusClass");
			fail("InstantiationException expected");
		} catch (InstantiationException expected) {
		}

		env = new ServerEnvironment("org.ijsberg.iglu.sample.configuration.TestAssembly");
		assertNotNull(env.getAssembly());

		try {
			env = new ServerEnvironment("org.ijsberg.iglu.sample.configuration.TestAssembly", "-xcl");
			fail("ConfigurationException expected");
		} catch (ConfigurationException expected) {
		}

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
