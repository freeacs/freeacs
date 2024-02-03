package com.github.freeacs.web.app.page.unit;

import com.github.freeacs.web.app.input.Input;
import com.github.freeacs.web.app.input.InputData;
import com.github.freeacs.web.app.util.DateUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/** The Class UnitStatusData. */
@Setter
@Getter
public class UnitStatusData extends InputData {
  /** The graph type. */
  private Input graphType = Input.getStringInput("type");

  private Input syslogFilter = Input.getStringInput("syslogFilter");

  /** The graph method. */
  private Input graphMethod = Input.getStringInput("method");

  /** The aggregate. */
  private Input aggregate = Input.getStringArrayInput("aggregate");

  /** The start. */
  private Input start = Input.getDateInput("start", DateUtils.Format.DEFAULT);

  /** The method. */
  private Input method = Input.getStringInput("method");

  /** The end. */
  private Input end = Input.getDateInput("end", DateUtils.Format.DEFAULT);

  /** The period. */
  private Input period = Input.getStringInput("period");

  @Override
  public void bindForm(Map<String, Object> root) {}

  @Override
  public boolean validateForm() {
    return false;
  }

}
