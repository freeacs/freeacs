package com.github.freeacs.dbi.report;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
public class RecordVoipCall extends Record<RecordVoipCall> {
  private static final KeyFactory keyFactory =
      new KeyFactory("Unittype", "Profile", "SoftwareVersion", "Channel");
  private Key key;

  private Date tms;
  private PeriodType periodType;
  private String unittypeName;
  private String profileName;
  private String softwareVersion;
  private String channel;

  private Counter unitCount = new Counter();
  private Average mosAvg = new Average(100);

  protected RecordVoipCall() {}

  public RecordVoipCall(
      Date tms,
      PeriodType periodType,
      String unittypeName,
      String profileName,
      String softwareVersion,
      String channel) {
    this.tms = tms;
    this.periodType = periodType;
    this.unittypeName = unittypeName;
    this.profileName = profileName;
    this.softwareVersion = softwareVersion;
    this.channel = channel;
    this.key =
        keyFactory.makeKey(tms, periodType, unittypeName, profileName, softwareVersion, channel);
  }

  @Override
  public void add(RecordVoipCall record) {
    getUnitCount().add(record.getUnitCount());
  }

  @Override
  public RecordVoipCall clone() {
    RecordVoipCall clone =
        new RecordVoipCall(tms, periodType, unittypeName, profileName, softwareVersion, channel);
    clone.setUnitCount(getUnitCount().clone());
    clone.setMosAvg(getMosAvg().clone());
    return clone;
  }

  public KeyFactory getKeyFactory() {
    return keyFactory;
  }

  public Average getMosAvg() {
    if (unitCount.get() == 0) {
      return new Average(100);
    }
    return mosAvg;
  }
}
