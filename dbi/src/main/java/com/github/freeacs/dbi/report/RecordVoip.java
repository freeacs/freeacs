package com.github.freeacs.dbi.report;

import java.util.Date;

public class RecordVoip extends Record<RecordVoip> {

	private static KeyFactory keyFactory = new KeyFactory("Unittype", "Profile", "SoftwareVersion", "Line");
	private Key key;

	private Date tms;
	private PeriodType periodType;
	private String unittypeName;
	private String profileName;
	private String softwareVersion;
	private String line;

	protected Average mosAvg = new Average(100);
	//	protected Counter mos12Count = new Counter();
	//	protected Counter mos23Count = new Counter();
	//	protected Counter mos34Count = new Counter();
	//	protected Counter mos45Count = new Counter();
	protected Average jitterAvg = new Average();
	//	protected Counter jitterAbove200msCount = new Counter();
	protected Average jitterMax = new Average();
	protected Average percentLossAvg = new Average();
	//	protected Counter percentLossAbove10Count = new Counter();
	protected Average callLengthAvg = new Average(60 * 1000);
	protected Counter callLengthTotal = new Counter(3600);
	protected Counter incomingCallCount = new Counter();
	//	protected Counter sipRegisterFailedCount = new Counter();
	protected Counter outgoingCallCount = new Counter();
	protected Counter outgoingCallFailedCount = new Counter();
	protected Counter abortedCallCount = new Counter();
	// protected Counter VoIPQuality = new Counter();
	protected Counter noSipServiceTime = new Counter();

	protected RecordVoip() {
	}

