/* =======================================================================
 * Copyright (c) 2003-2010 IJsberg Automatisering BV. All rights reserved.
 * Redistribution and use of this code are permitted provided that the
 * conditions of the Iglu License are met.
 * The license can be found in org.ijsberg.iglu.StandardApplication.java
 * and is also published on http://iglu.ijsberg.org/LICENSE.
 * =======================================================================
 */
package org.ijsberg.iglu.util.caching.module;

//TODO move up out of util


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import org.ijsberg.iglu.configuration.Startable;
import org.ijsberg.iglu.logging.Level;
import org.ijsberg.iglu.logging.LogEntry;
import org.ijsberg.iglu.scheduling.Pageable;
import org.ijsberg.iglu.util.caching.Cache;
import org.ijsberg.iglu.util.caching.CachedObject;

/**
 * This class is a basic caching service that stores objects for a certain amount of time.
 */
public class StandardCache<K, V> implements Cache<K, V>, Startable, Pageable
{
	private Date lastRun = new Date();

	private final HashMap<K, CachedObject<V>> data = new HashMap<K, CachedObject<V>>(50);
	private final HashMap<K, CachedObject<V>> mirror = new HashMap<K, CachedObject<V>>(50);

	public static final int DEFAULT_TTL = 900;// 15 minutes; 0 = don't cache
	public static final int DEFAULT_CLEANUP_INTERVAL = 180; // 3 minutes; 0 = never cleanup

	private int ttlInSeconds = DEFAULT_TTL;
	private long cleanupInterval = DEFAULT_CLEANUP_INTERVAL;

	//statistics
	private long hits;
	private long misses;
	private long unavailable;
	private long delayedHits;
	private long delayedMisses;
	
	private String deviceId; 

	/**
	 * Constructs a cache.
	 *
	 * @param serviceId the service ID to be used in the application registry
	 */
	public StandardCache(String serviceId)
	{
		this.deviceId = serviceId;
	}

	/**
	 * Constructs a cache.
	 *
	 * @param serviceId the service ID to be used in the application registry
	 * @param ttl time to live in seconds for cached objects
	 * @param cleanupInterval cleanup interval in seconds
	 */
	protected StandardCache(String serviceId, int ttl, long cleanupInterval)
	{
		this.deviceId = serviceId;
		this.ttlInSeconds = ttl;
		this.cleanupInterval = cleanupInterval;
	}

	/**
	 * Returns a status report containing behavior and statistics.
	 *
	 * @return status report
	 */
	public String getReport()
	{
		StringBuffer info = new StringBuffer();
		if (!isCachingEnabled())
		{
			info.append("cache behaviour: DISABLED\n");
		}
		else if (isCachingPermanent())
		{
			info.append("cache behaviour: PERMANENT\n");
		}
		else
		{
			info.append("cache behaviour: NORMAL\n");
		}

		info.append("time to live: " + ttlInSeconds + " s\n");
		info.append("cleanup interval: " + cleanupInterval + " s\n");
		info.append("cache size: " + data.size() + " objects\n");
		info.append("cache mirror size: " + mirror.size() + " object(s)\n");
		info.append("cache hits: " + hits + '\n');
		info.append("cache misses: " + misses + '\n');
		info.append("cache unavailable: " + unavailable + '\n');
		info.append("cache delayed hits: " + delayedHits + '\n');
		info.append("cache delayed misses: " + delayedMisses + '\n');
		info.append("next cleanup run: " + new Date(lastRun.getTime() + (cleanupInterval * 1000)) + "\n");
		return info.toString();
	}

	
	private boolean isStarted = false;
	
	/**
	 */
	public void start()
	{
		isStarted = true;
	}

	/**
	 */
	public boolean isStarted()
	{
		return isStarted;
	}
	
	/**
	 * Clears storage. Resets statistics.
	 * Is invoked by superclass.
	 */
	public void stop()
	{
		hits = 0;
		misses = 0;
		unavailable = 0;
		delayedHits = 0;
		delayedMisses = 0;
		data.clear();
		mirror.clear();
		isStarted = false;
	}

