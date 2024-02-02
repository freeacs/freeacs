package com.github.freeacs.web.app.page.syslog;

import com.github.freeacs.web.app.input.Input;
import com.github.freeacs.web.app.input.InputData;
import com.github.freeacs.web.app.util.DateUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/** The Class SyslogData. */
@Getter
@Setter
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

  @Override
  public void bindForm(Map<String, Object> root) {}

  @Override
  public boolean validateForm() {
    return false;
  }
}
