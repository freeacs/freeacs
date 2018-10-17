package com.github.freeacs.dbi.report;

import java.util.Date;

public class RecordProvisioning extends Record<RecordProvisioning> {
  private static KeyFactory keyFactory =
      new KeyFactory("Unittype", "Profile", "SoftwareVersion", "Output");
  private Key key;

  private Date tms;
  private PeriodType periodType;
  private String unittypeName;
  private String profileName;
  private String softwareVersion;
  private String output;

  private Counter provisioningOkCount = new Counter();
  private Counter provisioningRescheduledCount = new Counter();
  private Counter provisioningErrorCount = new Counter();
  private Counter provisioningMissingCount = new Counter();
  private Average sessionLengthAvg = new Average(1000);

  protected RecordProvisioning() {}

  public RecordProvisioning(
      Date tms,
      PeriodType periodType,
      String unittypeName,
      String profileName,
      String softwareVersion,
      String output) {
    this.tms = tms;
    this.periodType = periodType;
    this.unittypeName = unittypeName;
    this.profileName = profileName;
    this.softwareVersion = softwareVersion;
    this.output = output;
    this.key =
        keyFactory.makeKey(tms, periodType, unittypeName, profileName, softwareVersion, output);
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

  @Override
  public RecordProvisioning clone() {
    RecordProvisioning clone =
        new RecordProvisioning(tms, periodType, unittypeName, profileName, softwareVersion, output);
    clone.setProvisioningOkCount(getProvisioningOkCount());
    clone.setProvisioningRescheduledCount(getProvisioningRescheduledCount());
    clone.setProvisioningErrorCount(getProvisioningErrorCount());
    clone.setProvisioningMissingCount(getProvisioningMissingCount());
    clone.setSessionLengthAvg(getSessionLengthAvg());
    return clone;
  }

  @Override
  public void add(RecordProvisioning record) {
    getProvisioningOkCount().add(record.getProvisioningOkCount());
    getProvisioningRescheduledCount().add(record.getProvisioningRescheduledCount());
    getProvisioningErrorCount().add(record.getProvisioningErrorCount());
    getProvisioningMissingCount().add(record.getProvisioningMissingCount());
    getSessionLengthAvg().add(record.getSessionLengthAvg());
  }

  public Average getProvisioningQuality() {
    Average quality = new Average(1);
    quality.add(0, getProvisioningErrorCount().get());
    // a missing heartbeat/provsioning weighs 1/4 of an actual ERROR
    quality.add(0, getProvisioningMissingCount().get() / 4);
    // a rescheduled provisioning is considered OK, but weighs 1/4 of an OK
    quality.add(100, getProvisioningRescheduledCount().get() / 4);
    quality.add(100, getProvisioningOkCount().get());
    return quality;
  }

  public KeyFactory getKeyFactory() {
    return keyFactory;
  }

  public Average getSessionLengthAvg() {
    return sessionLengthAvg;
  }

  public void setSessionLengthAvg(Average sessionLengthAvg) {
    this.sessionLengthAvg = sessionLengthAvg;
  }

  public String getSoftwareVersion() {
    return softwareVersion;
  }

  public String getOutput() {
    return output;
  }

  public String toString() {
    return getKey()
        + ": "
        + getProvisioningOkCount().get()
        + ", "
        + getProvisioningRescheduledCount().get()
        + ", "
        + ", "
        + getProvisioningErrorCount().get()
        + ", "
        + getProvisioningMissingCount().get()
        + getSessionLengthAvg().get();
  }

  public Counter getProvisioningOkCount() {
    return provisioningOkCount;
  }

  public void setProvisioningOkCount(Counter provisioningOkCount) {
    this.provisioningOkCount = provisioningOkCount;
  }

  public Counter getProvisioningRescheduledCount() {
    return provisioningRescheduledCount;
  }

  public void setProvisioningRescheduledCount(Counter provisioningRescheduledCount) {
    this.provisioningRescheduledCount = provisioningRescheduledCount;
  }

  public Counter getProvisioningErrorCount() {
    return provisioningErrorCount;
  }

  public void setProvisioningErrorCount(Counter provisioningErrorCount) {
    this.provisioningErrorCount = provisioningErrorCount;
  }

  public Counter getProvisioningMissingCount() {
    return provisioningMissingCount;
  }

  public void setProvisioningMissingCount(Counter provisioningMissingCount) {
    this.provisioningMissingCount = provisioningMissingCount;
  }
}
