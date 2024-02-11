package com.github.freeacs.dbi.util;

import com.github.freeacs.common.util.NaturalComparator;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Getter
public class MapWrapper<V> {
  private final Map<String, V> map;

  public MapWrapper(boolean strictOrder) {
    if (strictOrder) {
      map = new TreeMap<>(new NaturalComparator());
    } else {
      map = new HashMap<>();
    }
  }

}
