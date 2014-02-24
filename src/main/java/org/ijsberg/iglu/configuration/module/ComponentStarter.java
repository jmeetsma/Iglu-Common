/*
 * Copyright 2011-2013 Jeroen Meetsma - IJsberg
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

package org.ijsberg.iglu.configuration.module;

import org.ijsberg.iglu.configuration.Startable;
import org.ijsberg.iglu.logging.Level;
import org.ijsberg.iglu.logging.LogEntry;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * If ComponentStarter is added to a cluster, it will register Startable components.
 * Registered Startables can be controlled by ComponentStarter instead of having
 * to start and stop them separately.
 *
 * @author jmeetsma
 * @see Startable
 */
public class ComponentStarter implements Startable {

	protected boolean isStarted = false;
	protected Map<Integer, Startable> registeredStartables = new LinkedHashMap<Integer, Startable>();

	public synchronized void register(Startable startable) {

		System.out.println(new LogEntry("registering " + startable + " with " + this));
		System.out.flush();
		if (isStarted && !startable.isStarted()) {
			startable.start();
		}
		registeredStartables.put(startable.toString().hashCode(), startable);
	}

	public synchronized void unregister(Startable startable) {
		registeredStartables.remove(startable.toString().hashCode());
	}


	public synchronized void start() {
		if (!isStarted) {
			isStarted = true;
			for (Startable startable : registeredStartables.values()) {
				if (!startable.isStarted()) {
					try {
						System.out.println(new LogEntry(Level.VERBOSE, "starting component " + startable));
						startable.start();
					} catch (Exception e) {
						System.out.println(new LogEntry(Level.CRITICAL, "unable to start component " + startable +
								" with message: " + e.getMessage(), e));
						isStarted = false;
						return;
					}
				}
			}
		}
	}

	public synchronized boolean isStarted() {
		return isStarted;
	}

	public synchronized void stop() {
		isStarted = false;

		Startable[] startables = registeredStartables.values().toArray(new Startable[0]);
		for (int i = startables.length - 1; i >= 0; i--) {
			if (startables[i].isStarted()) {
				System.out.println(new LogEntry(Level.VERBOSE, "stopping component " + startables[i]));
				startables[i].stop();
			}
		}
	}

}
