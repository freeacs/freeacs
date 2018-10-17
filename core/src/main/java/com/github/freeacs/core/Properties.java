package com.github.freeacs.core;

import com.typesafe.config.Config;
import java.util.HashMap;
import java.util.Map;

public class Properties {
  private final String reports;
  private final boolean staging;
  private final Integer shellScriptLimit;
  private final Integer completedJobLimit;
  private final Integer shellScriptPoolSize;
  private final String syslogCleanup;
  private final Config environment;
  private final String contextPath;
  private final Map<Integer, Integer> syslogSeverityLimit;

  public Properties(Config config) {
    this.environment = config;
    this.contextPath = getOrDefault("server.servlet.context-path", "/");
    this.syslogCleanup = getOrDefault("syslog.cleanup", "normal");
    this.completedJobLimit = getOrDefault("completed.job.limit", 40);
    this.shellScriptPoolSize = getOrDefault("shellscript.poolsize", 4);
    this.shellScriptLimit = getOrDefault("shellscript.limit", 7);
    this.reports = getOrDefault("reports", "Basic");
    this.staging = getOrDefault("staging", false);
    this.syslogSeverityLimit = new HashMap<>();
    Config syslogConfig = environment.getConfig("syslog.severity");
    for (int i = 0; syslogConfig.hasPath(i + ".limit"); i++) {
      syslogSeverityLimit.put(i, syslogConfig.getInt(i + ".limit"));
    }
  }

  @SuppressWarnings("unchecked")
  private <T> T getOrDefault(String key, T defaultValue) {
    Object obj = environment.hasPath(key) ? environment.getAnyRef(key) : null;
    if (obj != null) {
      return (T) obj;
    }
    return defaultValue;
  }

  public Integer getSyslogSeverityLimit(int severity) {
    return syslogSeverityLimit.get(severity);
  }

  public String getContextPath() {
    return contextPath;
  }

  public String getReports() {
    return reports;
  }

  public boolean isStaging() {
    return staging;
  }

  public Integer getShellScriptLimit() {
    return shellScriptLimit;
  }

  public Integer getCompletedJobLimit() {
    return completedJobLimit;
  }

  public Integer getShellScriptPoolSize() {
    return shellScriptPoolSize;
  }

  public String getSyslogCleanup() {
    return syslogCleanup;
  }
}
