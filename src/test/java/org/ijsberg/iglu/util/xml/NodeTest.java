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

package org.ijsberg.iglu.util.xml;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 */
public class NodeTest {

	private static String xmlString =
			"<article name=\"test1\" type=\"2\" category=\"COMPANY INFO\">\n" +
					"\t<h3>\n" +
					"\t\t<title />\n" +
					"\t</h3>\n" +
					"\t<more>\n" +
					"\t\t<img src=\"/images/read.gif\" />\n" +
					"\t</more>\n" +
					"</article>";


	@Test
	public void testCloneFull() throws Exception {
		Node node = new Node("envelope");
		node.parse(xmlString);

		assertEquals("test1", node.getFirstNodeByNameInTree("article").getAttribute("name"));
		assertEquals("test1", node.getFirstNodeByNameInTree("article").getAttributes().get("name"));

		Node clone = node.cloneFull();

		assertEquals("test1", clone.getFirstNodeByNameInTree("article").getAttribute("name"));
		assertEquals("test1", clone.getFirstNodeByNameInTree("article").getAttributes().get("name"));
	}
}
