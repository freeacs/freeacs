package com.github.freeacs.web.app.page.job;

import com.github.freeacs.web.app.input.Input;
import com.github.freeacs.web.app.input.InputData;
import java.util.Map;

/**
 * Job Overview input definition.
 *
 * @author Jarl Andre Hubenthal
 */
public class JobsData extends InputData {
  /** The status. */
  private Input status = Input.getStringInput("status");

  /** The firmware. */
  private Input firmware = Input.getStringInput("firmware");

  /** The job name. */
  private Input jobName = Input.getStringInput("jobname");

  /** The filter. */
  private Input filter = Input.getStringInput("filterstring");

  /**
   * Sets the status.
   *
   * @param status the new status
   */
  public void setStatus(Input status) {
    this.status = status;
  }

  /**
   * Gets the status.
   *
   * @return the status
   */
  public Input getStatus() {
    return status;
  }

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
   * Sets the job name.
   *
   * @param jobName the new job name
   */
  public void setJobName(Input jobName) {
    this.jobName = jobName;
  }

  /**
   * Gets the job name.
   *
   * @return the job name
   */
  public Input getJobName() {
    return jobName;
  }

  /**
   * Sets the filter.
   *
   * @param filter the new filter
   */
  public void setFilter(Input filter) {
    this.filter = filter;
  }

  /**
   * Gets the filter.
   *
   * @return the filter
   */
  public Input getFilter() {
    return filter;
  }

  @Override
  public void bindForm(Map<String, Object> root) {}

  @Override
  public boolean validateForm() {
    return false;
  }
}
