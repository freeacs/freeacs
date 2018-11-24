package com.github.freeacs.web.app.util;

import com.github.freeacs.common.util.Cache;
import com.github.freeacs.common.util.CacheValue;
import com.github.freeacs.dbi.DBI;
import com.github.freeacs.web.security.AllowedUnittype;
import com.github.freeacs.web.security.ThreadUser;
import com.github.freeacs.web.security.WebUser;

/**
 * The Session Store for xAPS Web. Uses session id as unique identifier.
 *
 * @author Jarl Andre Hubenthal
 */
public class SessionCache {
  /** The cache. */
  private static Cache cache = new Cache();

  /** Reset. */
  public static void reset() {
    cache = new Cache();
  }

  /**
   * Key.
   *
   * @param sessionId the session id
   * @param keypart the keypart
   * @return the string
   */
  private static String key(String sessionId, String keypart) {
    return sessionId + keypart;
  }

  /**
   * Put dbi.
   *
   * @param sessionId the session id
   * @param xapsCache the xaps cache
   * @param lifeTimeSec the life time sec
   */
  public static void putDBI(String sessionId, DBI xapsCache, int lifeTimeSec) {
    if (xapsCache == null) {
      cache.remove(key(sessionId, "dbi"));
    } else {
      String key = key(sessionId, "dbi");
      cache.put(key, new CacheValue(xapsCache, Cache.SESSION, (lifeTimeSec * 1000)));
    }
  }

  public static void removeSession(String sessionId) {
    cache.removeSession(sessionId);
  }

  /**
   * Gets the dBI.
   *
   * @param sessionId the session id
   * @return the dBI
   */
  public static DBI getDBI(String sessionId) {
    if (cache.get(key(sessionId, "dbi")) != null) {
      return (DBI) cache.get(key(sessionId, "dbi")).getObject();
    }
    return null;
  }

  /**
   * Gets the session data.
   *
   * @param sessionId the session id
   * @return the session data
   */
  public static SessionData getSessionData(String sessionId) {
    CacheValue cv = cache.get(key(sessionId, "sessionData"));
    if (cv == null) {
      SessionData sessionData = new SessionData();
      WebUser webUser = ThreadUser.getUserDetails();
      sessionData.setUser(webUser);
      sessionData.setFilteredUnittypes(AllowedUnittype.retrieveAllowedUnittypes(webUser));
      cache.put(
          key(sessionId, "sessionData"),
          new CacheValue(sessionData, Cache.SESSION, Long.MAX_VALUE)); // fixme oh please ...
      return sessionData;
    }
    return (SessionData) cv.getObject();
  }

  public static Cache getCache() {
    return cache;
  }
}
