package com.github.freeacs.stun;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class KickTest {

  private boolean checkPublicIp;

  @Before
  public void init() {
    checkPublicIp = Properties.CHECK_PUBLIC_IP;
  }

  @After
  public void clean() {
    Properties.CHECK_PUBLIC_IP = checkPublicIp;
  }

  @Test
  public void testPublicIpCheckEnabled() throws MalformedURLException {
    // When:
    Properties.CHECK_PUBLIC_IP = true;
    String ip = "http://192.168.0.1";

    // When:
    boolean isPublic = new Kick().checkIfPublicIP(ip);

    // Then:
    assertFalse(isPublic);
  }

  @Test
  public void testPublicIpCheckDisabled() throws MalformedURLException {
    // When:
    Properties.CHECK_PUBLIC_IP = false;
    String ip = "http://192.168.0.1";

    // When:
    boolean isPublic = new Kick().checkIfPublicIP(ip);

    // Then:
    assertTrue(isPublic);
  }
}
