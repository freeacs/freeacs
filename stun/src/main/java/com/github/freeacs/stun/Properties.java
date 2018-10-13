package com.github.freeacs.stun;

import com.typesafe.config.Config;

public class Properties {

  public static boolean EXPECT_PORT_FORWARDING;
  public static boolean RUN_WITH_STUN;
  public static String SECONDARY_IP;
  public static String PRIMARY_IP;
  public static Integer SECONDARY_PORT;
  public static Integer PRIMARY_PORT;
  public static Integer KICK_INTERVAL;
  public static boolean CHECK_PUBLIC_IP;
  public static Integer KICK_RESCAN;

  private final Config environment;

  public Properties(Config config) {
    this.environment = config;
    KICK_RESCAN = getOrDefault("kick.rescan", 60);
    CHECK_PUBLIC_IP = getOrDefault("kick.check-public-ip", false);
    KICK_INTERVAL = getOrDefault("kick.interval", 1000);
    PRIMARY_PORT = getOrDefault("primary.port", 3478);
    SECONDARY_PORT = getOrDefault("secondary.port", 3478);
    PRIMARY_IP = getOrDefault("primary.ip", null);
    SECONDARY_IP = getOrDefault("secondary.ip", null);
    RUN_WITH_STUN = getOrDefault("test.runwithstun", false);
    EXPECT_PORT_FORWARDING = getOrDefault("kick.expect-port-forwarding", false);
  }

  @SuppressWarnings("unchecked")
  private <T> T getOrDefault(String key, T defaultValue) {
    Object obj = environment.hasPath(key) ? environment.getAnyRef(key) : null;
    if (obj == null) {
      return defaultValue;
    }
    return (T) obj;
  }
}
