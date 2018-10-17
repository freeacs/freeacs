package com.github.freeacs.tr069;

import com.github.freeacs.base.Log;
import com.typesafe.config.Config;
import org.apache.commons.lang3.StringUtils;

public class Properties {
  public static String DIGEST_SECRET;
  public static boolean FILE_AUTH_USED;
  public static boolean DISCOVERY_MODE;
  public static String[] DISCOVERY_BLOCK;
  public static String AUTH_METHOD;
  public static int CONCURRENT_DOWNLOAD_LIMIT;
  public static String PUBLIC_URL;

  private Config environment;
  private String contextPath;

  public Properties(Config config) {
    this.environment = config;
    setAuthMethod(config.getString("auth.method"));
    setFileAuthUsed(config.getBoolean("file.auth.used"));
    setPublicUrl(config.getString("public.url"));
    setDigestSecret(config.getString("digest.secret"));
    setDiscoveryMode(config.getBoolean("discovery.mode"));
    setDiscoveryBlock(getOrDefault("discovery.block", null));
    setConcurrentDownloadLimit(getOrDefault("concurrent.download.limit", 50));
    setContextPath(getOrDefault("server.servlet.context-path", "/"));
  }

  private <T> T getOrDefault(String key, T defaultValue) {
    Object obj = environment.hasPath(key) ? environment.getAnyRef(key) : null;
    if (obj != null) {
      return (T) obj;
    }
    return defaultValue;
  }

  private void setContextPath(String contextPath) {
    this.contextPath = contextPath;
  }

  private void setConcurrentDownloadLimit(Integer concurrentDownloadLimit) {
    CONCURRENT_DOWNLOAD_LIMIT = concurrentDownloadLimit;
  }

  private void setFileAuthUsed(Boolean fileAuthUsed) {
    FILE_AUTH_USED = fileAuthUsed;
  }

  private void setAuthMethod(String authMethod) {
    AUTH_METHOD = authMethod;
  }

  private void setPublicUrl(String url) {
    PUBLIC_URL = url;
  }

  private void setDigestSecret(String digestSecret) {
    DIGEST_SECRET = digestSecret;
  }

  private void setDiscoveryMode(Boolean discoveryMode) {
    DISCOVERY_MODE = discoveryMode;
  }

  private void setDiscoveryBlock(String discoveryBlock) {
    DISCOVERY_BLOCK =
        StringUtils.isEmpty(discoveryBlock) ? new String[0] : discoveryBlock.split("\\s*,\\s*");
  }

  public boolean isParameterkeyQuirk(SessionData sessionData) {
    return isQuirk("parameterkey", sessionData.getUnittypeName(), sessionData.getVersion());
  }

  public boolean isUnitDiscovery(SessionData sessionData) {
    return isQuirk("unitdiscovery", sessionData.getUnittypeName(), sessionData.getVersion());
  }

  public boolean isTerminationQuirk(SessionData sessionData) {
    return isQuirk("termination", sessionData.getUnittypeName(), sessionData.getVersion());
  }

  public boolean isPrettyPrintQuirk(SessionData sessionData) {
    return isQuirk("prettyprint", sessionData.getUnittypeName(), sessionData.getVersion());
  }

  public boolean isIgnoreVendorConfigFile(SessionData sessionData) {
    return isQuirk(
        "ignorevendorconfigfile", sessionData.getUnittypeName(), sessionData.getVersion());
  }

  public boolean isNextLevel0InGPN(SessionData sessionData) {
    return isQuirk("nextlevel0ingpn", sessionData.getUnittypeName(), sessionData.getVersion());
  }

  private boolean isQuirk(String quirkName, String unittypeName, String version) {
    if (unittypeName == null) {
      Log.debug(
          Properties.class,
          "The unittypename (null) could not be found. The quirk "
              + quirkName
              + " will return default false");
      return false;
    }
    for (String quirk : getQuirks(unittypeName, version)) {
      if (quirk.equals(quirkName)) {
        return true;
      }
    }
    return false;
  }

  private String[] getQuirks(String unittypeName, String version) {
    String quirks = null;
    if (version != null && environment.hasPath("quirks." + unittypeName + "." + version)) {
      quirks = environment.getString("quirks." + unittypeName + "." + version);
    }
    if (quirks == null && environment.hasPath("quirks." + unittypeName)) {
      quirks = environment.getString("quirks." + unittypeName);
    }
    if (quirks != null) {
      return quirks.split("\\s*,\\s*");
    } else {
      return new String[0];
    }
  }

  public String getContextPath() {
    return contextPath;
  }
}