	/**
	 * Initializes cache.
	 * Properties:
	 * <ul>
	 * <li>ttlInSeconds: time to live for stored objects in seconds (default: 900 = 15 minutes)</li>
	 * <li>cleanup_interval: interval for check for expired objects in seconds (default: 180 = 3 minutes)</li>
	 * </ul>
	 */
	public void setProperties(Properties properties)
	{
		ttlInSeconds = Integer.valueOf(properties.getProperty("ttlInSeconds","" + ttlInSeconds));
		cleanupInterval = Integer.valueOf(properties.getProperty("cleanup_interval", "" + cleanupInterval));
	}


	/**
	 * Stores an object in the cache.
	 * Nulls can (and must) be stored as well.
	 * This saves a lot of unnecessary lookups!
	 *
	 * @param key the key to retrieve the object by
	 * @param object the object to be cached
	 */
	public void store(K key, V object)
	{
		if (isCachingEnabled())
		{
			CachedObject<V> co = new CachedObject<V>(object);
			storeCachedObject(key, co);
		}
	}

	private boolean isCachingEnabled()
	{
		return ttlInSeconds > 0 && isActive();
	}

	private boolean isCachingPermanent()
	{
		return cleanupInterval == 0;
	}

	/**
	 * 
	 * @param key
	 * @return true if a null was deliberately stored under a key
	 */
	public boolean containsStoredNull(K key)
	{
		CachedObject<V> co = getCachedObject(key);
		return isCachingEnabled() && co != null && co.getObject() == null && !co.isBeingRetrieved() && !co.isExpired(ttlInSeconds);
	}


	/**
	 * Retrieves an object from cache.
	 * This method should be used if a programmer suspects bursts of requests for
	 * a particular object.
	 * If this is the case, the first thread will retrieve the object,
	 * the others will wait for some time, in order to save overhead.
	 *
	 * @param key the key to retrieve the object by
	 * @param timeout time in millis to wait for the first thread to retrieve an object from the original location
	 * @return the cached object or null if it's not found
	 */
	public Object retrieve(K key, int timeout)
	{
		Object retval = null;
		if (isCachingEnabled())
		{
			CachedObject<V> co = getCachedObject(key);
			if (co == null || isCachedObjectExpired(co))
			{
				misses++;
				co = new CachedObject<V>();
				co.setBeingRetrieved();
				this.storeCachedObject(key, co);
//				registerThatObjectIsBeingRetrieved(key);
				//sorry, retrieve it from its original location
				// (in the mean time other requests will be stalled)
//				return null;
			}
			else if (co.getObject() != null)
			{
				hits++;
				retval = co.getObject();
			}
			else
			{
				//the timeout for retrieving an object is used instead of the cache timeout
				co = getCachedObjectOnceRetrievedByOtherThread(key, timeout);
				if (co == null)
				{
					//this could happen on a rare occasion and may not lead to problems
					delayedMisses++;
		// 			return null;
				}
				else if (co.getObject() == null)//still null
				{
					delayedMisses++;
					if (co.isExpired(timeout) && co.isBeingRetrieved())
					{
						// prolongate retrieval state if cached object is not a designated null
						co.setBeingRetrieved();
//						registerThatObjectIsBeingRetrieved(key);
					}
		//			return null;
				}
				else
				{
					delayedHits++;
					retval = co.getObject();
				}
			}
		}
		return retval;
	}

	private CachedObject<V> getCachedObjectOnceRetrievedByOtherThread(K key, int timeout)
	{
		CachedObject<V> co;
		do
		{
			try
			{
				Thread.sleep(10);
			}
			catch (InterruptedException ie)
			{
				//...
			}
			co = getCachedObject(key);
		}
		while (co != null //which may occur if it was removed from data
				&& co.getObject() == null //not retrieved
				&& co.isBeingRetrieved() //still trying
				&& !co.isExpired(timeout));//retrieval timeout not expired
		return co;
	}

