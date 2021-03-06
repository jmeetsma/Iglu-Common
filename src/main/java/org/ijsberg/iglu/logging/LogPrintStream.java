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

import java.io.OutputStream;
import java.io.PrintStream;

/**
 */
public class LogPrintStream extends PrintStream {
	Logger logger;

	public LogPrintStream(OutputStream standardStream, Logger logger) {
		super(standardStream, true);
		this.logger = logger;
	}

	public void println(Object message) {
		if (message instanceof LogEntry) {
			logger.log((LogEntry) message);
		} else {
			super.println(message);
		}
	}

	public void print(Object message) {
		if (message instanceof LogEntry) {
			logger.log((LogEntry) message);
		} else {
			super.print(message);
		}
	}

/*	public void println(String message) {   TODO leads to stack overflow
		logger.log(new LogEntry("STDOUT " + message));
		super.println(message);
	}

	public void print(String message) {
		logger.log(new LogEntry("STDOUT " + message));
		super.print(message);
	} */
}
