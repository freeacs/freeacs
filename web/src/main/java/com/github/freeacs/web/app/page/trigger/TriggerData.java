package com.github.freeacs.web.app.page.trigger;

import com.github.freeacs.web.app.input.Input;
import com.github.freeacs.web.app.input.InputData;
import java.util.Map;

public class TriggerData extends InputData {
  private Input triggerId = Input.getIntegerInput("triggerId");
  private Input unitTypeId = Input.getIntegerInput("unitTypeId");
  private Input profileId = Input.getIntegerInput("profileId");
  private Input name = Input.getStringInput("name");
  private Input description = Input.getStringInput("description");
  private Input syslogEventId = Input.getIntegerInput("syslogEventId");
  private Input toList = Input.getStringInput("toList");
  private Input evalPeriodMinutes = Input.getIntegerInput("evalPeriodMinutes");
  private Input numberTotal = Input.getIntegerInput("noTotal");
  private Input numerPerUnit = Input.getIntegerInput("noPrUnit");
  private Input numberOfUnits = Input.getIntegerInput("noUnits");
  private Input notifyIntervalHours = Input.getIntegerInput("notifyIntervalHours");
  private Input triggerType = Input.getIntegerInput("typeTrigger");
  private Input parentTrigger = Input.getIntegerInput("parentTrigger");
  private Input notifyType = Input.getIntegerInput("notifyType");
  private Input action = Input.getStringInput("action");
  private Input scriptFileId = Input.getIntegerInput("scriptFileId");
  private Input active = Input.getBooleanInput("active");

  @Override
  protected void bindForm(Map<String, Object> root) {}

  @Override
  protected boolean validateForm() {
    return false;
  }

  public Input getUnitTypeId() {
    return unitTypeId;
  }

  public void setUnitTypeId(Input unitTypeId) {
    this.unitTypeId = unitTypeId;
  }

  public Input getProfileId() {
    return profileId;
  }

  public void setProfileId(Input profileId) {
    this.profileId = profileId;
  }

  public Input getName() {
    return name;
  }

  public void setName(Input name) {
    this.name = name;
  }

  public Input getDescription() {
    return description;
  }

  public void setDescription(Input description) {
    this.description = description;
  }

  public Input getSyslogEventId() {
    return syslogEventId;
  }

  public void setSyslogEventId(Input syslogEventId) {
    this.syslogEventId = syslogEventId;
  }

  public Input getToList() {
    return toList;
  }

  public void setToList(Input toList) {
    this.toList = toList;
  }

  public Input getEvalPeriodMinutes() {
    return evalPeriodMinutes;
  }

  public void setEvalPeriodMinutes(Input evalPeriodMinutes) {
    this.evalPeriodMinutes = evalPeriodMinutes;
  }

  public Input getNumberTotal() {
    return numberTotal;
  }

  public void setNumberTotal(Input numberTotal) {
    this.numberTotal = numberTotal;
  }

  public Input getNumberPerUnit() {
    return numerPerUnit;
  }

  public void setNumerPerUnit(Input numerPerUnit) {
    this.numerPerUnit = numerPerUnit;
  }

  public Input getNumberOfUnits() {
    return numberOfUnits;
  }

  public void setNumberOfUnits(Input numberOfUnits) {
    this.numberOfUnits = numberOfUnits;
  }

  public Input getNotifyIntervalHours() {
    return notifyIntervalHours;
  }

  public void setNotifyIntervalHours(Input notifyIntervalHours) {
    this.notifyIntervalHours = notifyIntervalHours;
  }

  public Input getTriggerId() {
    return triggerId;
  }

  public void setTriggerId(Input triggerId) {
    this.triggerId = triggerId;
  }

  public Input getTriggerType() {
    return triggerType;
  }

  public void setTriggerType(Input triggerType) {
    this.triggerType = triggerType;
  }

  public Input getParentTrigger() {
    return parentTrigger;
  }

  public void setParentTrigger(Input parentTrigger) {
    this.parentTrigger = parentTrigger;
  }

  public Input getNotifyType() {
    return notifyType;
  }

  public void setNotifyType(Input notifyType) {
    this.notifyType = notifyType;
  }

  public Input getAction() {
    return action;
  }

  public void setAction(Input action) {
    this.action = action;
  }

  public Input getScriptFileId() {
    return scriptFileId;
  }

  public void setScriptFileId(Input scriptFileId) {
    this.scriptFileId = scriptFileId;
  }

  public Input getActive() {
    return active;
  }

  public void setActive(Input active) {
    this.active = active;
  }
}
