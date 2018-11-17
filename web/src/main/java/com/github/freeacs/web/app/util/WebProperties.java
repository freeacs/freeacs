package com.github.freeacs.web.app.util;

import java.util.Collections;
import java.util.Map;

import com.typesafe.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Takes care of retrieving properties from a property file. If the same property file is requested
 * the previous WebProperties instance with this propertyfile will be returned. Can easily be
 * extended to control the property values by enforcing strong quality rules. As an example: Like
 * checking the consistency for a database connection url.
 *
 * @author Jarl Andre Hubenthal
 */
public class WebProperties {
  public static String SYSLOG_SERVER_HOST;
  public static String KEYSTORE_PASS;
  public static String MONITOR_LOCATION;
  public static String LOCALE;
  public static boolean JAVASCRIPT_DEBUG;
  public static boolean IX_EDIT_ENABLED;
  public static boolean CONFIRM_CHANGES;
  public static String PROPERTIES;
  public static boolean UNIT_CONFIG_AUTOFILTER;
  public static boolean CONFIDENTIALS_RESTRICTED;
  public static boolean GZIP_ENABLED;
  public static boolean DEBUG;
  public static boolean SHOW_VOIP;
  public static boolean SHOW_HARDWARE;
  public static Integer SESSION_TIMEOUT;
  public static String CONTEXT_PATH;
  public static Integer SERVER_PORT;

  public WebProperties(Config config) {
    setSessionTimeout(config.hasPath("session.timeout") ? config.getInt("session.timeout") : 60);
    setShowHardware(config.hasPath("unit.dash.hardware") && config.getBoolean("unit.dash.hardware"));
    setShowVoip(config.hasPath("unit.dash.voip") && config.getBoolean("unit.dash.voip"));
    setDebug(config.hasPath("debug") && config.getBoolean("debug"));
    setGzipEnabled(!config.hasPath("gzip.enabled") || config.getBoolean("gzip.enabled"));
    setConfidentialsRestricted(config.hasPath("confidentials.restricted") && config.getBoolean("confidentials.restricted"));
    setUnitConfigAutofilter(config.hasPath("unit.config.autofilter") && config.getBoolean("unit.config.autofilter"));
    setProperties(config.hasPath("properties") ? config.getString("properties") : "default");
    setConfirmChanges(config.hasPath("confirmchanges") && config.getBoolean("confirmchanges"));
    setIxEditEnabled(config.hasPath("ixedit.enabled") && config.getBoolean("ixedit.enabled"));
    setJavascriptDebug(config.hasPath("javascript.debug") && config.getBoolean("javascript.debug"));
    setLocale(config.hasPath("locale") ? config.getString("locale") : null);
    setMonitorLocation(config.hasPath("monitor.location") ? config.getString("monitor.location") : null);
    setKeyStorePass(config.hasPath("keystore.pass") ? config.getString("keystore.pass") : "changeit");
    setContextPath(config.hasPath("server.servlet.context-path") ? config.getString("server.servlet.context-path") : "/");
    setServerPort(config.hasPath("server.port") ? config.getInt("server.port") : 8080);
    setSyslogServerHost(config.hasPath("syslog.server.host") ? config.getString("syslog.server.host") : "localhost");
  }

  private void setSyslogServerHost(String syslogServerHost) {
    SYSLOG_SERVER_HOST = syslogServerHost;
  }
  private void setContextPath(String contextPath) {
    CONTEXT_PATH = contextPath;
  }
  private void setServerPort(Integer port) {
    SERVER_PORT = port;
  }
  private void setSessionTimeout(Integer timeout) {
    SESSION_TIMEOUT = timeout;
  }
  private void setShowHardware(Boolean showHardware) {
    SHOW_HARDWARE = showHardware;
  }
  private void setShowVoip(Boolean showVoip) {
    SHOW_VOIP = showVoip;
  }
  private void setDebug(Boolean debug) {
    DEBUG = debug;
  }
  private void setGzipEnabled(Boolean enabled) {
    GZIP_ENABLED = enabled;
  }
  private void setConfidentialsRestricted(Boolean restricted) {
    CONFIDENTIALS_RESTRICTED = restricted;
  }
  private void setUnitConfigAutofilter(Boolean autofilter) {
    UNIT_CONFIG_AUTOFILTER = autofilter;
  }
  private void setProperties(String properties) {
    PROPERTIES = properties;
  }
  private void setConfirmChanges(Boolean confirmChanges) {
    CONFIRM_CHANGES = confirmChanges;
  }
  private void setIxEditEnabled(Boolean enabled) {
    IX_EDIT_ENABLED = enabled;
  }
  private void setJavascriptDebug(Boolean debug) {
    JAVASCRIPT_DEBUG = debug;
  }
  private void setLocale(String locale) {
    LOCALE = locale;
  }
  private void setMonitorLocation(String monitorLocation) {
    MONITOR_LOCATION = monitorLocation;
  }
  private void setKeyStorePass(String keyStorePass) {
    KEYSTORE_PASS = keyStorePass;
  }

  /**
   * Gives the customization to the dash display as defined in the web properties file.
   *
   * @param unittypeName The unit type name to find settings for
   * @return List of CustomDashDisplayProperty containing the settings
   */
  public static Map<String, String> getCustomDash(String unittypeName) {
    // TODO should this be reimplemented?
    return Collections.emptyMap();
  }
}
