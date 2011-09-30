package org.ijsberg.iglu.logging;

import org.ijsberg.iglu.logging.Logger;
import org.ijsberg.iglu.logging.LogEntry;

import java.util.List;
import java.util.ArrayList;

/**
 */
public class DummyLogger implements Logger {

	LogEntry lastEntry;

	public void log(LogEntry entry) {
		lastEntry = entry;
	}

	public LogEntry getLastEntry() {
		return lastEntry;
	}
}
