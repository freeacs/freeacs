package com.github.freeacs.web.app.page.file;

import com.github.freeacs.web.app.input.Input;
import com.github.freeacs.web.app.input.InputData;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/** The Class UpgradeData. */
@Setter
@Getter
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

  @Override
  public void bindForm(Map<String, Object> root) {}

  @Override
  public boolean validateForm() {
    return false;
  }
}
