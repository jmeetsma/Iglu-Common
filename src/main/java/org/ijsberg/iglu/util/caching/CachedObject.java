/* =======================================================================
 * Copyright (c) 2003-2010 IJsberg Automatisering BV. All rights reserved.
 * Redistribution and use of this code are permitted provided that the
 * conditions of the Iglu License are met.
 * The license can be found in org.ijsberg.iglu.StandardApplication.java
 * and is also published on http://iglu.ijsberg.org/LICENSE.
 * =======================================================================
 */
package org.ijsberg.iglu.util.caching;

import java.io.Serializable;

/**
 * Wrapper for objects which are to be stored in a cache
 *
 * @see StandardCache
 */

public class CachedObject<T> implements Serializable
{
	private long lastTimeAccessed;
	private long timeoutStartTime;
	private T cachedObject;
	private boolean isBeingRetrieved;


	/**
	 * Constructs an empty wrapper. TODO what for?
	 */
	public CachedObject()
	{
		timeoutStartTime = System.currentTimeMillis();
		lastTimeAccessed = timeoutStartTime;
	}


	/**
	 * Constructs a wrapper for a cached object.
	 *
	 * @param object cached object
	 */
	public CachedObject(T object)
	{
		timeoutStartTime = System.currentTimeMillis();
		lastTimeAccessed = timeoutStartTime;
		cachedObject = object;
	}

	/**
	 * @return cached object
	 */
	public T getObject()
	{
		lastTimeAccessed = System.currentTimeMillis();
		return cachedObject;
	}

	/**
	 *
	 * @param object
	 * @return cached object
	 */
	public Object setObject(T object)
	{
		timeoutStartTime = System.currentTimeMillis();
		lastTimeAccessed = System.currentTimeMillis();
		this.cachedObject = object;
		isBeingRetrieved = false;
		return cachedObject;
	}
	/**
	 * @return the last time the wrapped object was stored or retrieved (in milliseconds since january 1st 1970)
	 */
	public long getLastTimeAccessed()
	{
		return lastTimeAccessed;
	}

	/**
	 * @param timeoutInMillis time in milliseconds after which the object must be considered to be expired
	 * @return true if the object's cache time is expired
	 */
	public boolean isExpired(int timeoutInMillis)
	{
		return System.currentTimeMillis() > timeoutStartTime + timeoutInMillis;
	}

	/**
	 * @return true if some other thread has indicated that it's
	 *         already retrieving the object to be cached
	 */
	public boolean isBeingRetrieved()
	{
		return isBeingRetrieved;
	}

	/**
	 * Sets the indication that some thread is already retrieving the object to be cached.
	 *
	 */
	public void setBeingRetrieved()
	{
		cachedObject = null;
		timeoutStartTime = System.currentTimeMillis();
		this.isBeingRetrieved = true;
	}

	/**
	 * @return description containg the toString()-result of the cached object
	 */
	public String toString()
	{
		return "cached object: " + cachedObject.toString();
	}
}
