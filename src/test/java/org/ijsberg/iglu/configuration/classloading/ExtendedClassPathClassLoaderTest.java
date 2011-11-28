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

package org.ijsberg.iglu.configuration.classloading;

import org.ijsberg.iglu.configuration.classloading.ExtendedClassPathClassLoader;
import org.ijsberg.iglu.util.io.FileSupport;
import org.ijsberg.iglu.util.io.StreamSupport;
import org.ijsberg.iglu.util.misc.StringSupport;
import org.ijsberg.iglu.util.reflection.ReflectionSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URL;

import static junit.framework.Assert.*;

/**
 */
public class ExtendedClassPathClassLoaderTest {

	private File tmpDir;
	private String pathToResources = StringSupport.replaceAll(this.getClass().getPackage().getName(), ".", "/");

	@Before
	public void setUp() throws Exception {
		tmpDir = FileSupport.createTmpDir("Iglu-Common-test");
		new File(tmpDir.getAbsolutePath() + "/jars/").mkdirs();
		new File(tmpDir.getAbsolutePath() + "/resources/").mkdirs();
		new File(tmpDir.getAbsolutePath() + "/classes/" + pathToResources).mkdirs();

		Thread.sleep(100);

		FileSupport.copyClassLoadableResourceToFileSystem(pathToResources + "/jars/iglu-telnet-server-1.0.jar", tmpDir.getAbsolutePath() + "/jars/");
		FileSupport.copyClassLoadableResourceToFileSystem(pathToResources + "/resources/test.properties", tmpDir.getAbsolutePath() + "/resources/");
		FileSupport.copyClassLoadableResourceToFileSystem(pathToResources + "/ExtendedClassPathClassLoaderTestHelper.class", tmpDir.getAbsolutePath() + "/classes/" + pathToResources);
	}

	@After
	public void tearDown() {
		tmpDir.delete();
	}

	@Test
	public void testClassLoaderInAction() throws Exception {

		ExtendedClassPathClassLoader classLoader = new ExtendedClassPathClassLoader(tmpDir.getAbsolutePath() + "/classes");
		Object helper = ReflectionSupport.instantiateClass(classLoader, "org.ijsberg.iglu.configuration.classloading.ExtendedClassPathClassLoaderTestHelper", tmpDir);
		assertSame(classLoader, helper.getClass().getClassLoader());
	}


	@Test
	public void testFindClass() throws Exception {

		ExtendedClassPathClassLoader classLoader = new ExtendedClassPathClassLoader(tmpDir.getAbsolutePath() + "/jars");
		Class clasz = classLoader.findClass("org.ijsberg.iglu.samples.telnet.EmbeddedServer");
		assertNotNull(clasz);
		assertEquals("org.ijsberg.iglu.samples.telnet.EmbeddedServer", clasz.getName());

		try {
			classLoader.findClass("org.ijsberg.iglu.AbsentClass");
			fail("ClassNotFoundException expected");
		} catch (ClassNotFoundException expected) {};

		try {
			classLoader.findClass("org.ijsberg.iglu.util.io.FileSupport");
			fail("ClassNotFoundException expected");
		} catch (ClassNotFoundException expected) {};

		clasz = classLoader.findClass("users");
		assertNotNull(clasz);
		assertEquals("java.util.ResourceBundle", clasz.getName());

	}

	@Test
	public void testLoadClass() throws Exception {

		ExtendedClassPathClassLoader classLoader = new ExtendedClassPathClassLoader(tmpDir.getAbsolutePath() + "/jars");
		Class clasz = classLoader.loadClass("org.ijsberg.iglu.samples.telnet.EmbeddedServer");
		assertNotNull(clasz);

		try {
			classLoader.loadClass("org.ijsberg.iglu.AbsentClass");
			fail("ClassNotFoundException expected");
		} catch (ClassNotFoundException expected) {};

		clasz = classLoader.loadClass("org.ijsberg.iglu.util.io.FileSupport");
		assertNotNull(clasz);
		try {
			clasz = classLoader.loadClass("users");
			System.out.println(clasz);
			fail("ClassNotFoundException expected");
		} catch (ClassNotFoundException expected) {};


	}

	@Test
	public void testGetResource() throws Exception {
		URL url = this.getClass().getClassLoader().getResource("org/ijsberg/iglu/configuration/classloading/test.properties");
		assertTrue(url.toString().endsWith("classloading/test.properties"));

		ExtendedClassPathClassLoader classLoader = new ExtendedClassPathClassLoader(tmpDir.getAbsolutePath() + "/jars");
		url = classLoader.getResource("users.properties");
		assertTrue(url.toString().startsWith("jar:file:/"));
		assertTrue(url.toString().endsWith(".jar!/users.properties"));

		classLoader = new ExtendedClassPathClassLoader(tmpDir.getAbsolutePath() + "/resources");
		url = classLoader.getResource("test.properties");
		assertTrue(url.toString().startsWith("file:/"));
		assertTrue(url.toString().endsWith("/resources/test.properties"));

	}


	@Test
	public void testGetResourceAsStream() throws Exception {

		ExtendedClassPathClassLoader classLoader = new ExtendedClassPathClassLoader(tmpDir.getAbsolutePath() + "/jars;" + tmpDir.getAbsolutePath() + "/classes");
		Object helper = ReflectionSupport.instantiateClass(classLoader, "org.ijsberg.iglu.configuration.classloading.ExtendedClassPathClassLoaderTestHelper", tmpDir);
		ReflectionSupport.invokeMethod(helper, "testGetResourceAsStream");
	}

	@Test
	public void testGetResourceBundle() throws Exception {

		Object helper = ReflectionSupport.instantiateClass("org.ijsberg.iglu.configuration.classloading.ExtendedClassPathClassLoaderTestHelper", tmpDir);
		ReflectionSupport.invokeMethod(helper, "testGetResourceBundleFail");

		ExtendedClassPathClassLoader classLoader = new ExtendedClassPathClassLoader(tmpDir.getAbsolutePath() + "/jars;" + tmpDir.getAbsolutePath() + "/classes");
		helper = ReflectionSupport.instantiateClass(classLoader, "org.ijsberg.iglu.configuration.classloading.ExtendedClassPathClassLoaderTestHelper", tmpDir);
		ReflectionSupport.invokeMethod(helper, "testGetResourceBundle");
	}
}
