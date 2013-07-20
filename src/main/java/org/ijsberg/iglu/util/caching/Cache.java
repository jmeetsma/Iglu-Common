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

import java.util.Collection;
import java.util.Set;

/**
 * Interface for a cache service that stores objects for a certain amount of time.
 */
public interface Cache<K, V> {
	/**
	 * Stores an object in the cache.
	 * Nulls should be stored as well to indicate
	 * that a value can not be retrieved at all.
	 *
	 * @param key    the key to retrieve the object by
	 * @param object the object to be cached
	 * @see this#containsStoredNull(Object)
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
	 * @param key     the key to retrieve the object by
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
