package com.github.freeacs.dbi.report;

import java.util.Date;

public class KeyFactory {
  private String[] keyNames;

  public KeyFactory(String... keyNames) {
    this.keyNames = keyNames;
  }

  public Key makeKey(Date tms, PeriodType periodType, String... values) {
    if (values.length != keyNames.length) {
      throw new IllegalArgumentException(
          "The number of values differ from the number of names defined in this factory");
    } else {
      KeyElement[] keyElements = new KeyElement[values.length];
      for (int i = 0; i < values.length; i++) {
        keyElements[i] = new KeyElement(keyNames[i], values[i]);
      }
      return new Key(tms, periodType, keyElements);
    }
  }

  public String[] getKeyNames() {
    return keyNames;
  }
}
