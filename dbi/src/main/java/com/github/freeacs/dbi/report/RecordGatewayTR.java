package com.github.freeacs.dbi.report;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
public class RecordGatewayTR extends Record<RecordGatewayTR> {
  public static KeyFactory keyFactory = new KeyFactory("Unittype", "Profile", "SoftwareVersion");
  private final Key key;

  private final Date tms;
  private final PeriodType periodType;
  private final String unittypeName;
  private final String profileName;
  private final String softwareVersion;

  private Average wanUptimeAvg = new Average(1);
  private Average pingSuccessCountAvg = new Average(1);
  private Average pingFailureCountAvg = new Average(1);
  private Average pingResponseTimeAvg = new Average(1);
  private Average downloadSpeedAvg = new Average(1024);
  private Average uploadSpeedAvg = new Average(1024);

  public RecordGatewayTR(
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

  public RecordGatewayTR clone() {
    RecordGatewayTR clone =
        new RecordGatewayTR(tms, periodType, unittypeName, profileName, softwareVersion);
    clone.setDownloadSpeedAvg(getDownloadSpeedAvg().clone());
    clone.setPingFailureCountAvg(getPingFailureCountAvg().clone());
    clone.setPingResponseTimeAvg(getPingResponseTimeAvg().clone());
    clone.setPingSuccessCountAvg(getPingSuccessCountAvg().clone());
    clone.setUploadSpeedAvg(getUploadSpeedAvg().clone());
    clone.setWanUptimeAvg(getWanUptimeAvg().clone());
    return clone;
  }

  public void add(RecordGatewayTR record) {
    getDownloadSpeedAvg().add(record.getDownloadSpeedAvg());
    getPingFailureCountAvg().add(record.getPingFailureCountAvg());
    getPingResponseTimeAvg().add(record.getPingResponseTimeAvg());
    getPingSuccessCountAvg().add(record.getPingSuccessCountAvg());
    getUploadSpeedAvg().add(record.getUploadSpeedAvg());
    getWanUptimeAvg().add(record.getWanUptimeAvg());
  }

  public KeyFactory getKeyFactory() {
    return keyFactory;
  }
}
