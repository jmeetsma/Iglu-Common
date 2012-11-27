/* =======================================================================
 * Copyright (c) 2003-2010 IJsberg Automatisering BV. All rights reserved.
 * Redistribution and use of this code are permitted provided that the
 * conditions of the Iglu License are met.
 * The license can be found in org.ijsberg.iglu.StandardApplication.java
 * and is also published on http://iglu.ijsberg.org/LICENSE.
 * =======================================================================
 */
package org.ijsberg.iglu.util.caching;

import java.util.Collection;
import java.util.Set;

import org.ijsberg.iglu.configuration.Startable;

/**
 * Interface for a cache service that stores objects for a certain amount of time.
 */
public interface Cache<K, V>
{
	/**
	 * Stores an object in the cache.
	 * Nulls should be stored as well to indicate
	 * that a value can not be retrieved at all.
	 * @see this#containsStoredNull(Object)
	 *
	 * @param key the key to retrieve the object by
	 * @param object the object to be cached
	 */
	void store(K key, V object);

	/**
	 * Nulls may be stored in the cache to indicate that it's
	 * not possible to retrieve the value elsewhere.
	 *
	 * @param key
	 * @return true if a null is stored under a key
	 */
	boolean containsStoredNull(K key);

	/**
	 * Retrieves an object from cache.
	 *
	 * @param key the key to retrieve the object by
	 * @param timeout time to wait for the first thread to retrieve an object from the original location
	 * @return the cached object or null if it's not found
	 */
	Object retrieve(K key, int timeout);


	/**
	 * @return a set containing all stored objects
	 */
	Set<V> retrieveAll();

	/**
	 * Retrieves an object from cache.
	 *
	 * @param key the key to retrieve the object by
	 * @return the cached object or null if it's not found
	 */
	V retrieve(K key);

	/**
	 * Removes an object from cache.
	 *
	 * @param key object key
	 */
	void clear(K key);

	/**
	 * Removes a collection of objects from cache.
	 *
	 * @param keys object keys
	 */
	void clear(Collection<K> keys);

	/**
	 * Removes all objects from cache.
	 */
	void clear();
}
