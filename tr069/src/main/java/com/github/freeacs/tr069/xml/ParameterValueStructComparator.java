package com.github.freeacs.tr069.xml;

import java.util.Comparator;

public class ParameterValueStructComparator implements Comparator<ParameterValueStruct> {
  /**
   * Effectively returns a - b; e.g. +1 (or any +ve number) if a > b 0 if a == b -1 (or any -ve
   * number) if a < b
   */
  public int compare(ParameterValueStruct a, ParameterValueStruct b) {
    return a.getName().compareTo(b.getName());
  }
}
