package com.owera.common.log;

import com.owera.common.util.Cache;
import com.owera.common.util.CacheValue;

public class Context {
	private static Cache cache = new Cache();
	
	public static String X = "X";

	public static void put(String key, String value, long cacheTimeoutMs) {
		cache.put(Thread.currentThread() + key, new CacheValue(value, Cache.SESSION, cacheTimeoutMs));
	}

	public static void put(String key, Object value, long cacheTimeoutMs) {
		cache.put(Thread.currentThread() + key, new CacheValue(value, Cache.SESSION, cacheTimeoutMs));
	}

	public static String get(String key) {
		CacheValue cv = cache.get(Thread.currentThread() + key);
		if (cv != null)
			return (String) cv.getObject();
		return null;
	}
	
	public static Object getObject(String key) {
		CacheValue cv = cache.get(Thread.currentThread() + key);
		if (cv != null)
			return cv.getObject();
		return null;		
	}
	
	public static void remove(String key) {
		cache.remove(Thread.currentThread() + key);
	}
}
