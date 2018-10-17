package com.github.freeacs.web.app.page.report.uidata;

import com.github.freeacs.dbi.SyslogConstants;
import com.github.freeacs.dbi.Unit;
import com.github.freeacs.web.app.page.AbstractWebPage;
import freemarker.template.TemplateModelException;
import java.util.ArrayList;
import java.util.Arrays;
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
public class RecordUIDataSyslogSumFromReport
    implements Comparable<RecordUIDataSyslogSumFromReport> {
  /**
   * Gets the warnings.
   *
   * @return the warnings
   */
  public int getWarnings() {
    return warnings;
  }

  /**
   * Gets the errors.
   *
   * @return the errors
   */
  public int getErrors() {
    return errors;
  }

  /**
   * Gets the total.
   *
   * @return the total
   */
  public int getTotal() {
    return total;
  }

  /** The warnings. */
  private int warnings;

  /** The errors. */
  private int errors;

  /** The total. */
  private int total;

  /** The unit. */
  private Unit unit;

  /**
   * Instantiates a new record ui data syslog sum from report.
   *
   * @param unit the unit
   */
  public RecordUIDataSyslogSumFromReport(Unit unit) {
    this.unit = unit;
  }

  /**
   * Gets the unit.
   *
   * @return the unit
   */
  public Unit getUnit() {
    return unit;
  }

  /** The records. */
  private List<RecordUIDataSyslogFromReport> records = new ArrayList<>();

  /** The row background style. */
  private String rowBackgroundStyle;

  /**
   * Gets the records.
   *
   * @return the records
   */
  public List<RecordUIDataSyslogFromReport> getRecords() {
    return records;
  }

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

  /**
   * Gets the row background style.
   *
   * @return the row background style
   */
  public String getRowBackgroundStyle() {
    try {
      int score = 100;
      if (errors > 0) {
        score = score - 40;
      }
      if (warnings > 0) {
        score = score - 30;
      }
      rowBackgroundStyle =
          new AbstractWebPage.RowBackgroundColorMethod().exec(Arrays.asList(String.valueOf(score)));
    } catch (TemplateModelException e) {
      rowBackgroundStyle = "";
    }
    return rowBackgroundStyle;
  }

  @Override
  public int compareTo(RecordUIDataSyslogSumFromReport o) {
    return Integer.valueOf(o.getTotal()).compareTo(Integer.valueOf(getTotal()));
  }
}
