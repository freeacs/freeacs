package com.github.freeacs.web.app.page.report.uidata;

import com.github.freeacs.dbi.Unit;
import com.github.freeacs.dbi.report.RecordSyslog;
import com.github.freeacs.web.app.page.syslog.SyslogUtil;
import freemarker.template.TemplateModelException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * This wrapper class contains helper methods, and also a static converter method <code>
 * convertRecords(Unit unit,Collection<RecordSyslog> records)</code> for a larger set of
 * RecordSyslog objects.
 *
 * @author Jarl Andre Hubenthal
 */
public class RecordUIDataSyslogFromReport {
  /** The entry. */
  private RecordSyslog entry;

  /**
   * Gets the entry.
   *
   * @return the entry
   */
  public RecordSyslog getEntry() {
    return entry;
  }

  /** The unit. */
  private Unit unit;

  /**
   * Gets the unit.
   *
   * @return the unit
   */
  public Unit getUnit() {
    return unit;
  }

  /**
   * Gets the message count.
   *
   * @return the message count
   */
  public Long getMessageCount() {
    return entry.getMessageCount().get();
  }

  /** Instantiates a new record ui data syslog from report. */
  RecordUIDataSyslogFromReport() {}

  /**
   * Gets the severity.
   *
   * @return the severity
   * @throws TemplateModelException the template model exception
   */
  public String getSeverity() throws TemplateModelException {
    return new SyslogUtil.GetSeverityText().exec(Arrays.asList(entry.getSeverity()));
  }

  /**
   * Gets the facility.
   *
   * @return the facility
   * @throws TemplateModelException the template model exception
   */
  public String getFacility() throws TemplateModelException {
    return new SyslogUtil.GetFacilityText().exec(Arrays.asList(entry.getFacility()));
  }

  /**
   * Instantiates a new record ui data syslog from report.
   *
   * @param unit the unit
   * @param record the record
   */
  public RecordUIDataSyslogFromReport(Unit unit, RecordSyslog record) {
    this.entry = record;
    this.unit = unit;
  }

  /**
   * Convert records.
   *
   * @param unit the unit
   * @param records the records
   * @return the list
   */
  public static List<RecordUIDataSyslogFromReport> convertRecords(
      Unit unit, Collection<RecordSyslog> records) {
    List<RecordUIDataSyslogFromReport> list = new ArrayList<>();
    for (RecordSyslog record : records) {
      list.add(new RecordUIDataSyslogFromReport(unit, record));
    }
    return list;
  }

  /**
   * Gets the tms as string.
   *
   * @return the tms as string
   */
  public String getTmsAsString() {
    return RecordUIDataConstants.DATE_FORMAT.format(entry.getTms()).replace(" ", "&nbsp;");
  }

  /**
   * Gets the row background style.
   *
   * @return the row background style
   */
  public String getRowBackgroundStyle() {
    return "background-color:#"
        + SyslogUtil.getBackgroundColor(Integer.parseInt(entry.getSeverity()))
        + ";";
  }
}
