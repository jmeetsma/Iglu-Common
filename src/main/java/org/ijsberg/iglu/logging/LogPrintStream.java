package org.ijsberg.iglu.logging;

import org.ijsberg.iglu.logging.module.SimpleFileLogger;

import java.io.*;

/**
 */
public class LogPrintStream extends PrintStream
{
	Logger logger;

	public LogPrintStream(OutputStream standardStream, Logger logger){
		super(standardStream, true);
		this.logger = logger;
	}

    public void println(Object message) {
		if(message instanceof LogEntry) {
			logger.log((LogEntry)message);
		} else {
			super.println(message);
		}
    }

	public void print(Object message) {
		if(message instanceof LogEntry) {
			logger.log((LogEntry)message);
		} else {
			super.print(message);
		}
	}
}
