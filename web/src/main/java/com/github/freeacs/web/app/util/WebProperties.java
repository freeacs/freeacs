package com.github.freeacs.web.app.util;

import com.typesafe.config.Config;
import java.util.Collections;
import java.util.Map;

/**
 * Takes care of retrieving properties from a property file. If the same property file is requested
 * the previous WebProperties instance with this propertyfile will be returned. Can easily be
 * extended to control the property values by enforcing strong quality rules. As an example: Like
 * checking the consistency for a database connection url.
 *
 * @author Jarl Andre Hubenthal
 */
public class WebProperties {
  private String syslogServerHost;
  private String keystorePass;
  private String monitorLocation;
  private String locale;
  private boolean javascriptDebug;
  private boolean ixEditEnabled;
  private boolean confirmChanges;
  private String properties;
  private boolean unitConfigAutofilter;
  private boolean confidentialsRestricted;
  private boolean gzipEnabled;
  private boolean debug;
  private boolean showVoip;
  private boolean showHardware;
  private Integer sessionTimeout;
  private String contextPath;
  private Integer serverPort;

  private static WebProperties instance;

  public WebProperties(Config config) {
    setSessionTimeout(config.hasPath("session.timeout") ? config.getInt("session.timeout") : 60);
    setShowHardware(
        config.hasPath("unit.dash.hardware") && config.getBoolean("unit.dash.hardware"));
    setShowVoip(config.hasPath("unit.dash.voip") && config.getBoolean("unit.dash.voip"));
    setDebug(config.hasPath("debug") && config.getBoolean("debug"));
    setGzipEnabled(!config.hasPath("gzip.enabled") || config.getBoolean("gzip.enabled"));
    setConfidentialsRestricted(
        config.hasPath("confidentials.restricted")
            && config.getBoolean("confidentials.restricted"));
    setUnitConfigAutofilter(
        config.hasPath("unit.config.autofilter") && config.getBoolean("unit.config.autofilter"));
    setProperties(config.hasPath("properties") ? config.getString("properties") : "default");
    setConfirmChanges(config.hasPath("confirmchanges") && config.getBoolean("confirmchanges"));
    setIxEditEnabled(config.hasPath("ixedit.enabled") && config.getBoolean("ixedit.enabled"));
    setJavascriptDebug(config.hasPath("javascript.debug") && config.getBoolean("javascript.debug"));
    setLocale(config.hasPath("locale") ? config.getString("locale") : null);
    setMonitorLocation(
        config.hasPath("monitor.location") ? config.getString("monitor.location") : null);
    setKeyStorePass(
        config.hasPath("keystore.pass") ? config.getString("keystore.pass") : "changeit");
    setContextPath(
        config.hasPath("server.servlet.context-path")
            ? config.getString("server.servlet.context-path")
            : "/");
    setServerPort(config.hasPath("server.port") ? config.getInt("server.port") : 8080);
    setSyslogServerHost(
        config.hasPath("syslog.server.host")
            ? config.getString("syslog.server.host")
            : "localhost");
    instance = this;
  }

  public static WebProperties getInstance() {
    return instance;
  }

  private void setSyslogServerHost(String syslogServerHost) {
    this.syslogServerHost = syslogServerHost;
  }

  private void setContextPath(String contextPath) {
    this.contextPath = contextPath;
  }

  private void setServerPort(Integer port) {
    serverPort = port;
  }

  private void setSessionTimeout(Integer timeout) {
    sessionTimeout = timeout;
  }

  private void setShowHardware(Boolean showHardware) {
    this.showHardware = showHardware;
  }

  private void setShowVoip(Boolean showVoip) {
    this.showVoip = showVoip;
  }

  private void setDebug(Boolean debug) {
    this.debug = debug;
  }

  private void setGzipEnabled(Boolean enabled) {
    gzipEnabled = enabled;
  }

  private void setConfidentialsRestricted(Boolean restricted) {
    confidentialsRestricted = restricted;
  }

  private void setUnitConfigAutofilter(Boolean autofilter) {
    unitConfigAutofilter = autofilter;
  }

  private void setProperties(String properties) {
    this.properties = properties;
  }

  private void setConfirmChanges(Boolean confirmChanges) {
    this.confirmChanges = confirmChanges;
  }

  private void setIxEditEnabled(Boolean enabled) {
    ixEditEnabled = enabled;
  }

  private void setJavascriptDebug(Boolean debug) {
    javascriptDebug = debug;
  }

  private void setLocale(String locale) {
    this.locale = locale;
  }

  private void setMonitorLocation(String monitorLocation) {
    this.monitorLocation = monitorLocation;
  }

  private void setKeyStorePass(String keyStorePass) {
    keystorePass = keyStorePass;
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

  public String getSyslogServerHost() {
    return syslogServerHost;
  }

  public String getKeystorePass() {
    return keystorePass;
  }

  public String getMonitorLocation() {
    return monitorLocation;
  }

  public String getLocale() {
    return locale;
  }

  public boolean isJavascriptDebug() {
    return javascriptDebug;
  }

  public boolean isIxEditEnabled() {
    return ixEditEnabled;
  }

  public boolean isConfirmChanges() {
    return confirmChanges;
  }

  public String getProperties() {
    return properties;
  }

  public boolean isUnitConfigAutofilter() {
    return unitConfigAutofilter;
  }

  public boolean isConfidentialsRestricted() {
    return confidentialsRestricted;
  }

  public boolean isGzipEnabled() {
    return gzipEnabled;
  }

  public boolean isDebug() {
    return debug;
  }

  public boolean isShowVoip() {
    return showVoip;
  }

  public boolean isShowHardware() {
    return showHardware;
  }

  public Integer getSessionTimeout() {
    return sessionTimeout;
  }

  public String getContextPath() {
    return contextPath;
  }

  public Integer getServerPort() {
    return serverPort;
  }
}
