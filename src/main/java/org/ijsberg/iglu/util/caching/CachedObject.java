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
package org.ijsberg.iglu.util.caching;

import java.io.Serializable;

/**
 * Wrapper for objects which are to be stored in a cache
 *
 * @see StandardCache
 */

public class CachedObject<T> implements Serializable {
	private long lastTimeAccessed;
	private long timeoutStartTime;
	private T cachedObject;
	private boolean isBeingRetrieved;


	/**
	 * Constructs an empty wrapper. TODO what for?
	 */
	public CachedObject() {
		timeoutStartTime = System.currentTimeMillis();
		lastTimeAccessed = timeoutStartTime;
	}


	/**
	 * Constructs a wrapper for a cached object.
	 *
	 * @param object cached object
	 */
	public CachedObject(T object) {
		timeoutStartTime = System.currentTimeMillis();
		lastTimeAccessed = timeoutStartTime;
		cachedObject = object;
	}

	/**
	 * @return cached object
	 */
	public T getObject() {
		lastTimeAccessed = System.currentTimeMillis();
		return cachedObject;
	}

	/**
	 * @param object
	 * @return cached object
	 */
	public Object setObject(T object) {
		timeoutStartTime = System.currentTimeMillis();
		lastTimeAccessed = System.currentTimeMillis();
		this.cachedObject = object;
		isBeingRetrieved = false;
		return cachedObject;
	}

	/**
	 * @return the last time the wrapped object was stored or retrieved (in milliseconds since january 1st 1970)
	 */
	public long getLastTimeAccessed() {
		return lastTimeAccessed;
	}

	/**
	 * @param timeoutInMillis time in milliseconds after which the object must be considered to be expired
	 * @return true if the object's cache time is expired
	 */
	public boolean isExpired(int timeoutInMillis) {
		return System.currentTimeMillis() > timeoutStartTime + timeoutInMillis;
	}

	/**
	 * @return true if some other thread has indicated that it's
	 *         already retrieving the object to be cached
	 */
	public boolean isBeingRetrieved() {
		return isBeingRetrieved;
	}

	/**
	 * Sets the indication that some thread is already retrieving the object to be cached.
	 */
	public void setBeingRetrieved() {
		cachedObject = null;
		timeoutStartTime = System.currentTimeMillis();
		this.isBeingRetrieved = true;
	}

	/**
	 * @return description containg the toString()-result of the cached object
	 */
	public String toString() {
		return "cached object: " + cachedObject.toString();
	}
}
