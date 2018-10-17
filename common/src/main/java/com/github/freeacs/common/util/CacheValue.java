package com.github.freeacs.common.util;

import java.util.Calendar;

/**
 * CacheValue can be made in 3 different ways, and be read in 2 different ways,
 *
 * <ul>
 *   <li>Use Cache.SESSION (default) if you want it to timeout after X seconds after last access to
 *       the object.
 *   <li>Use Cache.ABSOLUTE if you want it to timeout after X seconds after creation of the object.
 *   <li>Use Cache.ABSOLUTE if you want it to timeout after at a certain time.
 * </ul>
 *
 * The first constructor could handle all 3 ways, but the constructor with Calendar is specifically
 * made to fit the 3 option.
 *
 * <p>When reading you usually change the lastRead-timestamp. However, it is possible to read
 * "unnoticed", something that could be useful when you just what to inspect the cache, not actually
 * read as use the data for anything.
 *
 * @author Morten Simonsen
 */
public class CacheValue {
  private int type;
  private Object object;
  private long created;
  private long accessed;
  private long modified;
  private long timeout;
  private CleanupNotifier cleanupNotifier;

  public CacheValue(Object object, int type, long timeout) {
    if (type == Cache.SESSION || type == Cache.ABSOLUTE) {
      this.type = type;
    } else {
      this.type = Cache.SESSION;
    }
    this.object = object;
    this.timeout = timeout;
    this.created = System.currentTimeMillis();
    this.accessed = System.currentTimeMillis();
  }

  public CacheValue(Object object, Calendar timeout) {
    this(object, Cache.ABSOLUTE, timeout.getTime().getTime() - System.currentTimeMillis());
  }

  public CacheValue(Object object) {
    this(object, Cache.SESSION, Cache.STANDARDTIMEOUT);
  }

  public Object getObject() {
    accessed = System.currentTimeMillis();
    return object;
  }

  public void setObject(Object object) {
    modified = System.currentTimeMillis();
    this.object = object;
  }

  public long getCreated() {
    return created;
  }

  public long getLastAccess() {
    return accessed;
  }

  public int getType() {
    return type;
  }

  public void setCreated(long l) {
    created = l;
  }

  public void setType(int i) {
    type = i;
  }

  public long getTimeout() {
    return timeout;
  }

  public void setTimeout(long long1) {
    timeout = long1;
  }

  public long getModified() {
    return modified;
  }

  public void setModified(long modified) {
    this.modified = modified;
  }

  public CleanupNotifier getCleanupNotifier() {
    return cleanupNotifier;
  }

  public void setCleanupNotifier(CleanupNotifier cleanupNotifier) {
    this.cleanupNotifier = cleanupNotifier;
  }
}
