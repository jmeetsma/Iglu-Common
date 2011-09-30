package org.ijsberg.iglu.logging;

/**
 *
 */
public enum Level {DEBUG, VERBOSE, CRITICAL;
	public static String[] LEVEL_DESC_ABBR = {"DBG", "VBS", "CRT"};
	public static String[] LEVEL_DESC = {"debug", "verbose", "critical"};

	public String getShortDescription() {
		return LEVEL_DESC_ABBR[ordinal()];
	}

	public String getDescription() {
		return LEVEL_DESC[ordinal()];
	}
}
