package com.github.freeacs.dbi.util;

import com.github.freeacs.common.util.NaturalComparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class MapWrapper<V> {
  private Map<String, V> map;

  public MapWrapper(boolean strictOrder) {
    if (strictOrder) {
      map = new TreeMap<String, V>(new NaturalComparator());
    } else {
      map = new HashMap<>();
    }
  }

  public Map<String, V> getMap() {
    return map;
  }
}
