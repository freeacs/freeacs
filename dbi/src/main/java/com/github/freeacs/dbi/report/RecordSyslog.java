package com.github.freeacs.dbi.report;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
public class RecordSyslog extends Record<RecordSyslog> {
  private static final KeyFactory keyFactory =
      new KeyFactory("Unittype", "Profile", "Severity", "EventId", "Facility");
  private Key key;

  private Date tms;
  private PeriodType periodType;
  private String unittypeName;
  private String profileName;
  private String severity;
  private String eventId;
  private String facility;

  private Counter messageCount = new Counter();

  protected RecordSyslog() {}

  public RecordSyslog(
      Date tms,
      PeriodType periodType,
      String unittypeName,
      String profileName,
      String severity,
      String eventId,
      String facility) {
    this.tms = tms;
    this.periodType = periodType;
    this.unittypeName = unittypeName;
    this.profileName = profileName;
    this.severity = severity;
    this.eventId = eventId;
    this.facility = facility;
    this.key =
        keyFactory.makeKey(tms, periodType, unittypeName, profileName, severity, eventId, facility);
  }

  @Override
  public void add(RecordSyslog record) {
    getMessageCount().add(record.getMessageCount());
  }

  @Override
  public RecordSyslog clone() {
    RecordSyslog clone =
        new RecordSyslog(tms, periodType, unittypeName, profileName, severity, eventId, facility);
    clone.setMessageCount(getMessageCount().clone());
    return clone;
  }

  public KeyFactory getKeyFactory() {
    return keyFactory;
  }
}
