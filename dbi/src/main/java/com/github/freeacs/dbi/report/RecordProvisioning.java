package com.github.freeacs.dbi.report;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
public class RecordProvisioning extends Record<RecordProvisioning> {
  private static final KeyFactory keyFactory =
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

  public KeyFactory getKeyFactory() {
    return keyFactory;
  }
}
