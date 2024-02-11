package com.github.freeacs.dbi.report;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
public class RecordVoipTR extends Record<RecordVoipTR> {
  public static KeyFactory keyFactory =
      new KeyFactory("Unittype", "Profile", "SoftwareVersion", "Line", "LineStatus");
  private Key key;

  private Date tms;
  private PeriodType periodType;
  private String unittypeName;
  private String profileName;
  private String softwareVersion;
  private String line;
  private String lineStatus;

  protected Counter overruns = new Counter();
  protected Counter underruns = new Counter();
  protected Average percentLossAvg = new Average();
  protected Average callLengthAvg = new Average(60 * 1000);
  protected Counter callLengthTotal = new Counter(3600 * 1000);
  protected Counter incomingCallCount = new Counter();
  protected Counter outgoingCallCount = new Counter();
  protected Counter outgoingCallFailedCount = new Counter();
  protected Counter abortedCallCount = new Counter();
  protected Counter noSipServiceTime = new Counter();

  public RecordVoipTR(
      Date tms,
      PeriodType periodType,
      String unittypeName,
      String profileName,
      String softwareVersion,
      String line,
      String lineStatus) {
    this.tms = tms;
    this.periodType = periodType;
    this.unittypeName = unittypeName;
    this.profileName = profileName;
    this.softwareVersion = softwareVersion;
    this.line = line;
    this.lineStatus = lineStatus;
    this.key =
        keyFactory.makeKey(
            tms, periodType, unittypeName, profileName, softwareVersion, line, lineStatus);
  }

  public Average getPercentLossAvg() {
    if (callLengthTotal.get() == 0) {
      return new Average();
    }
    return percentLossAvg;
  }

  public Average getCallLengthAvg() {
    if (callLengthTotal.get() == 0) {
      return new Average(60 * 1000);
    }
    return callLengthAvg;
  }

  public RecordVoipTR clone() {
    RecordVoipTR clone =
        new RecordVoipTR(
            tms, periodType, unittypeName, profileName, softwareVersion, line, lineStatus);
    clone.setOverruns(getOverruns().clone());
    clone.setUnderruns(getUnderruns().clone());
    clone.setCallLengthTotal(getCallLengthTotal().clone());
    clone.setAbortedCallCount(getAbortedCallCount().clone());
    clone.setCallLengthAvg(getCallLengthAvg().clone());
    clone.setIncomingCallCount(getIncomingCallCount().clone());
    clone.setOutgoingCallCount(getOutgoingCallCount().clone());
    clone.setOutgoingCallFailedCount(getOutgoingCallFailedCount().clone());
    clone.setPercentLossAvg(getPercentLossAvg().clone());
    clone.setNoSipServiceTime(getNoSipServiceTime().clone());
    return clone;
  }

  public void add(RecordVoipTR record) {
    getOverruns().add(record.getOverruns());
    getUnderruns().add(record.getUnderruns());
    getCallLengthTotal().add(record.getCallLengthTotal());
    getAbortedCallCount().add(record.getAbortedCallCount());
    getCallLengthAvg().add(record.getCallLengthAvg());
    getIncomingCallCount().add(record.getIncomingCallCount());
    getOutgoingCallCount().add(record.getOutgoingCallCount());
    getOutgoingCallFailedCount().add(record.getOutgoingCallFailedCount());
    getPercentLossAvg().add(record.getPercentLossAvg());
    getNoSipServiceTime().add(record.getNoSipServiceTime());
  }

  public KeyFactory getKeyFactory() {
    return keyFactory;
  }
}
