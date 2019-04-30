package com.github.freeacs.tr069;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Data
@Slf4j
@Component
public class Properties {
  private String digestSecret;
  private boolean fileAuthUsed;
  private boolean discoveryMode;
  private String[] discoveryBlock;
  private String authMethod;
  private int concurrentDownloadLimit;
  private String publicUrl;
  private boolean appendHwVersion;

  private Environment environment;

  public Properties(Environment environment) {
    this.environment = environment;
    setAuthMethod(environment.getProperty("auth.method"));
    setFileAuthUsed(environment.getProperty("file.auth.used", Boolean.class, true));
    setPublicUrl(environment.getProperty("public.url"));
    setDigestSecret(environment.getProperty("digest.secret"));
    setDiscoveryMode(environment.getProperty("discovery.mode", Boolean.class, false));
    setDiscoveryBlock(environment.getProperty("discovery.block", String.class, null));
    setConcurrentDownloadLimit(environment.getProperty("concurrent.download.limit", Integer.class, 50));
    setAppendHwVersion(environment.getProperty("unit.type.append-hw-version", Boolean.class, false));
  }

  private void setDiscoveryBlock(String discoveryBlock) {
    this.discoveryBlock =
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
      log.debug("The unittypename (null) could not be found. The quirk "
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
    if (version != null && environment.getProperty("quirks." + unittypeName + "." + version) != null) {
      quirks = environment.getProperty("quirks." + unittypeName + "." + version);
    }
    if (quirks == null && environment.getProperty("quirks." + unittypeName) != null) {
      quirks = environment.getProperty("quirks." + unittypeName);
    }
    if (quirks != null) {
      return quirks.split("\\s*,\\s*");
    } else {
      return new String[0];
    }
  }
}
