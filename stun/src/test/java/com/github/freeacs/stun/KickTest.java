package com.github.freeacs.stun;

import com.typesafe.config.ConfigFactory;
import org.junit.After;
import org.junit.Test;

import java.net.MalformedURLException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class KickTest {

    @After
    public void after() {
        Properties.config = ConfigFactory.load();
    }

    @Test
    public void testPublicIpCheckEnabled() throws MalformedURLException {
        // When:
        Properties.config = ConfigFactory.load("ip-check-enabled.properties");
        String ip = "http://192.168.0.1";

        // When:
        boolean isPublic = Kick.checkIfPublicIP(ip);

        // Then:
        assertFalse(isPublic);
    }

    @Test
    public void testPublicIpCheckDisabled() throws MalformedURLException {
        // When:
        Properties.config = ConfigFactory.load("ip-check-disabled.properties");
        String ip = "http://192.168.0.1";

        // When:
        boolean isPublic = Kick.checkIfPublicIP(ip);

        // Then:
        assertTrue(isPublic);
    }
}
