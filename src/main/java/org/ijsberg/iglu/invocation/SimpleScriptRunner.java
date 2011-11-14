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

package org.ijsberg.iglu.invocation;

import org.ijsberg.iglu.ConfigurationException;
import org.ijsberg.iglu.Configuration;
import org.ijsberg.iglu.logging.Level;
import org.ijsberg.iglu.logging.LogEntry;
import org.ijsberg.iglu.util.collection.CollectionSupport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 */
public class SimpleScriptRunner extends CommandLineProcessor {
	private String script = "\n\n";
	private int pageIntervalInMinutes = 0;
	private int pageOffsetInMinutes = 0;

	public SimpleScriptRunner(Configuration configuration) {
		super(configuration);
	}


	/**
	 * Runs Iglu-style script.
	 */
	public void runScript() {
		if (script.trim().length() > 0) {
			BufferedReader reader = new BufferedReader(new StringReader(script));
			try {
				List results = runScript(reader);
				System.out.println(new LogEntry(Level.CRITICAL, "completed run of script in device '\" + this.deviceId + \"'\""));
			}
			catch (Throwable t) {
				throw new ConfigurationException("failed to complete run of script", t);
			}
		}
	}

	/**
	 * @param reader
	 * @return
	 */
	protected List runScript(BufferedReader reader) throws Throwable {
		List results = new ArrayList();
		String line;
		while ((line = readLine(reader)) != null) {
			Object result = processCommandLine(line);
			results.add(result);
		}
		return results;
	}


	/**
	 * @param reader
	 * @return
	 */
	private String readLine(BufferedReader reader) {
		try {
			return reader.readLine();
		}
		catch (IOException ioe) {
			//should be impossible
			throw new ConfigurationException("unable to read line from buffer");
		}
	}

	/**
	 * Expects a property 'script', containing the actual script to be run.
	 * Specify 'page_interval_in_minutes' and 'page_offset_in_minutes'
	 * to have script run periodically.
	 */
	public void setProperties(Properties properties) {
		script = properties.getProperty("script", script);
		pageIntervalInMinutes = Integer.parseInt(properties.getProperty("page_interval_in_minutes",
				"" + pageIntervalInMinutes));
		pageOffsetInMinutes = Integer.parseInt(properties.getProperty("page_offset_in_minutes",
				"" + pageOffsetInMinutes));
	}

	/**
	 * Does nothing.
	 */
	protected void _start() {
	}

	/**
	 * Does nothing.
	 */
	protected void _stop() {
	}

	/**
	 * @return
	 */
	public int getPageIntervalInMinutes() {
		return pageIntervalInMinutes;
	}

	/**
	 * @return
	 */
	public int getPageOffsetInMinutes() {
		return pageOffsetInMinutes;
	}

	/**
	 * @param officialTime
	 */
	public void onPageEvent(long officialTime) {
		runScript();
	}
}
