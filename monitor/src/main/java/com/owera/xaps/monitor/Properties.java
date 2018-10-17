package com.owera.xaps.monitor;

import com.typesafe.config.Config;

public class Properties {
  public static Long RETRY_SECS;
  public static String URL_BASE;

  private final Config config;

  public Properties(Config config) {
    this.config = config;
    URL_BASE = getMonitorURLBase();
    RETRY_SECS = getRetrySeconds();
  }

  public String getMonitorURLBase() {
    String urlBase = get("monitor.urlbase");
    if (urlBase == null) {
      return "http://localhost/";
    }
    if (!urlBase.endsWith("/")) {
      urlBase += "/";
    }
    return urlBase;
  }

  public long getRetrySeconds() {
    String prop = get("monitor.retrysec");
    try {
      return Long.parseLong(prop);
    } catch (Throwable t) {
      return 300L;
    }
  }

  public String getContextPath() {
    return config.getString("server.servlet.context-path");
  }

  public int getServerPort() {
    return config.getInt("server.port");
  }

  public String get(String key) {
    return config.hasPath(key) ? config.getString(key) : null;
  }
}
