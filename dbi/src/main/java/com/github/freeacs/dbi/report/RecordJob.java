package com.github.freeacs.dbi.report;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
public class RecordJob extends Record<RecordJob> {
  private static final KeyFactory keyFactory = new KeyFactory("Unittype", "Job", "Group");
  private Key key;

  private Date tms;
  private PeriodType periodType;
  private String unittypeName;
  private String jobName;
  private String groupName;
  private Counter groupSize = new Counter();
  private Counter completed = new Counter();
  private Counter confirmedFailed = new Counter();
  private Counter unconfirmedFailed = new Counter();

  protected RecordJob() {}

  public RecordJob(
      Date tms, PeriodType periodType, String unittypeName, String jobName, String groupName) {
    this.tms = tms;
    this.periodType = periodType;
    this.unittypeName = unittypeName;
    this.jobName = jobName;
    this.groupName = groupName;
    this.key = keyFactory.makeKey(tms, periodType, unittypeName, jobName, groupName);
  }

  @Override
  public void add(RecordJob record) {
    getCompleted().add(record.getCompleted());
    getConfirmedFailed().add(record.getConfirmedFailed());
    getGroupSize().add(record.getGroupSize());
    getUnconfirmedFailed().add(record.getUnconfirmedFailed());
  }

  @Override
  public RecordJob clone() {
    RecordJob clone = new RecordJob(tms, periodType, unittypeName, jobName, groupName);
    clone.setCompleted(getCompleted().clone());
    clone.setConfirmedFailed(getConfirmedFailed().clone());
    clone.setGroupSize(getGroupSize().clone());
    clone.setUnconfirmedFailed(getUnconfirmedFailed().clone());
    return clone;
  }

  public Counter getFailed() {
    Counter counter = new Counter();
    counter.add(getConfirmedFailed());
    counter.add(getUnconfirmedFailed());
    return counter;
  }

  public KeyFactory getKeyFactory() {
    return keyFactory;
  }
}
