package com.github.freeacs.dbi.report;

import java.util.Date;

public class RecordHardwareTR extends Record<RecordHardwareTR> {
  public static KeyFactory keyFactory = new KeyFactory("Unittype", "Profile", "SoftwareVersion");
  private Key key;

  private Date tms;
  private PeriodType periodType;
  private String unittypeName;
  private String profileName;
  private String softwareVersion;

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

  public String getSoftwareVersion() {
    return softwareVersion;
  }

  public Average getCpeUptimeAvg() {
    return cpeUptimeAvg;
  }

  public void setCpeUptimeAvg(Average uptime) {
    this.cpeUptimeAvg = uptime;
  }

  public Average getMemoryTotalAvg() {
    return memoryTotalAvg;
  }

  public void setMemoryTotalAvg(Average memoryTotalAvg) {
    this.memoryTotalAvg = memoryTotalAvg;
  }

  public Average getMemoryFreeAvg() {
    return memoryFreeAvg;
  }

  public void setMemoryFreeAvg(Average memoryFreeAvg) {
    this.memoryFreeAvg = memoryFreeAvg;
  }

  public Average getCpuUsageAvg() {
    return cpuUsageAvg;
  }

  public void setCpuUsageAvg(Average cpuUsageAvg) {
    this.cpuUsageAvg = cpuUsageAvg;
  }

  public Average getProcessCountAvg() {
    return processCountAvg;
  }

  public void setProcessCountAvg(Average processCountAvg) {
    this.processCountAvg = processCountAvg;
  }

  public Average getTemperatureNowAvg() {
    return temperatureNowAvg;
  }

  public void setTemperatureNowAvg(Average temperatureNowAvg) {
    this.temperatureNowAvg = temperatureNowAvg;
  }

  public Average getTemperatureMaxAvg() {
    return temperatureMaxAvg;
  }

  public void setTemperatureMaxAvg(Average temperatureMaxAvg) {
    this.temperatureMaxAvg = temperatureMaxAvg;
  }
}
