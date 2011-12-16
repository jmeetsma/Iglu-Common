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

package org.ijsberg.iglu.logging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.PrintStream;
import java.math.BigDecimal;

import org.junit.After;
import org.junit.Test;

/**
 */
public class LogPrintStreamTest {

	private PrintStream systemOut = System.out;

	@Test
	public void testPrintToAlternateStream() throws Exception {
		DummyOutputStream dummyStream = new DummyOutputStream();
		System.setOut(new PrintStream(dummyStream));

		System.out.print("test 1");
		assertEquals("test 1", dummyStream.getLastOutput());

		System.out.print(27);
		assertEquals("27", dummyStream.getLastOutput());

		System.out.print(new BigDecimal(31));
		assertEquals("31", dummyStream.getLastOutput());
	}

	@Test
	public void testPrint() throws Exception {
		DummyOutputStream dummyStream = new DummyOutputStream();
		DummyLogger dummyLogger = new DummyLogger();
		LogPrintStream logPrintStream = new LogPrintStream(dummyStream, dummyLogger);
		System.setOut(logPrintStream);

		System.out.print("test 1");
		assertEquals("test 1", dummyStream.getLastOutput());

		System.out.print(27);
		assertEquals("27", dummyStream.getLastOutput());

		System.out.print(new BigDecimal(31));
		assertEquals("31", dummyStream.getLastOutput());

		assertNull(dummyLogger.getLastEntry());

		System.out.print(new LogEntry("test 1"));
		assertEquals("test 1", dummyLogger.getLastEntry().getMessage());

	}

	@Test
	public void testPrintln() throws Exception {
		DummyOutputStream dummyStream = new DummyOutputStream();
		DummyLogger dummyLogger = new DummyLogger();
		LogPrintStream logPrintStream = new LogPrintStream(dummyStream, dummyLogger);
		System.setOut(logPrintStream);

		System.out.println(new BigDecimal(31));
		assertEquals("31", dummyStream.getLastOutput().trim());

		System.out.println(new LogEntry("test 1"));
		assertEquals("test 1", dummyLogger.getLastEntry().getMessage());
	}

	@After
	public void tearDown() {
		System.out.print(new BigDecimal(22));
		System.setOut(systemOut);
	}
}