	public RecordVoip(Date tms, PeriodType periodType, String unittypeName, String profileName, String softwareVersion, String line) {
		this.tms = tms;
		this.periodType = periodType;
		this.unittypeName = unittypeName;
		this.profileName = profileName;
		this.softwareVersion = softwareVersion;
		this.line = line;
		this.key = keyFactory.makeKey(tms, periodType, unittypeName, profileName, softwareVersion, line);
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

	public Average getMosAvg() {
		if (callLengthTotal.get() == 0)
			return new Average(100);
		return mosAvg;
	}

	public void setMosAvg(Average mosAvg) {
		this.mosAvg = mosAvg;
	}

	/*
	public Counter getMos12Count() {
		return mos12Count;
	}

	public void setMos12Count(Counter mos12Count) {
		this.mos12Count = mos12Count;
	}

	public Counter getMos23Count() {
		return mos23Count;
	}

	public void setMos23Count(Counter mos23Count) {
		this.mos23Count = mos23Count;
	}

	public Counter getMos34Count() {
		return mos34Count;
	}

	public void setMos34Count(Counter mos34Count) {
		this.mos34Count = mos34Count;
	}

	public Counter getMos45Count() {
		return mos45Count;
	}

	public void setMos45Count(Counter mos45Count) {
		this.mos45Count = mos45Count;
	}
	*/

	public Average getJitterAvg() {
		if (callLengthTotal.get() == 0)
			return new Average();
		return jitterAvg;
	}

	public void setJitterAvg(Average jitterAvg) {
		this.jitterAvg = jitterAvg;
	}

	//	public Counter getJitterAbove200msCount() {
	//		return jitterAbove200msCount;
	//	}
	//
	//	public void setJitterAbove200msCount(Counter jitterAbove200msCount) {
	//		this.jitterAbove200msCount = jitterAbove200msCount;
	//	}

	public Average getPercentLossAvg() {
		if (callLengthTotal.get() == 0)
			return new Average();
		return percentLossAvg;
	}

	public void setPercentLossAvg(Average percentLossAvg) {
		this.percentLossAvg = percentLossAvg;
	}

	//	public Counter getPercentLossAbove10Count() {
	//		return percentLossAbove10Count;
	//	}
	//
	//	public void setPercentLossAbove10Count(Counter percentLossAbove10Count) {
	//		this.percentLossAbove10Count = percentLossAbove10Count;
	//	}

	public Average getCallLengthAvg() {
		if (callLengthTotal.get() == 0)
			return new Average(60 * 1000);
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

	//	public Counter getSipRegisterFailedCount() {
	//		return sipRegisterFailedCount;
	//	}
	//
	//	public void setSipRegisterFailedCount(Counter sipRegisterFailedCount) {
	//		this.sipRegisterFailedCount = sipRegisterFailedCount;
	//	}

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
	 * This method generates a score from 0 to 100, where 0 is worst and 100 is best. 0
	 * is no voip-service at all. 100 is all calls executed with perfect quality (MOS=4.35).
	 * 
	 * Assumptions:
	 * 1. The interval between register-failed is set to 30 minutes.
	 * 2. The reg-failed counted are only those which happens between 06-24
	 * 3. The reg-failed counted are only those which happen after sip-register has timed out.
	 * 4. The number of calls made/received is 1 pr 10h.
	 * 
	 * The algorithm goes like this:
	 * 1. Find the number of minutes with reg-failed: rfperiod = (reg-failed-count*30)
	 * 2. Find the number of missed calls in this time period: missedcalls = rfperiod / 600.
	 * 3. For every missed call, assign the MOS-score 1.
	 * 4. For every aborted call, assign the MOS-score 1.
	 * 5. For every executed call, use average. MOS-score.
	 * 6. Weigh all of the calls and calculate a (average total-MOS -1)*(100/3.38) (since MOS goes from 1-4.38). 
	 * 
	 */
	public Counter getVoIPQuality() {
		// 2.
		double missedCalls = (double) getNoSipServiceTime().get() / 600d; // Assume one missed called every 10h = 1 call with MOS = 0
		double aborted = getAbortedCallCount().get();
		double incoming = getIncomingCallCount().get();
		double outgoing = getOutgoingCallCount().get();
		double mos = 0;

		if (getMosAvg().get() != null)
			mos = (double) getMosAvg().get() / (double) getMosAvg().getDividend();
		//		System.out.println("missed:" + missed + ", aborted:" + aborted + ", incoming:" + incoming + ", outgoing:" + outgoing + ", mos:" + mos);
		if (missedCalls != 0 || aborted != 0 || incoming != 0 || outgoing != 0 || mos != 0) {
			double totalMos = ((missedCalls + aborted + (incoming + outgoing) * mos) / (missedCalls + aborted + incoming + outgoing)) - 1;
			Counter totalScore = new Counter(10);
			totalScore.set((long) (totalMos * 1000 / 3.38d));
			return totalScore;
		} else {
			return null;
		}
	}

	public RecordVoip clone() {
		RecordVoip clone = new RecordVoip(tms, periodType, unittypeName, profileName, softwareVersion, line);
		clone.setCallLengthTotal(this.getCallLengthTotal().clone());
		clone.setAbortedCallCount(this.getAbortedCallCount().clone());
		clone.setCallLengthAvg(this.getCallLengthAvg().clone());
		clone.setIncomingCallCount(this.getIncomingCallCount().clone());
		//		clone.setJitterAbove200msCount(this.getJitterAbove200msCount().clone());
		clone.setJitterAvg(this.getJitterAvg().clone());
		clone.setJitterMax(this.getJitterMax().clone());
		//		clone.setMos12Count(this.getMos12Count().clone());
		//		clone.setMos23Count(this.getMos23Count().clone());
		//		clone.setMos34Count(this.getMos34Count().clone());
		//		clone.setMos45Count(this.getMos45Count().clone());
		clone.setMosAvg(this.getMosAvg().clone());
		clone.setOutgoingCallCount(this.getOutgoingCallCount().clone());
		clone.setOutgoingCallFailedCount(this.getOutgoingCallFailedCount().clone());
		//		clone.setPercentLossAbove10Count(this.getPercentLossAbove10Count().clone());
		clone.setPercentLossAvg(this.getPercentLossAvg().clone());
		clone.setNoSipServiceTime(this.getNoSipServiceTime().clone());
		return clone;
	}

	public void add(RecordVoip record) {
		this.getCallLengthTotal().add(record.getCallLengthTotal());
		this.getAbortedCallCount().add(record.getAbortedCallCount());
		this.getCallLengthAvg().add(record.getCallLengthAvg());
		this.getIncomingCallCount().add(record.getIncomingCallCount());
		//		this.getJitterAbove200msCount().add(record.getJitterAbove200msCount());
		this.getJitterAvg().add(record.getJitterAvg());
		this.getJitterMax().add(record.getJitterMax());
		//		this.getMos12Count().add(record.getMos12Count());
		//		this.getMos23Count().add(record.getMos23Count());
		//		this.getMos34Count().add(record.getMos34Count());
		//		this.getMos45Count().add(record.getMos45Count());
		this.getMosAvg().add(record.getMosAvg());
		this.getOutgoingCallCount().add(record.getOutgoingCallCount());
		this.getOutgoingCallFailedCount().add(record.getOutgoingCallFailedCount());
		//		this.getPercentLossAbove10Count().add(record.getPercentLossAbove10Count());
		this.getPercentLossAvg().add(record.getPercentLossAvg());
		this.getNoSipServiceTime().add(record.getNoSipServiceTime());
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

	public Average getJitterMax() {
		return jitterMax;
	}

	public void setJitterMax(Average jitterMax) {
		this.jitterMax = jitterMax;
	}

	public Counter getNoSipServiceTime() {
		return noSipServiceTime;
	}

	public void setNoSipServiceTime(Counter noSipServiceTime) {
		this.noSipServiceTime = noSipServiceTime;
	}
}
