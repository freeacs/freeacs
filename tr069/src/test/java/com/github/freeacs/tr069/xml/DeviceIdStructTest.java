package com.github.freeacs.tr069.xml;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DeviceIdStructTest {

  @Test
  public void parseUnittypeNameWithForwardSlash() {
    // Given:
    String unittypeNameStr = "ODU/xxx/yyya²";

    // When:
    DeviceIdStruct struct = new DeviceIdStruct();
    struct.setProductClass(unittypeNameStr);

    // Then:
    Assertions.assertEquals("ODU-xxx-yyya2", struct.getProductClass());
  }

  @Test
  public void parseUnittypeNameWithBackwardSlash() {
    // Given:
    String unittypeNameStr = "ODU\\xxx\\yyya²";

    // When:
    DeviceIdStruct struct = new DeviceIdStruct();
    struct.setProductClass(unittypeNameStr);

    // Then:
    Assertions.assertEquals("ODU-xxx-yyya2", struct.getProductClass());
  }


  @Test
  public void mustSupportNull() {
    // Given:
    String unittypeNameStr = null;

    // When:
    DeviceIdStruct struct = new DeviceIdStruct();
    struct.setProductClass(unittypeNameStr);

    // Then:
    Assertions.assertNull(struct.getProductClass());
  }
}
