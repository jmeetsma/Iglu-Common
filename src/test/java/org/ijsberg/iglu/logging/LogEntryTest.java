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

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;

/**
 */
public class LogEntryTest {

	@Test
	public void testConstructor() throws Exception {
		LogEntry entry = new LogEntry(new Exception("test"));
		assertEquals("test", entry.getMessage());
	}

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
