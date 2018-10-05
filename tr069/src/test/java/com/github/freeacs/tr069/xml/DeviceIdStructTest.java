package com.github.freeacs.tr069.xml;

import org.junit.Assert;
import org.junit.Test;

public class DeviceIdStructTest {

  @Test
  public void parseUnittypeName() {
    // Given:
    String unittypeNameStr = "ODU/xxx/yyy";

    // When:
    DeviceIdStruct struct = new DeviceIdStruct();
    struct.setProductClass(unittypeNameStr);

    // Then:
    Assert.assertEquals("ODU-xxx-yyy", struct.getProductClass());
  }

  @Test
  public void mustSupportNull() {
    // Given:
    String unittypeNameStr = null;

    // When:
    DeviceIdStruct struct = new DeviceIdStruct();
    struct.setProductClass(unittypeNameStr);

    // Then:
    Assert.assertNull(struct.getProductClass());
  }
}
