package com.github.freeacs.dbi.report;

import java.util.Date;

public class RecordGroup extends Record<RecordGroup> {
  private static KeyFactory keyFactory = new KeyFactory("Unittype", "Group");
  private Key key;

  private Date tms;
  private PeriodType periodType;
  private String unittypeName;
  private String groupName;
  private Counter unitCount = new Counter();

  protected RecordGroup() {}

  public RecordGroup(Date tms, PeriodType periodType, String unittypeName, String groupName) {
    this.tms = tms;
    this.periodType = periodType;
    this.unittypeName = unittypeName;
    this.groupName = groupName;
    this.key = keyFactory.makeKey(tms, periodType, unittypeName, groupName);
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

  public String getGroupName() {
    return groupName;
  }

  public Counter getUnitCount() {
    return unitCount;
  }

  public void setUnitCount(Counter unitCount) {
    this.unitCount = unitCount;
  }

  @Override
  public void add(RecordGroup record) {
    getUnitCount().add(record.getUnitCount());
  }

  @Override
  public RecordGroup clone() {
    RecordGroup clone = new RecordGroup(tms, periodType, unittypeName, groupName);
    clone.setUnitCount(getUnitCount().clone());
    return clone;
  }

  public KeyFactory getKeyFactory() {
    return keyFactory;
  }
}
