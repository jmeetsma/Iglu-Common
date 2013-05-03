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

package org.ijsberg.iglu.logging.module;

import org.ijsberg.iglu.scheduling.Pageable;
import org.ijsberg.iglu.util.time.SafeDateFormat;

import java.io.*;
import java.util.Date;
import java.util.Properties;

/**
 */
public class RotatingFileLogger extends SimpleFileLogger implements Pageable {


	private int nrofLogFilesToKeep = 7;
	private int logRotateIntervalInHours = 24;
	public static SafeDateFormat TIMESTAMP_LOGFILE_FORMAT = new SafeDateFormat("yyyy_MM_dd_HH_mm");


	public RotatingFileLogger(String fileName) {
		super(fileName);
	}



	/**
	 * @param date
	 * @return
	 */
	private String getFileName(Date date)
	{
		String dateStr = null;
		if (date != null)
		{
			dateStr = TIMESTAMP_LOGFILE_FORMAT.format(date);
		}
		return fileName + (dateStr != null ? '.' + dateStr : "") + ".log";
	}

	@Override
	public int getPageIntervalInMinutes() {
		return logRotateIntervalInHours * 60;
	}

	@Override
	public int getPageOffsetInMinutes() {
		return 0;
	}

	public void setProperties(Properties properties) {
		super.setProperties(properties);
		nrofLogFilesToKeep = Integer.parseInt(properties.getProperty("nr_log_files_to_keep", "" + nrofLogFilesToKeep));
		logRotateIntervalInHours = Integer.parseInt(properties.getProperty("rotate_interval_hours", "" + logRotateIntervalInHours));
	}

	@Override
	public void onPageEvent(long officialTime) {
		synchronized (lock) {
			stop();
			File file = new File(fileName + ".log");
			file.renameTo(new File(getFileName(new Date(officialTime))));
			openLogStream();
			start();
		}

		Date officialDate = new Date(officialTime - (nrofLogFilesToKeep * logRotateIntervalInHours * 60 * 1000));
		String destLogFileName = getFileName(officialDate);
		File obsoleteFile = new File(destLogFileName);
		if (obsoleteFile.exists())
		{
			obsoleteFile.delete();
		}

	}


}
