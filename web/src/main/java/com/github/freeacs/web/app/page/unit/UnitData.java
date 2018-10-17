package com.github.freeacs.web.app.page.unit;

import com.github.freeacs.web.app.input.Input;
import com.github.freeacs.web.app.input.InputData;
import java.util.Map;

public class UnitData extends InputData {
  private Input unitParameter = Input.getStringInput("unitparameter");
  private Input unitParameterValue = Input.getStringInput("unitparametervalue");
  private Input filter = Input.getStringInput("filter");
  private Input cmd = Input.getStringInput("cmd");
  private Input initProvisioning = Input.getStringInput("init_provisioning");
  private Input initReadAll = Input.getStringInput("init_readall");
  private Input initRestart = Input.getStringInput("init_restart");
  private Input initReset = Input.getStringInput("init_reset");
  private Input initRefreshPage = Input.getStringInput("init_refreshpage");
  private Input frequency = Input.getStringInput("frequency");
  private Input spread = Input.getStringInput("spread");
  private Input changeFreqSpread = Input.getStringInput("change_freqspread");
  private Input unitUpgrade = Input.getStringArrayInput("unitupgrade");
  private Input showConfidential = Input.getBooleanInput("show_confidential");
  /**
   * Private Input modeSelect = Input.getStringInput("modeselect"); private Input inspectAction =
   * Input.getStringInput("inspectaction");
   */
  private Input unitDelete = Input.getStringInput("unitdelete");

  private Input unitMove = Input.getStringInput("unitmove");
  private Input unitReset = Input.getStringInput("unitreset");
  private Input newUnit = Input.getStringInput("new_unit");

  public Input getNewUnit() {
    return newUnit;
  }

  public void setNewUnit(Input newUnit) {
    this.newUnit = newUnit;
  }

  public Input getUnitParameter() {
    return unitParameter;
  }

  public void setUnitParameter(Input unitParameter) {
    this.unitParameter = unitParameter;
  }

  public Input getUnitParameterValue() {
    return unitParameterValue;
  }

  public void setUnitParameterValue(Input unitParameterValue) {
    this.unitParameterValue = unitParameterValue;
  }

  public Input getFilter() {
    return filter;
  }

  public void setFilter(Input filter) {
    this.filter = filter;
  }

  public void setCmd(Input cmd) {
    this.cmd = cmd;
  }

  public Input getCmd() {
    return cmd;
  }

  /**
   * Public void setModeSelect(Input modeSelect) { this.modeSelect = modeSelect; } public Input
   * getModeSelect() { return modeSelect; } public void setInspectAction(Input inspectAction) {
   * this.inspectAction = inspectAction; } public Input getInspectAction() { return inspectAction; }
   */
  public void setUnitDelete(Input unitDelete) {
    this.unitDelete = unitDelete;
  }

  public Input getUnitDelete() {
    return unitDelete;
  }

  public void setUnitMove(Input unitMove) {
    this.unitMove = unitMove;
  }

  public Input getUnitMove() {
    return unitMove;
  }

  public void setUnitReset(Input unitReset) {
    this.unitReset = unitReset;
  }

  public Input getUnitReset() {
    return unitReset;
  }

  @Override
  public void bindForm(Map<String, Object> root) {}

  @Override
  public boolean validateForm() {
    return false;
  }

  public Input getInitProvisioning() {
    return initProvisioning;
  }

  public void setInitProvisioning(Input initProvisioning) {
    this.initProvisioning = initProvisioning;
  }

  public Input getInitReadAll() {
    return initReadAll;
  }

  public void setInitReadAll(Input initReadAll) {
    this.initReadAll = initReadAll;
  }

  public Input getInitRefreshPage() {
    return initRefreshPage;
  }

  public void setInitRefreshPage(Input initRefreshPage) {
    this.initRefreshPage = initRefreshPage;
  }

  public Input getUnitUpgrade() {
    return unitUpgrade;
  }

  public void setUnitUpgrade(Input unitUpgrade) {
    this.unitUpgrade = unitUpgrade;
  }

  public Input getShowConfidential() {
    return showConfidential;
  }

  public void setShowConfidential(Input showConfidential) {
    this.showConfidential = showConfidential;
  }

  public Input getInitRestart() {
    return initRestart;
  }

  public void setInitRestart(Input initRestart) {
    this.initRestart = initRestart;
  }

  public Input getInitReset() {
    return initReset;
  }

  public void setInitReset(Input initReset) {
    this.initReset = initReset;
  }

  public Input getFrequency() {
    return frequency;
  }

  public void setFrequency(Input frequency) {
    this.frequency = frequency;
  }

  public Input getSpread() {
    return spread;
  }

  public void setSpread(Input spread) {
    this.spread = spread;
  }

  public Input getChangeFreqSpread() {
    return changeFreqSpread;
  }

  public void setChangeFreqSpread(Input changeFreqSpread) {
    this.changeFreqSpread = changeFreqSpread;
  }
}
