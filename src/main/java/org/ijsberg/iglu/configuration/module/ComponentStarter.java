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

package org.ijsberg.iglu.configuration.module;

import java.util.HashSet;
import java.util.Set;

import org.ijsberg.iglu.configuration.Startable;
import org.ijsberg.iglu.logging.LogEntry;

/**
 * If ComponentStarter is added to a cluster, it will register Startable components.
 * Registered Startables can be controlled by Compentstarter instead of having
 * to start and stop them separately.
 * 
 * @see Startable
 * @author jmeetsma
 *
 */
public class ComponentStarter implements Startable {

	protected boolean isStarted = false;
	protected Set<Startable> registeredStartables = new HashSet<Startable>();

	public synchronized void register(Startable startable) {

		System.out.println(new LogEntry("registering " + startable + " with " + this));
		System.out.flush();
		if (isStarted && !startable.isStarted()) {
			startable.start();
		}
		registeredStartables.add(startable);
	}

	public synchronized void unregister(Startable startable) {
		registeredStartables.remove(startable);
	}


	public synchronized void start() {
		if (!isStarted) {
			isStarted = true;
			try {
				for (Startable startable : registeredStartables) {
					if (!startable.isStarted()) {
						startable.start();
					}
				}
			}
			catch (Exception e) {
				isStarted = false;
			}
		}
	}

	public synchronized boolean isStarted() {
		return isStarted;
	}

	public synchronized void stop() {
		isStarted = false;

		Startable[] startables = registeredStartables.toArray(new Startable[0]);
		for (int i = startables.length - 1; i >= 0; i--) {
			if (startables[i].isStarted()) {
				startables[i].stop();
			}
		}
	}

}
