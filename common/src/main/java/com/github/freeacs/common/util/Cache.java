package com.github.freeacs.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Cache can store CacheValue object. A CacheValue is wrapper for an Object +
 * certain timestamps and timeout values. There are 3 types of timeout: <br>
 * <ul>
 * <li>Occurs X seconds after last read of the CacheValue
 * <li>Occurs X seconds after creation of CacheValue
 * <li>Occurs at given time (this is actually just a variation of the previous
 * one)
 * </ul>
 * You decide which "rule" to apply when you create the CacheValue object. When
 * a timeout occurs, the CacheValue CAN be removed from the Cache. However, to
 * trigger a "cleanup"- process (yes, that's the purpose of the inner class in
 * this class) you need to actually make an access to this Cache-class. I could
 * of course have made this cleanup-thing to go in an eternal loop, but decided
 * against it, mostly because if it somehow crashed it would be a severe problem
 * for this Cache (--> MemoryError). Then I would have to make code to check
 * that it was running for each access to the Cache-class. Instead of doing that
 * I made the cleanup-process only run each <code>cleanUpFrequence</code> ms
 * and checked that timestamp for each access. Best of breed you could say.
 * 
 * @author Morten Simonsen
 */
public class Cache {

	public static int SINCE_ACCESSED = 1; 
	public static int SESSION = 1;
	
	public static int SINCE_CREATED = 2;
	public static int ABSOLUTE = 2;

	public static int SINCE_MODIFIED = 3;
	
	public static long STANDARDTIMEOUT = 30 * 60 * 1000;

	private Hashtable<Object, CacheValue> map = new Hashtable<Object, CacheValue>();
	private long cleanupFrequence = 60 * 1000;
	private long lastCleanup = System.currentTimeMillis();

	private static Logger log = LoggerFactory.getLogger(Cache.class);

	class Cleanup extends Cache implements Runnable {

		public void run() {
			long now = System.currentTimeMillis();
			if (lastCleanup + cleanupFrequence < now) {
				lastCleanup = System.currentTimeMillis();
				log.debug("Cache.Cleanup.run() starts");
				Enumeration<Object> keys = map.keys();
				Vector<Object> keysToRemove = new Vector<Object>();
				while (keys.hasMoreElements()) {
					Object key = keys.nextElement();
					CacheValue value = (CacheValue) map.get(key);
					boolean remove = false;
					if (value == null)
						remove = true;
					else {
						remove = toBeRemoved(value, now);
					}
					if (remove) {
						log.debug("Key " + key + " with CacheValue " + value + " is to be removed");
						if (value != null && value.getCleanupNotifier() != null) {
							log.debug("CacheValue has a cleanupnotifier-class which will be executed");
							value.getCleanupNotifier().execute();
						}
						keysToRemove.addElement(key);
					}
				}
				keys = keysToRemove.elements();
				while (keys.hasMoreElements()) {
					map.remove(keys.nextElement());
				}
				log.debug("Cache removed " + keysToRemove.size() + " elements out of " + map.size() + " in "
						+ (System.currentTimeMillis() - lastCleanup) + " ms.");
			}
		}
	}

	private boolean toBeRemoved(CacheValue value, long now) {
		long timeoutMs = 0;
		if (value.getType() == Cache.SESSION)
			timeoutMs = value.getLastAccess() + value.getTimeout();
		else if (value.getType() == Cache.ABSOLUTE)
			timeoutMs = value.getCreated() + value.getTimeout();
		else if (value.getType() == Cache.SINCE_MODIFIED)
			timeoutMs = value.getModified();
		if (timeoutMs > 0 && timeoutMs < now)
			return true;
		return false;

	}

	public void put(Object key, CacheValue value) {
		cleanup();
		map.put(key, value);
	}

	public Hashtable<Object, CacheValue> getMap() {
		return map;
	}

	public void remove(Object key) {
		log.debug("Key " + key + " will be removed from Cache (explicitely - not by Cache.Cleanup.run())");
		if (key != null) {
			map.remove(key);
		}
	}

	public CacheValue get(Object key) {
		cleanup();
		CacheValue value = (CacheValue) map.get(key);
		if (value != null) {
			if (toBeRemoved(value, System.currentTimeMillis())) {
				map.remove(key);
				return null;
			}
		}
		return value;
	}

	private void cleanup() {
		if (lastCleanup + cleanupFrequence < System.currentTimeMillis()) {
			synchronized (this.getMap()) {
				Thread t = new Thread(new Cleanup());
				t.setName("Cache Cleanup");
				t.start();
			}
		}
	}
}
