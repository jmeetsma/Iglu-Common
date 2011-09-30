package org.ijsberg.iglu.logging;

import org.junit.Test;
import org.junit.After;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import org.ijsberg.iglu.logging.LogPrintStream;
import org.ijsberg.iglu.logging.LogEntry;

import java.io.PrintStream;
import java.math.BigDecimal;

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
