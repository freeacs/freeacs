package com.github.freeacs.web.app.page.report.uidata;

import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.SyslogEntry;
import com.github.freeacs.dbi.SyslogEvent;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.web.app.page.syslog.SyslogUtil;
import freemarker.template.TemplateModelException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This wrapper class contains helper methods, and also a static converter method <code>
 * convertRecords(List<SyslogEntry> records,XAPS xaps)</code> for a larger set of SyslogEntry
 * objects.
 *
 * @author Jarl Andre Hubenthal
 */
public class RecordUIDataSyslog {
  private final ACS acs;

  /** The entry. */
  private SyslogEntry entry;

  /**
   * Gets the severity.
   *
   * @return the severity
   * @throws TemplateModelException the template model exception
   */
  public String getSeverity() throws TemplateModelException {
    return new SyslogUtil.GetSeverityText()
        .exec(Collections.singletonList(entry.getSeverity().toString()));
  }

  /**
   * Gets the facility.
   *
   * @return the facility
   * @throws TemplateModelException the template model exception
   */
  public String getFacility() throws TemplateModelException {
    return new SyslogUtil.GetFacilityText()
        .exec(Collections.singletonList(entry.getFacility().toString()));
  }

  /**
   * Gets the ip address.
   *
   * @return the ip address
   */
  @SuppressWarnings("unused")
  public String getIpAddress() {
    return entry.getIpAddress();
  }

  /**
   * Instantiates a new record ui data syslog.
   *
   * @param record the record
   */
  public RecordUIDataSyslog(SyslogEntry record, ACS acs) {
    this.entry = record;
    this.acs = acs;
  }

  /**
   * Convert records.
   *
   * @param records the records
   * @return the list
   */
  public static List<RecordUIDataSyslog> convertRecords(List<SyslogEntry> records, ACS acs) {
    List<RecordUIDataSyslog> list = new ArrayList<>();
    for (SyslogEntry record : records) {
      list.add(new RecordUIDataSyslog(record, acs));
    }
    return list;
  }

  /**
   * Gets the message excerpt.
   *
   * @return the message excerpt
   */
  @SuppressWarnings("unused")
  public String getMessageExcerpt() {
    if (entry.getContent() == null || entry.getContent().length() > 90) {
      return entry.getContent().substring(0, 90);
    }
    return entry.getContent();
  }

  /**
   * Gets the message.
   *
   * @return the message
   */
  public String getMessage() {
    return entry.getContent();
  }

  /**
   * Gets the event id as string.
   *
   * @return the event id as string
   */
  @SuppressWarnings("unused")
  public String getEventIdAsString() {
    Unittype unittype = acs.getUnittype(entry.getUnittypeName());
    if (unittype != null) {
      @SuppressWarnings("static-access")
      SyslogEvent event = unittype.getSyslogEvents().getById(entry.getEventId());
      if (event != null) {
        return entry.getEventId() + "(" + event.getName() + ")";
      }
    }
    return String.valueOf(entry.getEventId());
  }

  /**
   * Gets the tms as string.
   *
   * @return the tms as string
   */
  @SuppressWarnings("unused")
  public String getTmsAsString() {
    return RecordUIDataConstants.DATE_FORMAT
        .format(entry.getCollectorTimestamp())
        .replace(" ", "&nbsp;");
  }

  /**
   * Gets the row background style.
   *
   * @return the row background style
   */
  @SuppressWarnings("unused")
  public String getRowBackgroundStyle() {
    return "background-color:#" + SyslogUtil.getBackgroundColor(entry.getSeverity()) + ";";
  }
}
