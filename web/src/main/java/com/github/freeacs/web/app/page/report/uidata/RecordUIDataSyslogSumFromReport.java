package com.github.freeacs.web.app.page.report.uidata;

import com.github.freeacs.dbi.SyslogConstants;
import com.github.freeacs.dbi.Unit;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * This wrapper class will contain a list of wrapped syslog entries
 *
 * <p>It also maintains a set of integers, for counting errors and warnings, and total entries.
 *
 * <p>Of special interest is the addRecord method, that is responsible for updating the numbers
 * according to the severity level of the added record.
 *
 * @author Jarl Andre Hubenthal
 */
@Getter
public class RecordUIDataSyslogSumFromReport
    implements Comparable<RecordUIDataSyslogSumFromReport> {

  /** The warnings. */
  private Long warnings;

  /** The errors. */
  private Long errors;

  /** The total. */
  private Long total;

  /** The unit. */
  private final Unit unit;

  /**
   * Instantiates a new record ui data syslog sum from report.
   *
   * @param unit the unit
   */
  public RecordUIDataSyslogSumFromReport(Unit unit) {
    this.unit = unit;
  }

  /** The records. */
  private final List<RecordUIDataSyslogFromReport> records = new ArrayList<>();

  /**
   * This method adds a record to an internal list, and updates three integers,
   * warnings/errors/total.
   *
   * @param record a wrapper record
   */
  public void addRecord(RecordUIDataSyslogFromReport record) {
    Integer severity = SyslogConstants.getSeverityInt(record.getEntry().getSeverity());
    if (severity == SyslogConstants.SEVERITY_WARNING) {
      warnings += record.getEntry().getMessageCount().get();
    }
    if (severity <= SyslogConstants.SEVERITY_ERROR) {
      errors += record.getEntry().getMessageCount().get();
    }
    total += record.getEntry().getMessageCount().get();
    this.records.add(record);
  }

  @Override
  public int compareTo(RecordUIDataSyslogSumFromReport o) {
    return o.getTotal().compareTo(getTotal());
  }

}
