package com.github.freeacs.tr069.base;

import com.github.freeacs.common.util.Cache;
import com.github.freeacs.common.util.CacheValue;
import com.github.freeacs.dbi.File;
import java.util.ArrayList;
import java.util.List;

public class BaseCache {
  private static Cache cache = new Cache();

  /** 2 minutes. */
  private static final int SESSIONDATA_CACHE_TIMEOUT = 3 * 60 * 1000;

  /** 10 minutes. */
  private static final int FIRMWAREIMAGE_CACHE_TIMEOUT = 10 * 60 * 1000;

  private static final String SESSION_KEY = "SESSION";

  private static final String FIRMWAREIMAGE_KEY = "FIRMWARE";

  /** Clears all parts of the cache, except for sessiondata. */
  public static void clearCache() {
    List<String> keyRemoveList = new ArrayList<>();
    for (Object key : cache.getMap().keySet()) {
      String keyStr = (String) key;
      if (!keyStr.contains(SESSION_KEY)) {
        keyRemoveList.add(keyStr);
      }
    }
    for (String key : keyRemoveList) {
      cache.remove(key);
    }
  }

  /**
   * Retrieves the current session data from the cache based on a key that identifies the client.
   *
   * @param unitKey Can be either session id or unit id
   * @return SessionDataI
   */
  public static SessionDataI getSessionData(String unitKey) {
    String key = unitKey + SESSION_KEY;
    CacheValue cv = cache.get(key);
    if (cv != null) {
      return (SessionDataI) cv.getObject();
    } else {
      throw new BaseCacheException(key);
    }
  }

  /**
   * Puts the given session data into the cache with a key that identifies the client.
   *
   * @param unitKey Can be either session id or unit id
   * @param sessionData The session data to be stored in cache
   */
  public static void putSessionData(String unitKey, SessionDataI sessionData) {
    if (sessionData != null) {
      String key = unitKey + SESSION_KEY;
      CacheValue cv = new CacheValue(sessionData, Cache.SESSION, SESSIONDATA_CACHE_TIMEOUT);
      cv.setCleanupNotifier(new SessionDataCacheCleanup(unitKey, sessionData));
      cache.put(key, cv);
    }
  }

  public static void removeSessionData(String unitKey) {
    String key = unitKey + SESSION_KEY;
    cache.remove(key);
  }

  public static File getFirmware(String firmwareName, String unittypeName) {
    String key = firmwareName + unittypeName + FIRMWAREIMAGE_KEY;
    CacheValue cv = cache.get(key);
    if (cv != null) {
      return (File) cv.getObject();
    } else {
      return null;
    }
  }

  public static void putFirmware(String firmwareName, String unittypeName, File firmware) {
    String key = firmwareName + unittypeName + FIRMWAREIMAGE_KEY;
    if (firmware != null) {
      CacheValue cv = new CacheValue(firmware, Cache.ABSOLUTE, FIRMWAREIMAGE_CACHE_TIMEOUT);
      cache.put(key, cv);
    }
  }
}
