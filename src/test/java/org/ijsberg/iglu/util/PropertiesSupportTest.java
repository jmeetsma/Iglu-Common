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

package org.ijsberg.iglu.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Properties;

import org.ijsberg.iglu.util.properties.PropertiesSupport;
import org.junit.Test;

/**
 */
public class PropertiesSupportTest {

	@Test
	public void testGetSubsection() throws Exception {
	}

	@Test
	public void testGetSubsectionKeys() throws Exception {
		//Properties
	}

	@Test
	public void testGetSubsections() throws Exception {
		//Properties
	}

	@Test
	public void testGetSubsectionsForSectionKey() throws Exception {
		//Properties properties, String sectionkey
	}
	
	@Test
	public void testGetCommandLineProperties() {
		
		Properties properties = PropertiesSupport.getCommandLineProperties("-test", "true");
		assertEquals(1, properties.size());
		assertEquals("true", properties.getProperty("test"));

		properties = PropertiesSupport.getCommandLineProperties("-test", "true", "-key", "value");
		assertEquals(2, properties.size());
		assertEquals("true", properties.getProperty("test"));
		assertEquals("value", properties.getProperty("key"));

		properties = PropertiesSupport.getCommandLineProperties("-test", "true", "-key");
		assertEquals(2, properties.size());
		assertEquals("true", properties.getProperty("test"));
		assertEquals("", properties.getProperty("key"));

		properties = PropertiesSupport.getCommandLineProperties("-test", "-key", "value");
		assertEquals(2, properties.size());
		assertEquals("", properties.getProperty("test"));
		assertEquals("value", properties.getProperty("key"));

		properties = PropertiesSupport.getCommandLineProperties("-test", "true", "-key", "value", "dummy");
		assertEquals(2, properties.size());
		assertEquals("true", properties.getProperty("test"));
		assertEquals("value", properties.getProperty("key"));

		properties = PropertiesSupport.getCommandLineProperties("dummy1", "-test", "true", "-key", "value", "dummy2");
		assertEquals(2, properties.size());
		assertEquals("true", properties.getProperty("test"));
		assertEquals("value", properties.getProperty("key"));
	}


}
