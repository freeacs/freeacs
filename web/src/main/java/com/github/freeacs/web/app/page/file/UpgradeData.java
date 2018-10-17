package com.github.freeacs.web.app.page.file;

import com.github.freeacs.web.app.input.Input;
import com.github.freeacs.web.app.input.InputData;
import java.util.Map;

/** The Class UpgradeData. */
public class UpgradeData extends InputData {
  /** The unittype cache. */
  private Input unittypeCache = Input.getStringInput("unittype-cache");
  /** The firmware. */
  private Input firmware = Input.getStringInput("firmware");
  /** The firmware cache. */
  private Input firmwareCache = Input.getStringInput("firmware-cache");
  /** The upgrade type. */
  private Input upgradeType = Input.getStringInput("type");
  /** The upgrade type cache. */
  private Input upgradeTypeCache = Input.getStringInput("upgradetype-cache");
  /** The search. */
  private Input search = Input.getStringInput("search");
  /** The search cache. */
  private Input searchCache = Input.getStringInput("search-cache");
  /** The url. */
  private Input url = Input.getStringInput("url");
  /** The version. */
  private Input version = Input.getStringInput("version");
  /** The parameters. */
  private Input parameters = Input.getStringInput("parameters");

  /**
   * Sets the firmware.
   *
   * @param firmware the new firmware
   */
  public void setFirmware(Input firmware) {
    this.firmware = firmware;
  }

  /**
   * Gets the firmware.
   *
   * @return the firmware
   */
  public Input getFirmware() {
    return firmware;
  }

  /**
   * Sets the upgrade type.
   *
   * @param upgradeType the new upgrade type
   */
  public void setUpgradeType(Input upgradeType) {
    this.upgradeType = upgradeType;
  }

  /**
   * Gets the upgrade type.
   *
   * @return the upgrade type
   */
  public Input getUpgradeType() {
    return upgradeType;
  }

  /**
   * Sets the search.
   *
   * @param search the new search
   */
  public void setSearch(Input search) {
    this.search = search;
  }

  /**
   * Gets the search.
   *
   * @return the search
   */
  public Input getSearch() {
    return search;
  }

  /**
   * Sets the url.
   *
   * @param url the new url
   */
  public void setUrl(Input url) {
    this.url = url;
  }

  /**
   * Gets the url.
   *
   * @return the url
   */
  public Input getUrl() {
    return url;
  }

  /**
   * Sets the unittype cache.
   *
   * @param unittypeCache the new unittype cache
   */
  public void setUnittypeCache(Input unittypeCache) {
    this.unittypeCache = unittypeCache;
  }

  /**
   * Gets the unittype cache.
   *
   * @return the unittype cache
   */
  public Input getUnittypeCache() {
    return unittypeCache;
  }

  /**
   * Sets the upgrade type cache.
   *
   * @param upgradeTypeCache the new upgrade type cache
   */
  public void setUpgradeTypeCache(Input upgradeTypeCache) {
    this.upgradeTypeCache = upgradeTypeCache;
  }

  /**
   * Gets the upgrade type cache.
   *
   * @return the upgrade type cache
   */
  public Input getUpgradeTypeCache() {
    return upgradeTypeCache;
  }

  /**
   * Sets the firmware cache.
   *
   * @param firmwareCache the new firmware cache
   */
  public void setFirmwareCache(Input firmwareCache) {
    this.firmwareCache = firmwareCache;
  }

  /**
   * Gets the firmware cache.
   *
   * @return the firmware cache
   */
  public Input getFirmwareCache() {
    return firmwareCache;
  }

  /**
   * Gets the version.
   *
   * @return the version
   */
  public Input getVersion() {
    return version;
  }

  /**
   * Sets the version.
   *
   * @param version the new version
   */
  public void setVersion(Input version) {
    this.version = version;
  }

  /**
   * Sets the parameters.
   *
   * @param parameters the new parameters
   */
  public void setParameters(Input parameters) {
    this.parameters = parameters;
  }

  /**
   * Gets the parameters.
   *
   * @return the parameters
   */
  public Input getParameters() {
    return parameters;
  }

  /**
   * Sets the search cache.
   *
   * @param searchCache the new search cache
   */
  public void setSearchCache(Input searchCache) {
    this.searchCache = searchCache;
  }

  /**
   * Gets the search cache.
   *
   * @return the search cache
   */
  public Input getSearchCache() {
    return searchCache;
  }

  @Override
  public void bindForm(Map<String, Object> root) {}

  @Override
  public boolean validateForm() {
    return false;
  }
}
