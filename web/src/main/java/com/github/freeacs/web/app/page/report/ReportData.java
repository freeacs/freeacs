package com.github.freeacs.web.app.page.report;

import com.github.freeacs.web.app.input.Input;
import com.github.freeacs.web.app.input.InputData;
import com.github.freeacs.web.app.util.DateUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/** The Class ReportData. */
@Setter
@Getter
public class ReportData extends InputData {
  /** The aggregate. */
  private Input aggregate = Input.getStringArrayInput("aggregate");

  /** The type. */
  private Input type = Input.getStringInput("type");

  /** The realtime. */
  private Input realtime = Input.getBooleanInput("realtime");

  /** The method. */
  private Input method = Input.getStringInput("method");

  /** The optional method. */
  private Input optionalMethod = Input.getStringInput("optionalmethod");

  /** The advanced view. */
  private Input advancedView = Input.getBooleanInput("advancedView");

  /** The legend index. */
  private Input legendIndex = Input.getIntegerInput("legendIndex");

  /** The start. */
  private Input start = Input.getDateInput("start", DateUtils.Format.DEFAULT);

  /** The end. */
  private Input end = Input.getDateInput("end", DateUtils.Format.DEFAULT);

  /** The period. */
  private Input period = Input.getStringInput("period");

  /**
   * The group selected in advanced view - not be confused with the group selected in the group
   * report.
   */
  private Input groupSelect = Input.getStringInput("groupselect");

  /** The software version in advanced view. */
  private Input swVersion = Input.getStringInput("swversion");

  /** The async. */
  private Input async = Input.getStringInput("async");

  @Override
  public void bindForm(Map<String, Object> root) {}

  @Override
  public boolean validateForm() {
    return false;
  }
}
