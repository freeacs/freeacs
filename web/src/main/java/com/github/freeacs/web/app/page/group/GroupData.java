package com.github.freeacs.web.app.page.group;

import com.github.freeacs.web.app.input.Input;
import com.github.freeacs.web.app.input.InputData;
import java.util.Map;

/**
 * Group Configuration and Group Create input definition.
 *
 * @author Jarl Andre Hubenthal
 */
public class GroupData extends InputData {
  /**
   * Gets the time rolling parameter.
   *
   * @return the time rolling parameter
   */
  public Input getTimeRollingParameter() {
    return timeRollingParameter;
  }

  /**
   * Sets the time rolling parameter.
   *
   * @param timeRollingParameter the new time rolling parameter
   */
  public void setTimeRollingParameter(Input timeRollingParameter) {
    this.timeRollingParameter = timeRollingParameter;
  }

  /**
   * Gets the time rolling format.
   *
   * @return the time rolling format
   */
  public Input getTimeRollingFormat() {
    return timeRollingFormat;
  }

  /**
   * Sets the time rolling format.
   *
   * @param timeRollingFormat the new time rolling format
   */
  public void setTimeRollingFormat(Input timeRollingFormat) {
    this.timeRollingFormat = timeRollingFormat;
  }

  /**
   * Gets the time rolling offset.
   *
   * @return the time rolling offset
   */
  public Input getTimeRollingOffset() {
    return timeRollingOffset;
  }

  /**
   * Sets the time rolling offset.
   *
   * @param timeRollingOffset the new time rolling offset
   */
  public void setTimeRollingOffset(Input timeRollingOffset) {
    this.timeRollingOffset = timeRollingOffset;
  }

  /** The groupname. */
  private Input groupname = Input.getStringInput("groupname");

  /** The description. */
  private Input description = Input.getStringInput("description");

  /** The parentgroup. */
  private Input parentgroup = Input.getStringInput("parentgroup");

  /** The cmd. */
  private Input cmd = Input.getStringInput("cmd");

  /** The time rolling parameter. */
  private Input timeRollingParameter = Input.getStringInput("timerollingparameter");

  /** The time rolling format. */
  private Input timeRollingFormat = Input.getStringInput("timerollingformat");

  /** The time rolling offset. */
  private Input timeRollingOffset = Input.getIntegerInput("timerollingoffset");

  /** The time rolling enabled. */
  private Input timeRollingEnabled = Input.getBooleanInput("timerollingenabled");

  /**
   * Gets the time rolling enabled.
   *
   * @return the time rolling enabled
   */
  public Input getTimeRollingEnabled() {
    return timeRollingEnabled;
  }

  /**
   * Sets the time rolling enabled.
   *
   * @param timeRollingEnabled the new time rolling enabled
   */
  public void setTimeRollingEnabled(Input timeRollingEnabled) {
    this.timeRollingEnabled = timeRollingEnabled;
  }

  /**
   * Gets the groupname.
   *
   * @return the groupname
   */
  public Input getGroupname() {
    return groupname;
  }

  /**
   * Sets the groupname.
   *
   * @param groupname the new groupname
   */
  public void setGroupname(Input groupname) {
    this.groupname = groupname;
  }

  /**
   * Gets the description.
   *
   * @return the description
   */
  public Input getDescription() {
    return description;
  }

  /**
   * Sets the description.
   *
   * @param description the new description
   */
  public void setDescription(Input description) {
    this.description = description;
  }

  /**
   * Gets the parentgroup.
   *
   * @return the parentgroup
   */
  public Input getParentgroup() {
    return parentgroup;
  }

  /**
   * Sets the parentgroup.
   *
   * @param parentgroup the new parentgroup
   */
  public void setParentgroup(Input parentgroup) {
    this.parentgroup = parentgroup;
  }

  public void setCmd(Input cmd) {
    this.cmd = cmd;
  }

  public Input getCmd() {
    return cmd;
  }

  @Override
  public void bindForm(Map<String, Object> root) {}

  @Override
  public boolean validateForm() {
    return false;
  }
}
