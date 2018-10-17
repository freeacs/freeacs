package com.github.freeacs.web.app.page.syslog;

import com.github.freeacs.web.app.input.Input;
import com.github.freeacs.web.app.input.InputData;
import com.github.freeacs.web.app.util.DateUtils;
import java.util.Map;

/** The Class SyslogData. */
public class SyslogData extends InputData {
  /** The severity. */
  private Input severity = Input.getStringArrayInput("severity");

  /** The timestamp start. */
  private Input timestampStart = Input.getDateInput("tmsstart", DateUtils.Format.DEFAULT);

  /** The timestamp end. */
  private Input timestampEnd = Input.getDateInput("tmsend", DateUtils.Format.DEFAULT);

  /** The user id. */
  private Input userId = Input.getStringInput("userid");

  /** The message. */
  private Input message = Input.getStringInput("message");

  /** The facility. */
  private Input facility = Input.getIntegerInput("facility");

  /** The facility version. */
  private Input facilityVersion = Input.getStringInput("facility_version");

  /** The max rows. */
  private Input maxRows = Input.getIntegerInput("maxrows");

  /** The advanced. */
  private Input advanced = Input.getBooleanInput("advancedView");

  /** The event. */
  private Input event = Input.getIntegerInput("event");

  /** The ipaddress. */
  private Input ipaddress = Input.getStringInput("ipaddress");

  /** The cmd. */
  private Input cmd = Input.getStringInput("cmd");

  /** The mode. */
  private Input mode = Input.getStringInput("mode");

  /** The fromh. */
  private Input fromh = Input.getStringInput("from-hour");

  /** The fromm. */
  private Input fromm = Input.getStringInput("from-minutes");

  /** The toh. */
  private Input toh = Input.getStringInput("to-hour");

  /** The tom. */
  private Input tom = Input.getStringInput("to-minutes");

  /** The url. */
  private Input url = Input.getStringInput("url");

  /**
   * Sets the url.
   *
   * @param history the new url
   */
  public void setUrl(Input history) {
    this.url = history;
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
   * Gets the facility.
   *
   * @return the facility
   */
  public Input getFacility() {
    return facility;
  }

  /**
   * Sets the facility.
   *
   * @param facility the new facility
   */
  public void setFacility(Input facility) {
    this.facility = facility;
  }

  /**
   * Gets the message.
   *
   * @return the message
   */
  public Input getMessage() {
    return message;
  }

  /**
   * Sets the message.
   *
   * @param message the new message
   */
  public void setMessage(Input message) {
    this.message = message;
  }

  /**
   * Gets the timestamp end.
   *
   * @return the timestamp end
   */
  public Input getTimestampEnd() {
    return timestampEnd;
  }

  /**
   * Sets the timestamp end.
   *
   * @param timestampEnd the new timestamp end
   */
  public void setTimestampEnd(Input timestampEnd) {
    this.timestampEnd = timestampEnd;
  }

  /**
   * Gets the timestamp start.
   *
   * @return the timestamp start
   */
  public Input getTimestampStart() {
    return timestampStart;
  }

  /**
   * Sets the timestamp start.
   *
   * @param timestampStart the new timestamp start
   */
  public void setTimestampStart(Input timestampStart) {
    this.timestampStart = timestampStart;
  }

  /**
   * Gets the user id.
   *
   * @return the user id
   */
  public Input getUserId() {
    return userId;
  }

  /**
   * Sets the user id.
   *
   * @param userId the new user id
   */
  public void setUserId(Input userId) {
    this.userId = userId;
  }

  /**
   * Gets the max rows.
   *
   * @return the max rows
   */
  public Input getMaxRows() {
    if (maxRows.getValue() == null) {
      maxRows.setValue("100");
    }
    return maxRows;
  }

  /**
   * Sets the max rows.
   *
   * @param maxRows the new max rows
   */
  public void setMaxRows(Input maxRows) {
    this.maxRows = maxRows;
  }

  /**
   * Gets the advanced.
   *
   * @return the advanced
   */
  public Input getAdvanced() {
    return advanced;
  }

  /**
   * Sets the advanced.
   *
   * @param advanced the new advanced
   */
  public void setAdvanced(Input advanced) {
    this.advanced = advanced;
  }

  /**
   * Sets the event.
   *
   * @param event the new event
   */
  public void setEvent(Input event) {
    this.event = event;
  }

  /**
   * Gets the event.
   *
   * @return the event
   */
  public Input getEvent() {
    return event;
  }

  /**
   * Sets the severity.
   *
   * @param severity the new severity
   */
  public void setSeverity(Input severity) {
    this.severity = severity;
  }

  /**
   * Gets the severity.
   *
   * @return the severity
   */
  public Input getSeverity() {
    return severity;
  }

  /**
   * Sets the ipaddress.
   *
   * @param ipaddress the new ipaddress
   */
  public void setIpaddress(Input ipaddress) {
    this.ipaddress = ipaddress;
  }

  /**
   * Gets the ipaddress.
   *
   * @return the ipaddress
   */
  public Input getIpaddress() {
    return ipaddress;
  }

  /**
   * Sets the cmd.
   *
   * @param cmd the cmd to set
   */
  public void setCmd(Input cmd) {
    this.cmd = cmd;
  }

  /**
   * Gets the cmd.
   *
   * @return the cmd
   */
  public Input getCmd() {
    return cmd;
  }

  /**
   * Sets the mode.
   *
   * @param mode the mode to set
   */
  public void setMode(Input mode) {
    this.mode = mode;
  }

  /**
   * Gets the mode.
   *
   * @return the mode
   */
  public Input getMode() {
    return mode;
  }

  /**
   * Gets the fromh.
   *
   * @return the fromh
   */
  public Input getFromh() {
    return fromh;
  }

  /**
   * Sets the fromh.
   *
   * @param fromh the fromh to set
   */
  public void setFromh(Input fromh) {
    this.fromh = fromh;
  }

  /**
   * Gets the fromm.
   *
   * @return the fromm
   */
  public Input getFromm() {
    return fromm;
  }

  /**
   * Sets the fromm.
   *
   * @param fromm the fromm to set
   */
  public void setFromm(Input fromm) {
    this.fromm = fromm;
  }

  /**
   * Gets the toh.
   *
   * @return the toh
   */
  public Input getToh() {
    return toh;
  }

  /**
   * Sets the toh.
   *
   * @param toh the toh to set
   */
  public void setToh(Input toh) {
    this.toh = toh;
  }

  /**
   * Gets the tom.
   *
   * @return the tom
   */
  public Input getTom() {
    return tom;
  }

  /**
   * Sets the tom.
   *
   * @param tom the tom to set
   */
  public void setTom(Input tom) {
    this.tom = tom;
  }

  @Override
  public void bindForm(Map<String, Object> root) {}

  @Override
  public boolean validateForm() {
    return false;
  }

  /**
   * Gets the facility version.
   *
   * @return the facility version
   */
  public Input getFacilityVersion() {
    return facilityVersion;
  }

  /**
   * Sets the facility version.
   *
   * @param facilityVersion the new facility version
   */
  public void setFacilityVersion(Input facilityVersion) {
    this.facilityVersion = facilityVersion;
  }
}
