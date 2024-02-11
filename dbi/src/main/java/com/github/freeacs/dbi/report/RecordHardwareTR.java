package com.github.freeacs.dbi.report;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
public class RecordHardwareTR extends Record<RecordHardwareTR> {
  public static KeyFactory keyFactory = new KeyFactory("Unittype", "Profile", "SoftwareVersion");
  private final Key key;

  private final Date tms;
  private final PeriodType periodType;
  private final String unittypeName;
  private final String profileName;
  private final String softwareVersion;

  private Average cpeUptimeAvg = new Average(1);
  private Average memoryTotalAvg = new Average(1);
  private Average memoryFreeAvg = new Average(1);
  private Average cpuUsageAvg = new Average(1);
  private Average processCountAvg = new Average(1);
  private Average temperatureNowAvg = new Average(1);
  private Average temperatureMaxAvg = new Average(1);

  public RecordHardwareTR(
      Date tms,
      PeriodType periodType,
      String unittypeName,
      String profileName,
      String softwareVersion) {
    this.tms = tms;
    this.periodType = periodType;
    this.unittypeName = unittypeName;
    this.profileName = profileName;
    if (softwareVersion == null) {
      softwareVersion = "Unknown";
    }
    this.softwareVersion = softwareVersion;
    this.key = keyFactory.makeKey(tms, periodType, unittypeName, profileName, softwareVersion);
  }

  public RecordHardwareTR clone() {
    RecordHardwareTR clone =
        new RecordHardwareTR(tms, periodType, unittypeName, profileName, softwareVersion);
    clone.setCpeUptimeAvg(getCpeUptimeAvg().clone());
    clone.setCpuUsageAvg(getCpuUsageAvg().clone());
    clone.setMemoryFreeAvg(getMemoryFreeAvg().clone());
    clone.setMemoryTotalAvg(getMemoryTotalAvg().clone());
    clone.setProcessCountAvg(getProcessCountAvg().clone());
    clone.setTemperatureMaxAvg(getTemperatureMaxAvg().clone());
    clone.setTemperatureNowAvg(getTemperatureNowAvg().clone());
    return clone;
  }

  public void add(RecordHardwareTR record) {
    getCpeUptimeAvg().add(record.getCpeUptimeAvg());
    getCpuUsageAvg().add(record.getCpuUsageAvg());
    getMemoryFreeAvg().add(record.getMemoryFreeAvg());
    getMemoryTotalAvg().add(record.getMemoryTotalAvg());
    getProcessCountAvg().add(record.getProcessCountAvg());
    getTemperatureMaxAvg().add(record.getTemperatureMaxAvg());
    getTemperatureNowAvg().add(record.getTemperatureNowAvg());
  }

  public KeyFactory getKeyFactory() {
    return keyFactory;
  }
}
