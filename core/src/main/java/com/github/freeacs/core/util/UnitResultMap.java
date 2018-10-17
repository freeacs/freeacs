package com.github.freeacs.core.util;

import java.util.LinkedHashMap;
import java.util.Map;

public class UnitResultMap<K, V> extends LinkedHashMap<K, V> {
  private static final long serialVersionUID = 1911267205492200962L;

  private int maxSize;

  private Map.Entry<K, V> eldestEntry;

  public UnitResultMap(int maxSize) {
    super(16, 0.75f, true);
    this.maxSize = maxSize;
  }

  protected boolean removeEldestEntry(Map.Entry<K, V> entry) {
    if (size() > maxSize) {
      eldestEntry = entry;
      return true;
    } else {
      eldestEntry = null;
      return false;
    }
  }

  public Map.Entry<K, V> getEldestEntry() {
    return eldestEntry;
  }

  public void setEldestEntry(Map.Entry<K, V> eldestEntry) {
    this.eldestEntry = eldestEntry;
  }
}
