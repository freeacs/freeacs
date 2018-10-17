package com.github.freeacs.web.app.page.report;

import com.github.freeacs.web.app.input.Input;
import com.github.freeacs.web.app.input.InputData;
import com.github.freeacs.web.app.util.DateUtils;
import java.util.Map;

/** The Class UnitListData. */
public class UnitListData extends InputData {
  /** The type. */
  private Input type = Input.getStringInput("type");

  private Input swVersion = Input.getStringInput("swVersion");

  /** The start. */
  private Input start = Input.getDateInput("start", DateUtils.Format.DEFAULT);

  /** The end. */
  private Input end = Input.getDateInput("end", DateUtils.Format.DEFAULT);

  private Input msgCountLow = Input.getIntegerInput("filter_msg_count_low");

  private Input msgCountHigh = Input.getIntegerInput("filter_msg_count_high");

  /**
   * The group selected in advanced view - not be confused with the group selected in the group
   * report.
   */
  private Input groupSelect = Input.getStringInput("groupselect");

  /** Syslog Unit List. */
  private Input filterSeverity = Input.getStringInput("severity");

  private Input filterFacility = Input.getStringInput("facility");
  private Input filterEventId = Input.getStringInput("eventid");
  private Input filterLimit = Input.getIntegerInput("filter_rows_count");

  /** Hardware Unit List. */
  private Input filterDdrHigh = Input.getIntegerInput("filter_ddr_high");

  private Input filterDdrLow = Input.getIntegerInput("filter_ddr_low");
  private Input filterOcmHigh = Input.getIntegerInput("filter_ocm_high");
  private Input filterOcmLow = Input.getIntegerInput("filter_ocm_low");
  private Input filterUptimeHigh = Input.getIntegerInput("filter_uptime_high");
  private Input filterUptimeLow = Input.getIntegerInput("filter_uptime_low");
  private Input filterOperand = Input.getStringInput("filter_operand");

  /** Voip Unit list. */
  private Input filterTotalScoreHigh = Input.getDoubleInput("filter_totalscore_high");

  private Input filterTotalScoreLow = Input.getDoubleInput("filter_totalscore_low");

  @Override
  public void bindForm(Map<String, Object> root) {
    // TODO not implemented
  }

  @Override
  public boolean validateForm() {
    // TODO not implemented
    return false;
  }

  public Input getEnd() {
    return end;
  }

  public Input getGroupSelect() {
    return groupSelect;
  }

  public Input getMsgCountHigh() {
    return msgCountHigh;
  }

  public Input getMsgCountLow() {
    return msgCountLow;
  }

  public Input getStart() {
    return start;
  }

  public Input getSwVersion() {
    return swVersion;
  }

  public Input getType() {
    return type;
  }

  public void setEnd(Input end) {
    this.end = end;
  }

  public void setGroupSelect(Input groupSelect) {
    this.groupSelect = groupSelect;
  }

  public void setMsgCountHigh(Input msgCountHigh) {
    this.msgCountHigh = msgCountHigh;
  }

  public void setMsgCountLow(Input msgCountLow) {
    this.msgCountLow = msgCountLow;
  }

  public void setStart(Input start) {
    this.start = start;
  }

  public void setSwVersion(Input swVersion) {
    this.swVersion = swVersion;
  }

  public void setType(Input type) {
    this.type = type;
  }

  public Input getFilterSeverity() {
    return filterSeverity;
  }

  public void setFilterSeverity(Input filterSeverity) {
    this.filterSeverity = filterSeverity;
  }

  public Input getFilterFacility() {
    return filterFacility;
  }

  public void setFilterFacility(Input filterFacility) {
    this.filterFacility = filterFacility;
  }

  public Input getFilterEventId() {
    return filterEventId;
  }

  public void setFilterEventId(Input filterEventId) {
    this.filterEventId = filterEventId;
  }

  public Input getFilterLimit() {
    return filterLimit;
  }

  public void setFilterLimit(Input filterLimit) {
    this.filterLimit = filterLimit;
  }

  public Input getFilterDdrHigh() {
    return filterDdrHigh;
  }

  public void setFilterDdrHigh(Input filterDdrHigh) {
    this.filterDdrHigh = filterDdrHigh;
  }

  public Input getFilterDdrLow() {
    return filterDdrLow;
  }

  public void setFilterDdrLow(Input filterDdrLow) {
    this.filterDdrLow = filterDdrLow;
  }

  public Input getFilterOcmHigh() {
    return filterOcmHigh;
  }

  public void setFilterOcmHigh(Input filterOcmHigh) {
    this.filterOcmHigh = filterOcmHigh;
  }

  public Input getFilterOcmLow() {
    return filterOcmLow;
  }

  public void setFilterOcmLow(Input filterOcmLow) {
    this.filterOcmLow = filterOcmLow;
  }

  public Input getFilterOperand() {
    return filterOperand;
  }

  public void setFilterOperand(Input filterOperand) {
    this.filterOperand = filterOperand;
  }

  public Input getFilterUptimeHigh() {
    return filterUptimeHigh;
  }

  public void setFilterUptimeHigh(Input filterUptimeHigh) {
    this.filterUptimeHigh = filterUptimeHigh;
  }

  public Input getFilterUptimeLow() {
    return filterUptimeLow;
  }

  public void setFilterUptimeLow(Input filterUptimeLow) {
    this.filterUptimeLow = filterUptimeLow;
  }

  public Input getFilterTotalScoreHigh() {
    return filterTotalScoreHigh;
  }

  public void setFilterTotalScoreHigh(Input filterTotalScoreHigh) {
    this.filterTotalScoreHigh = filterTotalScoreHigh;
  }

  public Input getFilterTotalScoreLow() {
    return filterTotalScoreLow;
  }

  public void setFilterTotalScoreLow(Input filterTotalScoreLow) {
    this.filterTotalScoreLow = filterTotalScoreLow;
  }
}
