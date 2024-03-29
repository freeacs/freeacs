package com.github.freeacs.common.util;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;

public class NaturalComparatorTest {

  @Test
  public void sortingTest() {
    Map<String, String> m = new TreeMap<>(new NaturalComparator());

    m.put("1", "1");
    m.put("2", "2");
    m.put("3", "3");
    m.put("10", "10");
    m.put("Abc", "Abc");
    m.put("abc", "abc");

    for (Entry<String, String> e : m.entrySet()) {
      assertNotNull(e.getKey());
    }
    assertNull(m.get(null));
  }
}
