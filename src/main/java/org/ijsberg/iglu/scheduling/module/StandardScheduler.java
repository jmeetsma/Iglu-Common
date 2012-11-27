/* =======================================================================
 * Copyright (c) 2003-2010 IJsberg Automatisering BV. All rights reserved.
 * Redistribution and use of this code are permitted provided that the
 * conditions of the Iglu License are met.
 * The license can be found in org.ijsberg.iglu.StandardApplication.java
 * and is also published on http://iglu.ijsberg.org/LICENSE.
 * =======================================================================
 */
package org.ijsberg.iglu.scheduling.module;

import org.ijsberg.iglu.configuration.Startable;
import org.ijsberg.iglu.logging.Level;
import org.ijsberg.iglu.logging.LogEntry;
import org.ijsberg.iglu.scheduling.Pageable;
import org.ijsberg.iglu.scheduling.Scheduler;
import org.ijsberg.iglu.util.misc.StringSupport;
import org.ijsberg.iglu.util.time.SchedulingSupport;
import org.ijsberg.iglu.util.time.TimeSupport;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

/**
 * Pages a number of objects once in a while.
 */
public class StandardScheduler implements Runnable, Startable, Scheduler
{
	public static SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS");

	protected Thread schedulerThread;
	public static final int SCHEDULER_HALT = 0;
	public static final int SCHEDULER_WAIT = 1;
	public static final int SCHEDULER_BUSY = 2;
	public static final int SCHEDULER_REQUEST = 3;

	protected int interval = 1;//in minutes
	protected int currentState = SCHEDULER_HALT;
	protected long lastCall;

	protected ArrayList pagedSystems = new ArrayList();

//	private Request initialRequest;

	/**
	 * Default constructor.
	 */
	public StandardScheduler()
	{
	}


	/**
	 * @return an overview of registered pageables
	 */
	public String getReport()
	{
		StringBuffer sb = new StringBuffer("Registered clients:\n");
		synchronized (pagedSystems)
		{
			Iterator i = pagedSystems.iterator();
			while (i.hasNext())
			{
				Pageable p = (Pageable) i.next();
				sb.append(p.toString() + '(' + p.getClass().getName() + ") ");
				sb.append("interval:" + p.getPageIntervalInMinutes() + " offset:" + p.getPageOffsetInMinutes());
				sb.append("\n");
			}
			sb.append("\n");
			sb.append("state: " + currentState + "\n");
			return sb.toString();
		}
	}

	/**
	 * Starts scheduler thread that pages pageables.
	 */
	public void start()
	{
		if ((schedulerThread == null || !schedulerThread.isAlive()) && interval > 0)
		{
			schedulerThread = new Thread(this);
			//act as internal request to be able to bind internal process
//			Request currentRequest = application.getCurrentRequest();
//			currentRequest.startRepresentingRequest(initialRequest);
//			application.bindInternalProcess(schedulerThread, "scheduler");
//			currentRequest.stopRepresentingRequest();
			schedulerThread.start();
			System.out.println(new LogEntry("scheduler thread started..."));
		}
	}

	/**
	 * Stops scheduler thread.
	 */
	public void stop()
	{
		if (schedulerThread != null)
		{
			schedulerThread.interrupt();
			try
			{
				schedulerThread.join(1000);
			}
			catch (InterruptedException ie)
			{
			}
//			application.releaseRequest(schedulerThread);
			schedulerThread = null;

			System.out.println(new LogEntry("scheduler thread stopped..."));
		}
	}
	
	public boolean isStarted() {
		return currentState != SCHEDULER_HALT;
	}

	/**
	 * Initializes scheduler.
	 */
	public void setProperties(Properties properties)
	{
		interval = Integer.valueOf(properties.getProperty("interval", "" + interval));
	}


	/**
	 * Registers pageables.
	 * If the pageable has specified a page interval > 0, it will be paged regularly.
	 *
	 * @param pageable
	 */
	public void register(Pageable pageable)
	{
		if(pageable.getPageIntervalInMinutes() <= 0)
		{
			System.out.println(new LogEntry("scheduler will not page " + StringSupport.trim(pageable.toString() + "'", 50, "...") + ": interval in minutes (" + pageable.getPageIntervalInMinutes() + ") is not valid"));
		}
		else
		{
			System.out.println(new LogEntry("scheduler will page " + StringSupport.trim(pageable.toString() + "'", 50, "...") + " every " + pageable.getPageIntervalInMinutes() + " minute(s)"));
		}
		synchronized (pagedSystems)
		{
			pagedSystems.add(pageable);
		}
	}


	/**
	 * Code executed by scheduler thread.
	 */
	public void run()
	{
		//register the current application for this thread
		// in case a subsystem logs to the environment
//		Environment.setCurrentApplication(application);
		currentState = SCHEDULER_WAIT;

		long currentTime = System.currentTimeMillis();
		long officialTime = TimeSupport.roundToMinute(currentTime);// + totalOffsetInMillis;

		while (currentState == SCHEDULER_WAIT)
		{
			try
			{
				long interval2 = SchedulingSupport.getTimeTillIntervalStart(System.currentTimeMillis(), interval);
				//prevent having two runs in one heartbeat
				if (interval2 < TimeSupport.SECOND_IN_MS)
				{
					interval2 += interval * TimeSupport.MINUTE_IN_MS;
				}
				Thread.sleep(interval2);
			}
			catch (InterruptedException ie)
			{
				System.out.println(new LogEntry(("scheduler interrupted...")));
				currentState = SCHEDULER_HALT;
			}
			if (currentState == SCHEDULER_WAIT)
			{
				currentState = SCHEDULER_BUSY;
				//do work (async in future)
				currentTime = System.currentTimeMillis();
				officialTime = TimeSupport.roundToMinute(currentTime);// + totalOffsetInMillis;

				synchronized (pagedSystems)
				{
					Iterator i = pagedSystems.iterator();
					while (i.hasNext())
					{
						Pageable pageable = (Pageable) i.next();
                        //check if intervals within limits
						if(pageable.getPageIntervalInMinutes() <= 0)
						{
							//log("scheduler can not page " + StringSupport.trim(pageable.toString() + "'", 50, "...") + ": interval in minutes (" + pageable.getPageIntervalInMinutes() + ") is not valid");
						}
						else if (SchedulingSupport.isWithinMinuteOfIntervalStart(officialTime, pageable.getPageIntervalInMinutes(), pageable.getPageOffsetInMinutes()) && pageable.isActive())
						{
							try
							{
								System.out.println(new LogEntry("scheduler about to page " + StringSupport.trim(pageable.toString() + "'", 50, "...")));
								//the page method is invoked by a system session
								//  so a developer may try to use it to outflank the security system
								//another risk is that the invoked method consumes too much time
								pageable.onPageEvent(officialTime);
							}
							catch (Exception e)//pageable is not per se a trusted component
							{
								System.out.println(new LogEntry(Level.CRITICAL, "error while paging pageable '" + StringSupport.trim(pageable.toString() + "'", 50, "..."), e));
							}
						}
					}
					lastCall = officialTime;
				}
				currentState = SCHEDULER_WAIT;
			}
		}
		currentState = SCHEDULER_HALT;
	}


}
