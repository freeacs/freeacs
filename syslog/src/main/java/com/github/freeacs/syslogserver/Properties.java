package com.github.freeacs.syslogserver;

import com.typesafe.config.Config;

public class Properties {
  private final Integer minSyslogdbCommitDelay;
  private final Integer maxSyslogdbCommitQueue;
  private final Integer maxMessagesPerMinute;
  private final Integer minFreeDiskSpace;
  private final String unknownUnitsAction;
  private final Integer maxFailoverMessageAge;
  private final Integer maxSyslogdbThreads;
  private final Integer maxMessagesInDuplicateBuffer;
  private final Integer maxMessagesInBuffer;
  private final Integer receiveBufferSize;
  private final Integer failoverProcessInterval;
  private final Integer port;
  private final boolean simulation;
  private final String contextPath;
  private final Config environment;

  public Properties(Config config) {
    this.environment = config;
    this.contextPath = getOrDefault("server.servlet.context-path", "/");
    this.simulation = getOrDefault("simulation", false);
    this.port = getOrDefault("port", 9116);
    this.failoverProcessInterval = getOrDefault("failover-process-interval", 30);
    this.receiveBufferSize = getOrDefault("receive-buffer-size", 10240);
    this.maxMessagesInBuffer = getOrDefault("max-messages-in-buffer", 100000);
    this.maxMessagesInDuplicateBuffer = getOrDefault("max-message-in-duplicate-buffer", 100000);
    this.maxSyslogdbThreads = getOrDefault("max-syslogdb-threads", 1);
    this.maxFailoverMessageAge = getOrDefault("max-failover-message-age", 24);
    this.unknownUnitsAction = getOrDefault("unknown-units", "discard");
    this.minFreeDiskSpace = getOrDefault("min-free-disk-space", 100);
    this.maxMessagesPerMinute = getOrDefault("max-message-pr-minute", 10000);
    this.maxSyslogdbCommitQueue = getOrDefault("max-syslog-db-commit-queue", 1000);
    this.minSyslogdbCommitDelay = getOrDefault("min-syslog-db-commit-delay", 5000);
  }

  @SuppressWarnings("unchecked")
  private <T> T getOrDefault(String key, T defaultValue) {
    Object obj = environment.hasPath(key) ? environment.getAnyRef(key) : null;
    if (obj != null) {
      return (T) obj;
    }
    return defaultValue;
  }

  public static String getDeviceIdPattern(int index) {
    // TODO should this be reimplemented?
    return null;
  }

  public String getContextPath() {
    return contextPath;
  }

  public Integer getMinSyslogdbCommitDelay() {
    return minSyslogdbCommitDelay;
  }

  public Integer getMaxSyslogdbCommitQueue() {
    return maxSyslogdbCommitQueue;
  }

  public Integer getMaxMessagesPerMinute() {
    return maxMessagesPerMinute;
  }

  public Integer getMinFreeDiskSpace() {
    return minFreeDiskSpace;
  }

  public String getUnknownUnitsAction() {
    return unknownUnitsAction;
  }

  public Integer getMaxFailoverMessageAge() {
    return maxFailoverMessageAge;
  }

  public Integer getMaxSyslogdbThreads() {
    return maxSyslogdbThreads;
  }

  public Integer getMaxMessagesInDuplicateBuffer() {
    return maxMessagesInDuplicateBuffer;
  }

  public Integer getMaxMessagesInBuffer() {
    return maxMessagesInBuffer;
  }

  public Integer getReceiveBufferSize() {
    return receiveBufferSize;
  }

  public Integer getFailoverProcessInterval() {
    return failoverProcessInterval;
  }

  public Integer getPort() {
    return port;
  }

  public boolean isSimulation() {
    return simulation;
  }
}
