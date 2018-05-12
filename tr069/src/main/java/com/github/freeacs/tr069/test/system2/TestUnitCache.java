package com.github.freeacs.tr069.test.system2;

import com.github.freeacs.common.util.Cache;
import com.github.freeacs.common.util.CacheValue;

/**
 * Contains cached TestUnit objects. We keep this objects here, rather in the SessionData object, 
 * because we need to keep the objects here for a rather long time, at least 60 minutes SESSION
 * cache (60 minutes since last access to the object) - and the SessionData cache is short-lived
 * (only 2 minutes). 
 * @author Morten
 *
 */
public class TestUnitCache {

	private static Cache cache = new Cache();

	public static TestUnit get(String unitId) {
		CacheValue cv = cache.get(unitId);
		if (cv == null)
			return null;
		else
			return (TestUnit) cv.getObject();
	}

	public static void put(String unitId, TestUnit testUnit) {
		CacheValue cv = new CacheValue(testUnit, Cache.SESSION, 3600 * 1000);
		cache.put(unitId, cv);
	}
	
	public static void remove(String unitId) {
		cache.remove(unitId);
	}
}
