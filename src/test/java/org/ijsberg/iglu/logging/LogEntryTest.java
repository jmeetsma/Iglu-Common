package org.ijsberg.iglu.logging;

import org.ijsberg.iglu.logging.LogEntry;
import org.ijsberg.iglu.logging.Level;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 */
public class LogEntryTest {

	@Test
	public void testToString() throws Exception {
		LogEntry entry = new LogEntry("test 1");
		assertEquals((double)System.currentTimeMillis(), entry.getTimeInMillis(), 500);
		assertEquals("DBG " + getFormattedTimestamp(entry) + " test 1", entry.toString());

		entry = new LogEntry(Level.VERBOSE, "test 2");
		assertEquals("VBS " + getFormattedTimestamp(entry) + " test 2", entry.toString());

		entry = new LogEntry(Level.CRITICAL, "test 3", "bite me");
		assertEquals("CRT " + getFormattedTimestamp(entry) + " test 3\nbite me\n", entry.toString());
	}

	private String getFormattedTimestamp(LogEntry entry) {
		String formattedTimeStamp = new SimpleDateFormat(
				LogEntry.DEFAULT_DATE_FORMAT).format(new Date(entry.getTimeInMillis()));
		return formattedTimeStamp;
	}

}
