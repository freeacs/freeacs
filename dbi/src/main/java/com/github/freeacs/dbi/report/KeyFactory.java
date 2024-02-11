package com.github.freeacs.dbi.report;

import lombok.Getter;

import java.util.Date;

@Getter
public class KeyFactory {
  private final String[] keyNames;

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

}
