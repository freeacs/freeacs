package com.github.freeacs.dbi.report;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
public class RecordGroup extends Record<RecordGroup> {
  private static final KeyFactory keyFactory = new KeyFactory("Unittype", "Group");
  private Key key;

  private Date tms;
  private PeriodType periodType;
  private String unittypeName;
  private String groupName;
  private Counter unitCount = new Counter();

  public RecordGroup(Date tms, PeriodType periodType, String unittypeName, String groupName) {
    this.tms = tms;
    this.periodType = periodType;
    this.unittypeName = unittypeName;
    this.groupName = groupName;
    this.key = keyFactory.makeKey(tms, periodType, unittypeName, groupName);
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
