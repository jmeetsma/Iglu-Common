package org.ijsberg.iglu.runtime.module;

import org.ijsberg.iglu.runtime.Startable;

import java.util.HashSet;
import java.util.Set;

public class Starter implements Startable {

	protected boolean isStarted = false;
	//TODO list?
	protected Set<Startable> registeredStartables = new HashSet<Startable>();

	//TODO Initializable / isReady

	public synchronized void register(Startable startable) {

		System.out.println("registering " + startable + " with " + this);
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

	//TODO reverse order
	public synchronized void stop() {
		isStarted = false;


		for (Startable startable : registeredStartables) {
			if (startable.isStarted()) {
				startable.stop();
			}
		}
	}

}
