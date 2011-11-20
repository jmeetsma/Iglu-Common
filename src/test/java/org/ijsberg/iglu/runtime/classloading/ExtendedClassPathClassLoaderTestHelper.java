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

package org.ijsberg.iglu.runtime.classloading;

import org.ijsberg.iglu.runtime.classloading.ExtendedClassPathClassLoader;

import java.io.File;
import java.io.InputStream;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertSame;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


/**
 * The compiled class is be moved to resources and loaded by the ExtendedClassPathClassLoader.
 *
 */
public class ExtendedClassPathClassLoaderTestHelper {

	private File tmpDir;

	public ExtendedClassPathClassLoaderTestHelper(File tmpDir) {
		this.tmpDir = tmpDir;
	}


	public void testGetResourceAsStream() throws Exception {

		ExtendedClassPathClassLoader classLoader = (ExtendedClassPathClassLoader)this.getClass().getClassLoader();
		InputStream input = classLoader.getResourceAsStream("users.properties");
		Properties properties = new Properties();
		properties.load(input);
		assertEquals("vikas1", properties.getProperty("vikas"));

		ResourceBundle bundle = ResourceBundle.getBundle("org.ijsberg.iglu.runtime.classloading.test");
		assertNotNull(bundle);
	}

	public void testGetResourceBundleFail() throws Exception {

		try {
			ResourceBundle.getBundle("users");
			fail("MissingResourceException expected");
		} catch(MissingResourceException expected) {}

	}

	public void testGetResourceBundle() throws Exception {

		ExtendedClassPathClassLoader classLoader = (ExtendedClassPathClassLoader)this.getClass().getClassLoader();
		ResourceBundle bundle = ResourceBundle.getBundle("users");
		assertNotNull(bundle);
		assertEquals("vikas1", bundle.getString("vikas"));
	}

}
