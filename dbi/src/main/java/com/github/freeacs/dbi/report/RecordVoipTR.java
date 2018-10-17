package com.github.freeacs.dbi.report;

import java.util.Date;

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

  protected RecordVoipTR() {}

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

  public Average getPercentLossAvg() {
    if (callLengthTotal.get() == 0) {
      return new Average();
    }
    return percentLossAvg;
  }

  public void setPercentLossAvg(Average percentLossAvg) {
    this.percentLossAvg = percentLossAvg;
  }

  public Average getCallLengthAvg() {
    if (callLengthTotal.get() == 0) {
      return new Average(60 * 1000);
    }
    return callLengthAvg;
  }

  public void setCallLengthAvg(Average callLengthAvg) {
    this.callLengthAvg = callLengthAvg;
  }

  public Counter getCallLengthTotal() {
    return callLengthTotal;
  }

  public void setCallLengthTotal(Counter callLengthTotal) {
    this.callLengthTotal = callLengthTotal;
  }

  public Counter getIncomingCallCount() {
    return incomingCallCount;
  }

  public void setIncomingCallCount(Counter incomingCallCount) {
    this.incomingCallCount = incomingCallCount;
  }

  public Counter getOutgoingCallCount() {
    return outgoingCallCount;
  }

  public void setOutgoingCallCount(Counter outgoingCallCount) {
    this.outgoingCallCount = outgoingCallCount;
  }

  public Counter getOutgoingCallFailedCount() {
    return outgoingCallFailedCount;
  }

  public void setOutgoingCallFailedCount(Counter outgoingCallFailedCount) {
    this.outgoingCallFailedCount = outgoingCallFailedCount;
  }

  public Counter getAbortedCallCount() {
    return abortedCallCount;
  }

  public void setAbortedCallCount(Counter abortedCallCount) {
    this.abortedCallCount = abortedCallCount;
  }

  /**
   * This method generates a score from 0 to 100, where 0 is worst and 100 is best. 0 is no
   * voip-service at all. 100 is all calls executed with perfect quality (MOS=4.35).
   *
   * <p>Assumptions: 1. The interval between register-failed is set to 30 minutes. 2. The reg-failed
   * counted are only those which happens between 06-24 3. The reg-failed counted are only those
   * which happen after sip-register has timed out. 4. The number of calls made/received is 1 pr
   * 10h.
   *
   * <p>The algorithm goes like this: 1. Find the number of minutes with reg-failed: rfperiod =
   * (reg-failed-count*30) 2. Find the number of missed calls in this time period: missedcalls =
   * rfperiod / 600. 3. For every missed call, assign the MOS-score 1. 4. For every aborted call,
   * assign the MOS-score 1. 5. For every executed call, use average. MOS-score. 6. Weigh all of the
   * calls and calculate a (average total-MOS -1)*(100/3.36) (since MOS goes from 1-4.36).
   */
  public Counter getVoIPQuality() {
    // 2.
    double missedCalls =
        (double) getNoSipServiceTime().get()
            / 600d; // Assume one missed called every 10h = 1 call with MOS = 0
    double aborted = getAbortedCallCount().get();
    double incoming = getIncomingCallCount().get();
    double outgoing = getOutgoingCallCount().get();
    double mos = 0;

    // make a simple MOS:
    // underruns + overruns = runs (counts as 1 packet for each run)
    // totalpackets = calllengthtotal * 50
    // packetloss = totalpackets*percentloss;
    // loss' = (packetloss + runs)/(totalpackets)
    // loss': 0.0 => MOS = 4.36
    // loss': >=1.0 => MOS = 1.0
    // formula: 4.36 - 3.36*(underruns + overruns + calllengthtotal * 50 *
    // percentloss/100)/(calllengthtotal * 50)

    if (callLengthTotal.get() != null) {
      mos =
          4.36d
              - 3.36d
                  * (underruns.get()
                      + overruns.get()
                      + callLengthTotal.get() * 2 * percentLossAvg.get())
                  / (callLengthTotal.get() * 50);
    }

    if (missedCalls != 0 || aborted != 0 || incoming != 0 || outgoing != 0 || mos != 0) {
      double totalMos =
          (missedCalls + aborted + (incoming + outgoing) * mos)
                  / (missedCalls + aborted + incoming + outgoing)
              - 1;
      Counter totalScore = new Counter(10);
      totalScore.set((long) (totalMos * 1000 / 3.38d));
      return totalScore;
    } else {
      return null;
    }
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

  public String getLine() {
    return line;
  }

  public String getSoftwareVersion() {
    return softwareVersion;
  }

  public Counter getNoSipServiceTime() {
    return noSipServiceTime;
  }

  public void setNoSipServiceTime(Counter noSipServiceTime) {
    this.noSipServiceTime = noSipServiceTime;
  }

  public Counter getOverruns() {
    return overruns;
  }

  public void setOverruns(Counter overruns) {
    this.overruns = overruns;
  }

  public Counter getUnderruns() {
    return underruns;
  }

  public void setUnderruns(Counter underruns) {
    this.underruns = underruns;
  }

  public String getLineStatus() {
    return lineStatus;
  }
}
