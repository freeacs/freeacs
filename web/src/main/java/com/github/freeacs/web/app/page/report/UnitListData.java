package com.github.freeacs.web.app.page.report;

import com.github.freeacs.web.app.input.Input;
import com.github.freeacs.web.app.input.InputData;
import com.github.freeacs.web.app.util.DateUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/** The Class UnitListData. */
@Getter
@Setter
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
}
