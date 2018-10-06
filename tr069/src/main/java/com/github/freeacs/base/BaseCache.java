package com.github.freeacs.base;

import com.github.freeacs.dbi.File;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.concurrent.TimeUnit;

public class BaseCache {

  private static final Cache<String, SessionDataI> sessionDataCache =
      CacheBuilder.newBuilder()
          .maximumSize(10000)
          .expireAfterAccess(3 * 60 * 1000, TimeUnit.MILLISECONDS)
          .build();

  private static final Cache<String, File> firmwareImageCache =
      CacheBuilder.newBuilder()
          .maximumSize(10000)
          .expireAfterAccess(10 * 60 * 1000, TimeUnit.MILLISECONDS)
          .build();

  private static final String SESSION_KEY = "SESSION";

  private static final String FIRMWAREIMAGE_KEY = "FIRMWARE";

  /**
   * Retrieves the current session data from the cache based on a key that identifies the client.
   *
   * @param unitKey Can be either session id or unit id
   * @return SessionDataI
   */
  public static SessionDataI getSessionData(String unitKey) {
    String key = unitKey + SESSION_KEY;
    SessionDataI cv = sessionDataCache.getIfPresent(key);
    if (cv != null) return cv;
    else throw new BaseCacheException(key);
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
      sessionDataCache.put(key, sessionData);
    }
  }

  public static void removeSessionData(String unitKey) {
    String key = unitKey + SESSION_KEY;
    sessionDataCache.invalidate(key);
  }

  public static File getFirmware(String firmwareName, String unittypeName) {
    String key = firmwareName + unittypeName + FIRMWAREIMAGE_KEY;
    File cv = firmwareImageCache.getIfPresent(key);
    if (cv != null) return cv;
    else return null;
  }

  public static void putFirmware(String firmwareName, String unittypeName, File firmware) {
    String key = firmwareName + unittypeName + FIRMWAREIMAGE_KEY;
    if (firmware != null) {
      firmwareImageCache.put(key, firmware);
    }
  }
}
