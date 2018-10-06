package com.github.freeacs.common.util;

import static org.junit.Assert.assertFalse;

import org.junit.Test;

public class IPAddressTest {

  @Test
  public void isNotPublic1() {
    // Given:
    String ip = "192.168.0.11";

    // When:
    boolean isPublic = IPAddress.isPublic(ip);

    // Then
    assertFalse(isPublic);
  }

  @Test
  public void isNotPublic2() {
    // Given:
    String ip = "172.16.0.11";

    // When:
    boolean isPublic = IPAddress.isPublic(ip);

    // Then
    assertFalse(isPublic);
  }

  @Test
  public void isNotPublic3() {
    // Given:
    String ip = "10.0.0.11";

    // When:
    boolean isPublic = IPAddress.isPublic(ip);

    // Then
    assertFalse(isPublic);
  }
}
