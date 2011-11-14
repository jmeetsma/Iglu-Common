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

package org.ijsberg.iglu.runtime.module;

import org.ijsberg.iglu.logging.LogEntry;

public class ServerEnvironment extends ModuleStarter implements Runnable {

	private Thread shutdownHook;
	private boolean isRunning;

	public ServerEnvironment() {
		super();
		shutdownHook = new Thread(new ShutdownProcess());
		Runtime.getRuntime().addShutdownHook(shutdownHook);
	}


	/**
	 * Performs a (forced) shutdown sequence.
	 */
	private class ShutdownProcess implements Runnable {

		/**
		 * Invokes shutdown when startup is completed.
		 */
		public void run() {
			System.out.println(new LogEntry("starting " + (isRunning ? "forced" : "")
					+ " application shutdown process..."));
			if (isStarted) {
				try {
					stop();
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
			System.out
					.println(new LogEntry("Application shutdown process completed..."));
		}
	}

	@Override
	public synchronized void start() {
		super.start();
		if (!isRunning) {
			new Thread(this).start();
		}
	}

	@Override
	public synchronized void stop() {
		super.stop();
		isRunning = false;
	}


	public void run() {
		isRunning = true;
		while (isRunning) {
			try {
				Thread.sleep(333);
			} catch (InterruptedException e) {
				if (isStarted) {
					System.out.println(new LogEntry("Keep-alive thread interrupted..."));
					stop();
				}
			}
		}
	}

}