	/**
	 * Places an empty wrapper in the cache to indicate that some thread should
	 * be busy retrieving the object, after which it should be cached after all.
	 *
	 * @param key
	 */
/*	private void registerThatObjectIsBeingRetrieved(Object key)
	{
		CachedObject co = new CachedObject();
		co.setBeingRetrieved();
		putCachedObject(key, co);
	}*/

	/**
	 *
	 * @param key
	 * @param co
	 * @return
	 */
	private Object storeCachedObject(K key, CachedObject<V> co)
	{
		//data is only locked if cleanup removes cached objects
		synchronized (data)
		{
			data.put(key, co);
		}
		//mirror is also locked if cleanup is busy collecting cached objects
		synchronized (mirror)
		{
			mirror.put(key, co);
		}
		System.out.println(new LogEntry("object with key " + key + " stored in cache"));
		return co;
	}

	/**
	 * @return a set containing all object stored
	 */
	public Set<V> retrieveAll()
	{
		HashSet<V> retval = new HashSet<V>();
		synchronized (data)
		{
			for(CachedObject<V> co : data.values())
			{
				if (co.getObject() != null) //skip temp objects
				{
					retval.add(co.getObject());
				}
			}
		}
		return retval;
	}

	/**
	 * Retrieves an object from cache.
	 *
	 * @param key the key to retrieve the object by
	 * @return the cached object or null if it's not found
	 */
	public V retrieve(K key)
	{
		V retval = null;
		if (isCachingEnabled())
		{
			CachedObject<V> co = getCachedObject(key);
			if (co == null || (isCachedObjectExpired(co)))
			{
				//take pressure off of cleanup
				misses++;
			}
			else if (co.getObject() == null)
			{
				unavailable++;
			}
			else
			{
				hits++;
				retval = co.getObject();
			}
		}
		return retval;
	}

	private boolean isCachedObjectExpired(CachedObject<V> co)
	{
		return co.isExpired(ttlInSeconds * 1000) && !isCachingPermanent();
	}

	private CachedObject<V> getCachedObject(K key)
	{
		synchronized (data)
		{
			return data.get(key);
		}
	}


	public int getPageIntervalInMinutes()
	{
		return (int)(cleanupInterval / 60);
	}

	public int getPageOffsetInMinutes()
	{
		return 0;
	}

	public void onPageEvent(long officialTime)
	{
		if (cleanupInterval > 0 && isCachingEnabled())
		{
			lastRun = new Date();
			cleanup();
		}
	}

	/**
	 * Removes all expired objects.
	 *
	 * @return the number of removed objects.
	 */
	public long cleanup()
	{
		int garbageSize = 0;
		if (isCachingEnabled())
		{
	//		CachedObject cachedObject;
			System.out.println(new LogEntry(Level.VERBOSE, "Identifying expired objects"));
			ArrayList<K> garbage = getExpiredObjects();
			garbageSize = garbage.size();
			System.out.println(new LogEntry("cache cleanup: expired objects: " + garbageSize));
			for(K key : garbage)
			{
				clear(key);
			}
		}
		return garbageSize;
	}

	private ArrayList<K> getExpiredObjects()
	{
		CachedObject<V> cachedObject;
		ArrayList<K> garbage = new ArrayList<K>();
		synchronized (mirror)
		{
			for(K key : mirror.keySet())
			{
				cachedObject = (CachedObject<V>) mirror.get(key);
				if (cachedObject.isExpired(ttlInSeconds * 1000))
				{
					garbage.add(key);
				}
			}
		}
		return garbage;
	}


