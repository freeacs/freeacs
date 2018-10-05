package com.github.freeacs.base.db;

import static org.junit.Assert.*;

import org.junit.Test;

public class DBAccessSessionTR069Test {

  @Test
  public void parseUnittypeName() {
    // Given:
    String unittypeNameStr = "ODU/xxx/yyy";

    // When:
    String parsedUnittypeName = DBAccessSessionTR069.parseUnittypeName(unittypeNameStr);

    // Then:
    assertEquals("ODU-xxx-yyy", parsedUnittypeName);
  }
}
