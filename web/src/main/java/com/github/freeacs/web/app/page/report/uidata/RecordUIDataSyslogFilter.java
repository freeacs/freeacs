/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.freeacs.web.app.page.report.uidata;

import com.github.freeacs.dbi.SyslogConstants;
import com.github.freeacs.web.app.input.InputSelectionFactory;
import com.github.freeacs.web.app.page.report.UnitListData;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class acts a wrapper for all the filter logic used on the syslog unit list page.
 *
 * <p>It makes sure to populate the filters to the template map.
 *
 * @author Jarl Andre Hubenthal
 */
public class RecordUIDataSyslogFilter {
  public final Integer msg_count_low;
  public static final Integer msg_count_low_default = 1;

  public final Integer msg_count_high;
  public static final Integer msg_count_high_default = null;

  public final Integer max_rows;
  public static final Integer max_rows_default = null;

  public final String severity;
  public static final String severity_default = null;
  private static final List<String> severityList =
      new ArrayList<>(SyslogConstants.severityMap.values());

  public final String facility;
  public static final String facility_default = null;
  private static final List<String> facilityList =
      new ArrayList<>(SyslogConstants.facilityMap.values());

  public final String eventid;
  public static final String eventid_default = null;
  private static final List<String> eventIdList =
      new ArrayList<>(SyslogConstants.eventMap.values());

  public RecordUIDataSyslogFilter(UnitListData inputData, Map<String, Object> root) {
    msg_count_low = inputData.getMsgCountLow().getInteger(msg_count_low_default);
    msg_count_high = inputData.getMsgCountHigh().getInteger(msg_count_high_default);
    severity = inputData.getFilterSeverity().getString(severity_default);
    facility = inputData.getFilterFacility().getString(facility_default);
    eventid = inputData.getFilterEventId().getString(eventid_default);
    max_rows = inputData.getFilterLimit().getInteger(max_rows_default);
    root.put(inputData.getMsgCountLow().getKey(), msg_count_low);
    root.put(inputData.getMsgCountHigh().getKey(), msg_count_high);
    root.put(inputData.getFilterLimit().getKey(), max_rows);
    root.put(
        inputData.getFilterEventId().getKey(),
        InputSelectionFactory.getDropDownSingleSelect(inputData.getFilterEventId(), eventIdList));
    root.put(
        inputData.getFilterFacility().getKey(),
        InputSelectionFactory.getDropDownSingleSelect(inputData.getFilterFacility(), facilityList));
    root.put(
        inputData.getFilterSeverity().getKey(),
        InputSelectionFactory.getDropDownSingleSelect(inputData.getFilterSeverity(), severityList));
  }

  public boolean isRecordSumRelevant(RecordUIDataSyslogSumFromReport record) {
    return isMessageCountRelevant(record);
  }

  private boolean isMessageCountRelevant(RecordUIDataSyslogSumFromReport record) {
    if (msg_count_high != null) {
      return record.getTotal() > msg_count_low && record.getTotal() < msg_count_high;
    }
    return record.getTotal() > msg_count_low;
  }
}