	/**
	 * Removes an object from cache.
	 *
	 * @param key object key
	 */
	public void clear(Object key)
	{
		Object removed;
		if (isCachingEnabled())
		{
			synchronized (mirror)
			{
				synchronized (data)
				{
					removed = data.remove(key);
					mirror.remove(key);
				}
			}
			System.out.println(new LogEntry("object with key " + key + (removed == null ? " NOT" : "") + " removed from cache"));
		}
	}

	/**
	 * Removes a collection of objects from cache.
	 *
	 * @param keys object keys
	 */
	public void clear(Collection keys)
	{
//		if (ttlInSeconds > 0/* && isActive()*/)
		{
			synchronized (mirror)
			{
				synchronized (data)
				{
					Iterator i = keys.iterator();
					while (i.hasNext())
					{
						Object key = i.next();
						data.remove(key);
						mirror.remove(key);
					}
				}
			}
		}
	}

	public void clear()
	{
		synchronized (mirror)
		{
			synchronized (data)
			{
				data.clear();
				mirror.clear();
			}
		}
	}


	/**
	 * Suspends cleanup to hold on to loaded data.
	 */
/*	public void activateOfflineMode()
	{
		try
		{
			stop();
			cleanupInterval = 0;
			ttlInSeconds = 1;
			start();
		}
		catch (EnvironmentException ce)
		{
			System.out.println(new LogEntry(Level.CRITICAL, ce));
		}
	}*/

	/**
	 * Activates the currently configured mode.
	 */
/*	public void activateOnlineMode()
	{
		if(service != null)
		{
			service.reset();
		}
	}*/



	/**
	 * Retrieves a cache from the current layer or creates it.
	 * Only a root request will be able to embed the constructed cache in the
	 * application.
	 *
	 * @param cacheName cache service ID
	 * @return
	 */
	public static <K, V> Cache<K, V> createCache(String cacheName)
	{
/*		Application application = Environment.currentApplication();
		Request request;
		Layer layer = null;
		if(application != null)
		{
			request = application.getCurrentRequest();
			if(request != null)
			{
				layer = request.getCurrentLayer();
			}
		}*/
		return createCache(cacheName, DEFAULT_TTL, DEFAULT_CLEANUP_INTERVAL/*, application, layer*/);
	}

	/**
	 * Retrieves a cache from the current layer or creates it.
	 * Only a root request will be able to embed the constructed cache in the
	 * application.
	 *
	 * @param cacheName cache service ID
	 * @param ttl time to live in seconds
	 * @param cleanupInterval cleanup interval in seconds
	 * @param application
	 * @param layer
	 * @return
	 */
	public static <K, V> Cache<K, V> createCache(String cacheName, int ttl, long cleanupInterval/*, Application application, Layer layer*/)
	{
//		if(layer == null)
		{
			StandardCache cache = new StandardCache(cacheName, ttl, cleanupInterval);
			cache.setProperties(new Properties());
//			cache.initialize();
			cache.start();
			return cache;
		}
/*		else
		{
			//create cache in application structure
			Cache cache;
			try
			{
				cache = (Cache) layer.getService(cacheName).getProxy(Cache.class);
			}
			catch (ConfigurationException e)
			{
				// Try again within this synchronized block
				// with some luck, someone else already is creating the cache, so wait for it...
				synchronized (Cache.class)
				{
					try
					{
						cache = (Cache) layer.getService(cacheName).getProxy(Cache.class);
					}
					catch (ConfigurationException e2)
					{
						// create new cache
						cache = new StandardCache(cacheName, ttl, cleanupInterval);
						try
						{
							//next statement will start cache
							layer.embedInService(cacheName, cache);
							application.log("cache '" + cacheName + "' created and embedded in layer '" + layer + '\'');
						}
						catch (SecurityException se)
						{
							System.out.println(new LogEntry(Level.CRITICAL, "cache '" + cacheName +
									"' created but NOT embedded in layer '" + layer + "' reason " + se));
						}
					}
				}
			}
			return cache;
		}
*/
	}

	@Override
	public boolean isActive() {
		// TODO Auto-generated method stub
		return isStarted;
	}
}
