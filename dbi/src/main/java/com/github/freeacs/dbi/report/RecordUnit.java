package com.github.freeacs.dbi.report;

import java.util.Date;

public class RecordUnit extends Record<RecordUnit> {
  private static KeyFactory keyFactory =
      new KeyFactory("Unittype", "Profile", "SoftwareVersion", "Status");
  private Key key;

  private Date tms;
  private PeriodType periodType;
  private String unittypeName;
  private String profileName;
  private String softwareVersion;
  private String status;

  private Counter unitCount = new Counter();

  protected RecordUnit() {}

  public RecordUnit(
      Date tms,
      PeriodType periodType,
      String unittypeName,
      String profileName,
      String softwareVersion,
      String status) {
    this.tms = tms;
    this.periodType = periodType;
    this.unittypeName = unittypeName;
    this.profileName = profileName;
    this.softwareVersion = softwareVersion;
    this.status = status;
    this.key =
        keyFactory.makeKey(tms, periodType, unittypeName, profileName, softwareVersion, status);
  }

  public Key getKey() {
    return key;
  }

  public Date getTms() {
    return tms;
  }

  public PeriodType getPeriodType() {
    return periodType;
  }

  public String getUnittypeName() {
    return unittypeName;
  }

  public String getProfileName() {
    return profileName;
  }

  public String getSoftwareVersion() {
    return softwareVersion;
  }

  public Counter getUnitCount() {
    return unitCount;
  }

  public void setUnitCount(Counter unitCount) {
    this.unitCount = unitCount;
  }

  @Override
  public void add(RecordUnit record) {
    getUnitCount().add(record.getUnitCount());
  }

  @Override
  public RecordUnit clone() {
    RecordUnit clone =
        new RecordUnit(tms, periodType, unittypeName, profileName, softwareVersion, status);
    clone.setUnitCount(getUnitCount().clone());
    return clone;
  }

  public KeyFactory getKeyFactory() {
    return keyFactory;
  }

  public String getStatus() {
    return status;
  }
}
