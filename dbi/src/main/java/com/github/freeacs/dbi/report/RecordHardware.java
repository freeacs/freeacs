package com.github.freeacs.dbi.report;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
public class RecordHardware extends Record<RecordHardware> {
  public static KeyFactory keyFactory = new KeyFactory("Unittype", "Profile", "SoftwareVersion");
  private Key key;

  private Date tms;
  private PeriodType periodType;
  private String unittypeName;
  private String profileName;
  private String softwareVersion;

  /** Private Counter unitCount = new Counter();. */
  private Counter bootCount = new Counter();

  private Counter bootWatchdogCount = new Counter();
  private Counter bootMiscCount = new Counter();
  private Counter bootPowerCount = new Counter();
  private Counter bootResetCount = new Counter();
  private Counter bootProvCount = new Counter();
  private Counter bootProvSwCount = new Counter();
  private Counter bootProvConfCount = new Counter();
  private Counter bootProvBootCount = new Counter();
  private Counter bootUserCount = new Counter();
  private Average memoryHeapDdrPoolAvg = new Average(1024);
  private Average memoryHeapDdrCurrentAvg = new Average(1024);
  private Average memoryHeapDdrLowAvg = new Average(1024);
  private Average memoryHeapOcmPoolAvg = new Average(1024);
  private Average memoryHeapOcmCurrentAvg = new Average(1024);
  private Average memoryHeapOcmLowAvg = new Average(1024);
  private Average memoryNpDdrPoolAvg = new Average(1);
  private Average memoryNpDdrCurrentAvg = new Average(1);
  private Average memoryNpDdrLowAvg = new Average(1);
  private Average memoryNpOcmPoolAvg = new Average(1);
  private Average memoryNpOcmCurrentAvg = new Average(1);
  private Average memoryNpOcmLowAvg = new Average(1);
  private Average cpeUptimeAvg = new Average(1);

  protected RecordHardware() {}

  public RecordHardware(
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

  public RecordHardware clone() {
    RecordHardware clone =
        new RecordHardware(tms, periodType, unittypeName, profileName, softwareVersion);
    clone.setBootCount(getBootCount().clone());
    clone.setBootMiscCount(getBootMiscCount().clone());
    clone.setBootPowerCount(getBootPowerCount().clone());
    clone.setBootProvBootCount(getBootProvBootCount().clone());
    clone.setBootProvConfCount(getBootProvConfCount().clone());
    clone.setBootProvCount(getBootProvCount().clone());
    clone.setBootProvSwCount(getBootProvSwCount().clone());
    clone.setBootUserCount(getBootUserCount().clone());
    clone.setBootWatchdogCount(getBootWatchdogCount().clone());
    clone.setMemoryHeapDdrPoolAvg(getMemoryHeapDdrPoolAvg().clone());
    clone.setMemoryHeapDdrCurrentAvg(getMemoryHeapDdrCurrentAvg().clone());
    clone.setMemoryHeapDdrLowAvg(getMemoryHeapDdrLowAvg().clone());
    clone.setMemoryHeapOcmPoolAvg(getMemoryHeapOcmPoolAvg().clone());
    clone.setMemoryHeapOcmCurrentAvg(getMemoryHeapOcmCurrentAvg().clone());
    clone.setMemoryHeapOcmLowAvg(getMemoryHeapOcmLowAvg().clone());
    clone.setMemoryNpDdrPoolAvg(getMemoryNpDdrPoolAvg().clone());
    clone.setMemoryNpDdrCurrentAvg(getMemoryNpDdrCurrentAvg().clone());
    clone.setMemoryNpDdrLowAvg(getMemoryNpDdrLowAvg().clone());
    clone.setMemoryNpOcmPoolAvg(getMemoryNpOcmPoolAvg().clone());
    clone.setMemoryNpOcmCurrentAvg(getMemoryNpOcmCurrentAvg().clone());
    clone.setMemoryNpOcmLowAvg(getMemoryNpOcmLowAvg().clone());
    clone.setCpeUptimeAvg(getCpeUptimeAvg().clone());
    return clone;
  }

  public void add(RecordHardware record) {
    getBootCount().add(record.getBootCount());
    getBootMiscCount().add(record.getBootMiscCount());
    getBootPowerCount().add(record.getBootPowerCount());
    getBootProvBootCount().add(record.getBootProvBootCount());
    getBootProvConfCount().add(record.getBootProvConfCount());
    getBootProvCount().add(record.getBootProvCount());
    getBootProvSwCount().add(record.getBootProvSwCount());
    getBootResetCount().add(record.getBootResetCount());
    getBootUserCount().add(record.getBootUserCount());
    getBootWatchdogCount().add(record.getBootWatchdogCount());
    getMemoryHeapDdrPoolAvg().add(record.getMemoryHeapDdrPoolAvg());
    getMemoryHeapDdrCurrentAvg().add(record.getMemoryHeapDdrCurrentAvg());
    getMemoryHeapDdrLowAvg().add(record.getMemoryHeapDdrLowAvg());
    getMemoryHeapOcmPoolAvg().add(record.getMemoryHeapOcmPoolAvg());
    getMemoryHeapOcmCurrentAvg().add(record.getMemoryHeapOcmCurrentAvg());
    getMemoryHeapOcmLowAvg().add(record.getMemoryHeapOcmLowAvg());
    getMemoryNpDdrPoolAvg().add(record.getMemoryNpDdrPoolAvg());
    getMemoryNpDdrCurrentAvg().add(record.getMemoryNpDdrCurrentAvg());
    getMemoryNpDdrLowAvg().add(record.getMemoryNpDdrLowAvg());
    getMemoryNpOcmPoolAvg().add(record.getMemoryNpOcmPoolAvg());
    getMemoryNpOcmCurrentAvg().add(record.getMemoryNpOcmCurrentAvg());
    getMemoryNpOcmLowAvg().add(record.getMemoryNpOcmLowAvg());
    getCpeUptimeAvg().add(record.getCpeUptimeAvg());
  }

  public KeyFactory getKeyFactory() {
    return keyFactory;
  }
}
