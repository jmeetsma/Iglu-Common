package org.ijsberg.iglu.logging;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 */
public class LogEntry implements Serializable
{
	private Level level = Level.DEBUG;
	private String message;
	private Serializable data;

	public static String DEFAULT_DATE_FORMAT = "yyyyMMdd HH:mm:ss.SSS";
	private long timeInMillis;

	public LogEntry(String message)
	{
		timeInMillis = System.currentTimeMillis();
		this.message = message;
	}

	public LogEntry(String message, Serializable data)
	{
		this(message);
		this.data = data;
	}

	public LogEntry(Level level, String message)
	{
		this(message);
		this.level = level;
	}

	public LogEntry(Level level, String message, Serializable data)
	{
		this(message, data);
		this.level = level;
	}

	/**
	 * Provides a proper default log entry if further handling is absent.
	 * 
	 * @return
	 */
	public String toString() {
		return level.getShortDescription() + " " +
				new SimpleDateFormat(DEFAULT_DATE_FORMAT).format(new Date(timeInMillis)) +
				(message != null ? " " + message : "") + (data != null ? "\n" + data + "\n" : "");
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
