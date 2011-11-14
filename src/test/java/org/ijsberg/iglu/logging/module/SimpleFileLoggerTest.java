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

package org.ijsberg.iglu.logging.module;

import org.ijsberg.iglu.logging.DummyOutputStream;
import org.ijsberg.iglu.logging.Level;
import org.ijsberg.iglu.logging.LogEntry;
import org.junit.Test;

import java.io.PrintStream;

import static org.junit.Assert.*;


/**
 */
public class SimpleFileLoggerTest {

	@Test
	public void testGetStackTracePart() throws Exception {
		StackTraceElement[] stackTracePart = SimpleFileLogger.getStackTracePart(0, 1);
		stackTracePart = SimpleFileLogger.getStackTracePart(200, 5);
		assertEquals(0, stackTracePart.length);

		stackTracePart = SimpleFileLogger.getStackTracePart(3, 198);
		assertTrue(stackTracePart.length > 10);
		assertTrue(stackTracePart.length < 100);


		stackTracePart = SimpleFileLogger.getStackTracePart(1, 5);
		assertEquals(5, stackTracePart.length);
		assertEquals(getClass().getName() + ".testGetStackTracePart(" + getClass().getSimpleName() +
				".java", stackTracePart[0].toString().split(":")[0]);
	}

	@Test
	public void testFormatArray() throws Exception {
		String text = SimpleFileLogger.formatArray(new String[]{"test 1", "2", "3"});
		assertEquals("test 1\n2\n3\n", text);
	}

	@Test
	public void testLog() throws Exception {
		DummyOutputStream dummyLogStream = new DummyOutputStream();
		SimpleFileLogger logger = new SimpleFileLogger(new PrintStream(dummyLogStream));

		logger.log(new LogEntry("test 1"));
		assertEquals(" test 1", dummyLogStream.getLastOutput().trim().substring(25));
		logger.log(new LogEntry("test 2"));
		assertEquals(" test 2", dummyLogStream.getLastOutput().trim().substring(25));

	}

	@Test
	public void testLogAtLevels() throws Exception {
		DummyOutputStream dummyLogStream = new DummyOutputStream();
		SimpleFileLogger logger = new SimpleFileLogger(new PrintStream(dummyLogStream));

		logger.setLogLevel("VERBOSE");
		logger.log(new LogEntry("test 1"));
		assertNull(dummyLogStream.getLastOutput());
		logger.log(new LogEntry(Level.VERBOSE, "test 2"));
		assertEquals(" test 2", dummyLogStream.getLastOutput().trim().substring(25));

	}

	@Test
	public void testLogWithStackTrace() throws Exception {
		DummyOutputStream dummyLogStream = new DummyOutputStream();
		SimpleFileLogger logger = new SimpleFileLogger(new PrintStream(dummyLogStream));

		logger.log(new LogEntry("test 1"));
		assertEquals(32, dummyLogStream.getLastOutput().trim().length());

		logger.setEntryOriginStackTraceDepth(3);
		logger.log(new LogEntry("test 2"));
		System.out.println(dummyLogStream.getLastOutput());
		assertTrue(dummyLogStream.getLastOutput().length() > 100);
		//TODO read lines: 1 ends with "test2" other 3 start with at, first of other contains this class's name
	}

	@Test
	public void testLogWithData() throws Exception {
		DummyOutputStream dummyLogStream = new DummyOutputStream();
		SimpleFileLogger logger = new SimpleFileLogger(new PrintStream(dummyLogStream));

		logger.log(new LogEntry("test 1"));
		assertEquals(32, dummyLogStream.getLastOutput().trim().length());

		logger.log(new LogEntry("test 2", "with data..."));
		System.out.println(dummyLogStream.getLastOutput());
		assertTrue(dummyLogStream.getLastOutput().trim().length() >= 45);
		//TODO rather check lines
	}

	@Test
	public void testLogWithThrowable() throws Exception {
		DummyOutputStream dummyLogStream = new DummyOutputStream();
		SimpleFileLogger logger = new SimpleFileLogger(new PrintStream(dummyLogStream));

		logger.log(new LogEntry("test 1"));
		assertEquals(32, dummyLogStream.getLastOutput().trim().length());

		logger.log(new LogEntry("test 2", new NullPointerException("Test Null Pointer")));
		System.out.println(dummyLogStream.getLastOutput());
//		assertEquals(48, dummyLogStream.getLastOutput().length());
		//TODO rather check lines
	}

	@Test
	public void testLogWithThrowable2() throws Exception {

		SimpleFileLogger logger = new SimpleFileLogger(System.out);

		logger.log(new LogEntry("test X", new NullPointerException("Test Null Pointer")));
//		assertEquals(48, dummyLogStream.getLastOutput().length());
		//TODO rather check lines
	}

	@Test
	public void testSetLogLevel() throws Exception {
		DummyOutputStream dummyLogStream = new DummyOutputStream();
		SimpleFileLogger logger = new SimpleFileLogger(new PrintStream(dummyLogStream));
		assertEquals(0, logger.getLogLevelOrdinal());
		logger.setLogLevel("VERBOSE");
		assertEquals(1, logger.getLogLevelOrdinal());
	}


}
