/*
 * Copyright 2011-2014 Jeroen Meetsma - IJsberg Automatisering BV
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

import org.ijsberg.iglu.util.misc.StringSupport;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 */
public class LogEntry implements Serializable {
	private Level level = Level.DEBUG;
	private String message;
	private Serializable data;

	public static String DEFAULT_DATE_FORMAT = "yyyyMMdd HH:mm:ss.SSS";
	private long timeInMillis;

	/*
	public LogEntry(Serializable data) {
		timeInMillis = System.currentTimeMillis();
		this.data = data;
		this.message = ""; //StringSupport.trim(data.toString(), 20, "...");
	}
    */
	public LogEntry(Throwable data) {
		timeInMillis = System.currentTimeMillis();
		this.data = data;
		this.message = data.getMessage();
	}

	public LogEntry(String message) {
		timeInMillis = System.currentTimeMillis();
		this.message = message;
	}

	public LogEntry(String message, Serializable data) {
		this(message);
		this.data = data;
	}

	public LogEntry(Level level, String message) {
		this(message);
		this.level = level;
	}

	public LogEntry(Level level, String message, Serializable data) {
		this(message, data);
		this.level = level;
	}

	/**
	 * Provides a proper default log entry if further handling is absent.
	 *
	 * @return
	 */
	public String toString() {
		StringBuffer retval = new StringBuffer(level.getShortDescription() + " " +
				new SimpleDateFormat(DEFAULT_DATE_FORMAT).format(new Date(timeInMillis)) +
				(message != null ? " " + message : "") + (data != null ? "\n" +
				(data instanceof Throwable ? "" : data) + "\n" : ""));

		if (data instanceof Throwable) {
			Throwable cause = (Throwable) data;
			while (cause != null) {
				retval.append("\n" + StringSupport.getRootStackTrace(cause, 20) + "\n");
				cause = cause.getCause();
			}
		}


		return retval.toString();
		//TODO make stacktracedepth configurable
	}


	public Level getLevel() {
		return level;
	}

	public String getMessage() {
		return message;
	}

	public Serializable getData() {
		return data;
	}

	public long getTimeInMillis() {
		return timeInMillis;
	}
}
