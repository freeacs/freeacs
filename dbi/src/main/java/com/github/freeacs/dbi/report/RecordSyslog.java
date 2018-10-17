package com.github.freeacs.dbi.report;

import java.util.Date;

public class RecordSyslog extends Record<RecordSyslog> {
  private static KeyFactory keyFactory =
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

  public Counter getMessageCount() {
    return messageCount;
  }

  public void setMessageCount(Counter messageCount) {
    this.messageCount = messageCount;
  }

  public Key getKey() {
    return key;
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

  @Override
  public PeriodType getPeriodType() {
    return periodType;
  }

  @Override
  public Date getTms() {
    return tms;
  }

  public KeyFactory getKeyFactory() {
    return keyFactory;
  }

  public String getUnittypeName() {
    return unittypeName;
  }

  public String getProfileName() {
    return profileName;
  }

  public String getSeverity() {
    return severity;
  }

  public String getEventId() {
    return eventId;
  }

  public String getFacility() {
    return facility;
  }
}
