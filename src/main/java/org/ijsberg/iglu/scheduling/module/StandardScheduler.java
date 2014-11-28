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
package org.ijsberg.iglu.scheduling.module;

import org.ijsberg.iglu.configuration.Startable;
import org.ijsberg.iglu.logging.Level;
import org.ijsberg.iglu.logging.LogEntry;
import org.ijsberg.iglu.scheduling.Pageable;
import org.ijsberg.iglu.scheduling.Scheduler;
import org.ijsberg.iglu.util.execution.Executable;
import org.ijsberg.iglu.util.misc.StringSupport;
import org.ijsberg.iglu.util.time.SchedulingSupport;
import org.ijsberg.iglu.util.time.TimeSupport;

import java.lang.reflect.UndeclaredThrowableException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

/**
 * Pages a number of objects once in a while.
 */
public class StandardScheduler implements Runnable, Startable, Scheduler {
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
	//proxies will get registered, the toString() method reflects the actual component
	protected ArrayList<String> pagedSystemObjectNames = new ArrayList();

	private boolean runAsync;

//	private Request initialRequest;

	/**
	 * Default constructor.
	 */
	public StandardScheduler() {
	}


	/**
	 * @return an overview of registered pageables
	 */
	@Override
	public String getReport() {
		StringBuffer sb = new StringBuffer("Registered clients:\n");
		synchronized (pagedSystems) {
			Iterator i = pagedSystems.iterator();
			while (i.hasNext()) {
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
	public void start() {
		if ((schedulerThread == null || !schedulerThread.isAlive()) && interval > 0) {
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
	public void stop() {
		if (schedulerThread != null) {
			schedulerThread.interrupt();
			try {
				schedulerThread.join(1000);
			} catch (InterruptedException ie) {
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
	public void setProperties(Properties properties) {
		interval = Integer.valueOf(properties.getProperty("interval", "" + interval));
	}


	/**
	 * Registers pageables.
	 * If the pageable has specified a page interval > 0, it will be paged regularly.
	 *
	 * @param pageable
	 */
	public void register(Pageable pageable) {
		if (!pagedSystemObjectNames.contains(pageable.toString())) {
			if (pageable.getPageIntervalInMinutes() <= 0) {
				System.out.println(new LogEntry(Level.VERBOSE, "scheduler will not page " + StringSupport.trim(pageable.toString() + "'", 80, "...") + ": interval in minutes (" + pageable.getPageIntervalInMinutes() + ") is not valid"));
			} else {
				System.out.println(new LogEntry(Level.VERBOSE, "scheduler will page " + StringSupport.trim(pageable.toString() + "'", 80, "...") + " every " + pageable.getPageIntervalInMinutes() + " minute(s)"));
			}
			synchronized (pagedSystems) {
				pagedSystems.add(pageable);
				pagedSystemObjectNames.add(pageable.toString());
			}
		} else {
			System.out.println(new LogEntry("pageable " + pageable + " already registered in scheduler"));
		}
	}


	/**
	 * Code executed by scheduler thread.
	 */
	public void run() {
		//register the current application for this thread
		// in case a subsystem logs to the environment
//		Environment.setCurrentApplication(application);
		currentState = SCHEDULER_WAIT;

		long currentTime = System.currentTimeMillis();
		//long officialTime = TimeSupport.roundToMinute(currentTime);// + totalOffsetInMillis;

		while (currentState == SCHEDULER_WAIT) {
			try {
				long interval2 = SchedulingSupport.getTimeTillIntervalStart(System.currentTimeMillis(), interval);
				//prevent having two runs in one heartbeat
				if (interval2 < TimeSupport.SECOND_IN_MS) {
					interval2 += interval * TimeSupport.MINUTE_IN_MS;
				}
				Thread.sleep(interval2);
			} catch (InterruptedException ie) {
				System.out.println(new LogEntry(Level.CRITICAL, ("scheduler interrupted...")));
				currentState = SCHEDULER_HALT;
			}
			if (currentState == SCHEDULER_WAIT) {
				currentState = SCHEDULER_BUSY;
				//do work (async in future)
				currentTime = System.currentTimeMillis();
				final long officialTime = TimeSupport.roundToMinute(currentTime);// + totalOffsetInMillis;

				synchronized (pagedSystems) {
					Iterator i = pagedSystems.iterator();
					while (i.hasNext()) {
						final Pageable pageable = (Pageable) i.next();
						//check if intervals within limits
						if (pageable.getPageIntervalInMinutes() <= 0) {
							//log("scheduler can not page " + StringSupport.trim(pageable.toString() + "'", 50, "...") + ": interval in minutes (" + pageable.getPageIntervalInMinutes() + ") is not valid");
						} else if (SchedulingSupport.isWithinMinuteOfIntervalStart(officialTime, pageable.getPageIntervalInMinutes(), pageable.getPageOffsetInMinutes()) && pageable.isStarted()) {
							System.out.println(new LogEntry("scheduler about to page " + StringSupport.trim(pageable.toString() + "'", 80, "...")));
							//the page method is invoked by a system session
							//  so a developer may try to use it to outflank the security system
							//another risk is that the invoked method consumes too much time

							if(runAsync) {
								new Executable() {
									public Object execute() {
										try {
											pageable.onPageEvent(officialTime);
										} catch (Exception e) {//pageable is not a trusted component
											//TODO keep history
											System.out.println(new LogEntry(Level.CRITICAL, "exception while paging pageable '" + StringSupport.trim(pageable.toString() + "'", 80, "..."), e));
										}
										return null;
									}
								}.executeAsync();
							} else {
								try {
									pageable.onPageEvent(officialTime);
								} catch (UndeclaredThrowableException e) {
									System.out.println(new LogEntry(Level.CRITICAL, "undeclared exception while paging pageable '" + StringSupport.trim(pageable.toString() + "'", 80, "..."), e.getCause()));
								} catch (Exception e) {//pageable is not a trusted component
									System.out.println(new LogEntry(Level.CRITICAL, "exception while paging pageable '" + StringSupport.trim(pageable.toString() + "'", 80, "..."), e));
								}
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
