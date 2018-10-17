package com.github.freeacs.stun;

import com.typesafe.config.Config;

public class Properties {
  private final boolean expectPortForwarding;
  private final boolean runWithStun;
  private final String secondaryIp;
  private final String primaryIp;
  private final Integer secondaryPort;
  private final Integer primaryPort;
  private final Integer kickInterval;
  private final boolean checkPublicIp;
  private final Integer kickRescan;
  private final String contextPath;

  private final Config environment;

  public Properties(Config config) {
    this.environment = config;
    contextPath = getOrDefault("server.servlet.context-path", "/");
    kickRescan = getOrDefault("kick.rescan", 60);
    checkPublicIp = getOrDefault("kick.check-public-ip", false);
    kickInterval = getOrDefault("kick.interval", 1000);
    primaryPort = getOrDefault("primary.port", 3478);
    secondaryPort = getOrDefault("secondary.port", 3478);
    primaryIp = getOrDefault("primary.ip", null);
    secondaryIp = getOrDefault("secondary.ip", null);
    runWithStun = getOrDefault("test.runwithstun", false);
    expectPortForwarding = getOrDefault("kick.expect-port-forwarding", false);
  }

  @SuppressWarnings("unchecked")
  private <T> T getOrDefault(String key, T defaultValue) {
    Object obj = environment.hasPath(key) ? environment.getAnyRef(key) : null;
    if (obj != null) {
      return (T) obj;
    }
    return defaultValue;
  }

  public boolean isExpectPortForwarding() {
    return expectPortForwarding;
  }

  public boolean isRunWithStun() {
    return runWithStun;
  }

  public String getSecondaryIp() {
    return secondaryIp;
  }

  public String getPrimaryIp() {
    return primaryIp;
  }

  public Integer getSecondaryPort() {
    return secondaryPort;
  }

  public Integer getPrimaryPort() {
    return primaryPort;
  }

  public Integer getKickInterval() {
    return kickInterval;
  }

  public boolean isCheckPublicIp() {
    return checkPublicIp;
  }

  public Integer getKickRescan() {
    return kickRescan;
  }

  public Config getEnvironment() {
    return environment;
  }

  public String getContextPath() {
    return contextPath;
  }
}
