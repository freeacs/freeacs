package com.github.freeacs.common.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TimestampMap {
  private static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
  /**
   * Using ConcurrentHashMap allow this class to share the map for iteration+read purposes without
   * conflicting insert/modify (in multiple threaded environment).
   */
  private Map<String, Long> map = new ConcurrentHashMap<>();

  private String oldest;
  private String newest;

  public void put(String key, Long tms) {
    if (key == null || tms == null) {
      return;
    }
    newest = key;
    if (oldest == null) {
      oldest = key;
    }
    map.put(key, tms);
  }

  public synchronized void putSync(String key, Long tms) {
    put(key, tms);
  }

  public Long get(String key) {
    return map.get(key);
  }

  public String newest() {
    return newest;
  }

  public String oldest() {
    return oldest;
  }

  public Map<String, Long> removeOld(long tooOldTms) {
    Iterator<String> iterator = map.keySet().iterator();
    Long oldestTms = Long.MAX_VALUE;
    String oldestKey = null;
    Map<String, Long> removedMap = new HashMap<>();
    while (iterator.hasNext()) {
      String key = iterator.next();
      Long tms = map.get(key);
      if (tms < tooOldTms) {
        iterator.remove();
        removedMap.put(key, tms);
      } else if (tms < oldestTms) {
        oldestKey = key;
        oldestTms = tms;
      }
    }
    this.oldest = oldestKey;
    if (map.isEmpty()) {
      this.newest = null;
    }
    return removedMap;
  }

  public synchronized Map<String, Long> removeOldSync(long tooOldTms) {
    return removeOld(tooOldTms);
  }

  public int size() {
    return map.size();
  }

  public String toString() {
    if (oldest != null && newest != null) {
      return "Contains: "
          + size()
          + " entries from "
          + sdf.format(new Date(map.get(oldest)))
          + " to "
          + sdf.format(map.get(newest));
    } else {
      return "Contains: " + size() + " entries";
    }
  }

  public Map<String, Long> getMap() {
    return map;
  }
}
