package com.github.freeacs.web.app.page.unit;

import com.github.freeacs.web.app.input.Input;
import com.github.freeacs.web.app.input.InputData;
import com.github.freeacs.web.app.util.DateUtils;
import java.util.Map;

/** The Class UnitStatusData. */
public class UnitStatusData extends InputData {
  /** The graph type. */
  private Input graphType = Input.getStringInput("type");

  private Input syslogFilter = Input.getStringInput("syslogFilter");

  public Input getSyslogFilter() {
    return syslogFilter;
  }

  public void setSyslogFilter(Input syslogFilter) {
    this.syslogFilter = syslogFilter;
  }

  /**
   * Sets the graph type.
   *
   * @param graphType the new graph type
   */
  public void setGraphType(Input graphType) {
    this.graphType = graphType;
  }

  /**
   * Gets the graph type.
   *
   * @return the graph type
   */
  public Input getGraphType() {
    return graphType;
  }

  /**
   * Sets the graph method.
   *
   * @param graphMethod the new graph method
   */
  public void setGraphMethod(Input graphMethod) {
    this.graphMethod = graphMethod;
  }

  /**
   * Gets the graph method.
   *
   * @return the graph method
   */
  public Input getGraphMethod() {
    return graphMethod;
  }

  /** The graph method. */
  private Input graphMethod = Input.getStringInput("method");

  /** The aggregate. */
  private Input aggregate = Input.getStringArrayInput("aggregate");

  /** The start. */
  private Input start = Input.getDateInput("start", DateUtils.Format.DEFAULT);

  /**
   * Sets the start.
   *
   * @param start the new start
   */
  public void setStart(Input start) {
    this.start = start;
  }

  /**
   * Gets the start.
   *
   * @return the start
   */
  public Input getStart() {
    return start;
  }

  /** The method. */
  private Input method = Input.getStringInput("method");

  /**
   * Sets the method.
   *
   * @param method the new method
   */
  public void setMethod(Input method) {
    this.method = method;
  }

  /**
   * Gets the method.
   *
   * @return the method
   */
  public Input getMethod() {
    return method;
  }

  /** The end. */
  private Input end = Input.getDateInput("end", DateUtils.Format.DEFAULT);

  /**
   * Sets the end.
   *
   * @param end the new end
   */
  public void setEnd(Input end) {
    this.end = end;
  }

  /**
   * Gets the end.
   *
   * @return the end
   */
  public Input getEnd() {
    return end;
  }

  /** The period. */
  private Input period = Input.getStringInput("period");

  /**
   * Sets the period.
   *
   * @param period the new period
   */
  public void setPeriod(Input period) {
    this.period = period;
  }

  /**
   * Gets the period.
   *
   * @return the period
   */
  public Input getPeriod() {
    return period;
  }

  @Override
  public void bindForm(Map<String, Object> root) {}

  @Override
  public boolean validateForm() {
    return false;
  }

  /**
   * Gets the aggregate.
   *
   * @return the aggregate
   */
  public Input getAggregate() {
    return aggregate;
  }

  /**
   * Sets the aggregate.
   *
   * @param aggregate the new aggregate
   */
  public void setAggregate(Input aggregate) {
    this.aggregate = aggregate;
  }
}
