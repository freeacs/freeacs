package com.github.freeacs.web.app.page.report;

import com.github.freeacs.web.app.input.Input;
import com.github.freeacs.web.app.input.InputData;
import com.github.freeacs.web.app.util.DateUtils;
import java.util.Map;

/** The Class ReportData. */
public class ReportData extends InputData {
  /** The aggregate. */
  private Input aggregate = Input.getStringArrayInput("aggregate");

  /**
   * Sets the aggregate.
   *
   * @param aggregate the new aggregate
   */
  public void setAggregate(Input aggregate) {
    this.aggregate = aggregate;
  }

  /**
   * Gets the aggregate.
   *
   * @return the aggregate
   */
  public Input getAggregate() {
    return aggregate;
  }

  /** The type. */
  private Input type = Input.getStringInput("type");

  /**
   * Sets the type.
   *
   * @param type the new type
   */
  public void setType(Input type) {
    this.type = type;
  }

  /**
   * Gets the type.
   *
   * @return the type
   */
  public Input getType() {
    return type;
  }

  /** The realtime. */
  private Input realtime = Input.getBooleanInput("realtime");

  /**
   * Sets the realtime.
   *
   * @param realtime the new realtime
   */
  public void setRealtime(Input realtime) {
    this.realtime = realtime;
  }

  /**
   * Gets the realtime.
   *
   * @return the realtime
   */
  public Input getRealtime() {
    return realtime;
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

  /** The optional method. */
  private Input optionalMethod = Input.getStringInput("optionalmethod");

  /**
   * Sets the optional method.
   *
   * @param optionalMethod the new optional method
   */
  public void setOptionalMethod(Input optionalMethod) {
    this.optionalMethod = optionalMethod;
  }

  /**
   * Gets the optional method.
   *
   * @return the optional method
   */
  public Input getOptionalMethod() {
    return optionalMethod;
  }

  /** The advanced view. */
  private Input advancedView = Input.getBooleanInput("advancedView");

  /**
   * Sets the advanced view.
   *
   * @param advancedView the new advanced view
   */
  public void setAdvancedView(Input advancedView) {
    this.advancedView = advancedView;
  }

  /**
   * Gets the advanced view.
   *
   * @return the advanced view
   */
  public Input getAdvancedView() {
    return advancedView;
  }

  /** The legend index. */
  private Input legendIndex = Input.getIntegerInput("legendIndex");

  /**
   * Sets the legend index.
   *
   * @param legendIndex the new legend index
   */
  public void setLegendIndex(Input legendIndex) {
    this.legendIndex = legendIndex;
  }

  /**
   * Gets the legend index.
   *
   * @return the legend index
   */
  public Input getLegendIndex() {
    return legendIndex;
  }

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

  /**
   * The group selected in advanced view - not be confused with the group selected in the group
   * report.
   */
  private Input groupSelect = Input.getStringInput("groupselect");

  public void setGroupSelect(Input groupSelect) {
    this.groupSelect = groupSelect;
  }

  public Input getGroupSelect() {
    return groupSelect;
  }

  /** The software version in advanced view. */
  private Input swVersion = Input.getStringInput("swversion");

  public void setSwVersion(Input swVersion) {
    this.swVersion = swVersion;
  }

  public Input getSwVersion() {
    return swVersion;
  }

  public void setAsync(Input async) {
    this.async = async;
  }

  public Input getAsync() {
    return async;
  }

  /** The async. */
  private Input async = Input.getStringInput("async");

  @Override
  public void bindForm(Map<String, Object> root) {}

  @Override
  public boolean validateForm() {
    return false;
  }
}
