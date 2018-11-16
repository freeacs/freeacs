package com.github.freeacs.common.util;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cache can store CacheValue object. A CacheValue is wrapper for an Object + certain timestamps and
 * timeout values. There are 3 types of timeout: <br>
 *
 * <ul>
 *   <li>Occurs X seconds after last read of the CacheValue
 *   <li>Occurs X seconds after creation of CacheValue
 *   <li>Occurs at given time (this is actually just a variation of the previous one)
 * </ul>
 *
 * You decide which "rule" to apply when you create the CacheValue object. When a timeout occurs,
 * the CacheValue CAN be removed from the Cache. However, to trigger a "cleanup"- process (yes,
 * that's the purpose of the inner class in this class) you need to actually make an access to this
 * Cache-class. I could of course have made this cleanup-thing to go in an eternal loop, but decided
 * against it, mostly because if it somehow crashed it would be a severe problem for this Cache (-->
 * MemoryError). Then I would have to make code to check that it was running for each access to the
 * Cache-class. Instead of doing that I made the cleanup-process only run each <code>
 * cleanUpFrequence</code> ms and checked that timestamp for each access. Best of breed you could
 * say.
 *
 * @author Morten Simonsen
 */
public class Cache {
  public static int SESSION = 1;

  public static int ABSOLUTE = 2;

  private static int SINCE_MODIFIED = 3;

  protected static long STANDARDTIMEOUT = 30 * 60 * 1000;

  private Map<Object, CacheValue> map = new Hashtable<>();
  private long cleanupFrequence = 60 * 1000;
  private long lastCleanup = System.currentTimeMillis();

  private static Logger log = LoggerFactory.getLogger(Cache.class);

  class Cleanup extends Cache implements Runnable {
    public void run() {
      int startSize = map.size();
      long now = System.currentTimeMillis();
      if (lastCleanup + cleanupFrequence < now) {
        lastCleanup = System.currentTimeMillis();
        log.debug("Cache.Cleanup.run() starts");
        int keysRemoved = cleanup(now, null);
        long howLong = System.currentTimeMillis() - lastCleanup;
        log.debug(
            "Cache removed "
                + keysRemoved
                + " elements out of "
                + startSize
                + " in "
                + howLong
                + " ms.");
      }
    }
  }

  public void removeSession(String sessionId) {
    int startSize = map.size();
    log.debug("Cache.removeSession(" + sessionId + ") starts");
    int keysRemoved = cleanup(System.currentTimeMillis(), (k) -> k.toString().contains(sessionId));
    log.debug(
        "Cache removed "
            + keysRemoved
            + " elements out of "
            + startSize
            + " in "
            + (System.currentTimeMillis() - lastCleanup)
            + " ms.");
  }

  private int cleanup(long now, Predicate<Object> predicate) {
    Iterator<Object> keys = map.keySet().iterator();
    List<Object> keysToRemove = new Vector<>();
    while (keys.hasNext()) {
      Object key = keys.next();
      CacheValue value = map.get(key);
      if (shouldRemove(now, predicate, key, value)) {
        log.debug("Key " + key + " with CacheValue " + value + " is to be removed");
        maybeNotifyCleanup(value);
        keysToRemove.add(key);
      }
    }
    keysToRemove.forEach(map::remove);
    return keysToRemove.size();
  }

  private void maybeNotifyCleanup(CacheValue value) {
    if (value != null && value.getCleanupNotifier() != null) {
      log.debug("CacheValue has a cleanupnotifier-class which will be executed");
      value.getCleanupNotifier().execute();
    }
  }

  private boolean shouldRemove(
      long now, Predicate<Object> predicate, Object key, CacheValue value) {
    boolean remove = predicate != null && predicate.test(key);
    if (!remove) {
      if (value != null) {
        remove = toBeRemoved(value, now);
      } else {
        remove = true;
      }
    }
    return remove;
  }

  private boolean toBeRemoved(CacheValue value, long now) {
    long timeoutMs = 0;
    if (value.getType() == SESSION) {
      timeoutMs = value.getLastAccess() + value.getTimeout();
    } else if (value.getType() == ABSOLUTE) {
      timeoutMs = value.getCreated() + value.getTimeout();
    } else if (value.getType() == SINCE_MODIFIED) {
      timeoutMs = value.getModified();
    }
    return timeoutMs > 0 && timeoutMs < now;
  }

  public void put(Object key, CacheValue value) {
    cleanup();
    map.put(key, value);
  }

  public Map<Object, CacheValue> getMap() {
    return map;
  }

  public void remove(Object key) {
    if (log.isDebugEnabled()) {
      log.debug(
          "Key " + key + " will be removed from Cache (explicitely - not by Cache.Cleanup.run())");
    }
    if (key != null) {
      map.remove(key);
    }
  }

  public CacheValue get(Object key) {
    cleanup();
    CacheValue value = map.get(key);
    if (value != null && toBeRemoved(value, System.currentTimeMillis())) {
      map.remove(key);
      return null;
    }
    return value;
  }

  private void cleanup() {
    if (lastCleanup + cleanupFrequence < System.currentTimeMillis()) {
      synchronized (getMap()) {
        Thread t = new Thread(new Cleanup());
        t.setName("Cache Cleanup");
        t.start();
      }
    }
  }
}
