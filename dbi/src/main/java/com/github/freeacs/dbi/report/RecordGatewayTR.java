package com.github.freeacs.dbi.report;

import java.util.Date;

public class RecordGatewayTR extends Record<RecordGatewayTR> {
  public static KeyFactory keyFactory = new KeyFactory("Unittype", "Profile", "SoftwareVersion");
  private Key key;

  private Date tms;
  private PeriodType periodType;
  private String unittypeName;
  private String profileName;
  private String softwareVersion;

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

  public String getSoftwareVersion() {
    return softwareVersion;
  }

  public Average getWanUptimeAvg() {
    return wanUptimeAvg;
  }

  public void setWanUptimeAvg(Average wanUptimeAvg) {
    this.wanUptimeAvg = wanUptimeAvg;
  }

  public Average getPingSuccessCountAvg() {
    return pingSuccessCountAvg;
  }

  public void setPingSuccessCountAvg(Average pingSuccessCountAvg) {
    this.pingSuccessCountAvg = pingSuccessCountAvg;
  }

  public Average getPingFailureCountAvg() {
    return pingFailureCountAvg;
  }

  public void setPingFailureCountAvg(Average pingFailureCountAvg) {
    this.pingFailureCountAvg = pingFailureCountAvg;
  }

  public Average getDownloadSpeedAvg() {
    return downloadSpeedAvg;
  }

  public void setDownloadSpeedAvg(Average downloadSpeedAvg) {
    this.downloadSpeedAvg = downloadSpeedAvg;
  }

  public Average getUploadSpeedAvg() {
    return uploadSpeedAvg;
  }

  public void setUploadSpeedAvg(Average uploadSpeedAvg) {
    this.uploadSpeedAvg = uploadSpeedAvg;
  }

  public Average getPingResponseTimeAvg() {
    return pingResponseTimeAvg;
  }

  public void setPingResponseTimeAvg(Average pingResponseTimeAvg) {
    this.pingResponseTimeAvg = pingResponseTimeAvg;
  }
}
