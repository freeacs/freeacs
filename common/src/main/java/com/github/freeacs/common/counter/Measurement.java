package com.github.freeacs.common.counter;

/**
 * Contains measurement data for one type (@see MeasurementTypes).
 * 
 * @author me3
 */
public class Measurement {
	private int hits;
	private long executeTime;

	public Measurement(int param, long execute) {
		this.hits = param;
		this.executeTime = execute;
	}

	public void add(Measurement maaling) {
		add(maaling.getHits(), maaling.getUtfortms());
	}

	public void subtract(Measurement maaling) {
		subtract(maaling.getHits(), maaling.getUtfortms());
	}

	public void add(int hits, long utfortms) {
		this.hits += hits;
		this.executeTime += utfortms;
	}

	public void subtract(int param, long utfortms) {
		this.hits -= param;
		if (this.hits < 0)
			this.hits = 0;
		this.executeTime -= utfortms;
		if (this.executeTime < 0)
			this.executeTime = 0;
	}

	public long getAvg() {
		if (hits > 0)
			return executeTime / hits;
		else
			return 0;
	}

	public String getAvgHTMLFormatted() {
		if (hits > 0)
			return "" + executeTime / hits;
		else
			return "&nbsp;";
	}

	public void inc(long utfortms) {
		add(1, utfortms);
	}

	public int getHits() {
		return hits;
	}

	public String getHitsHTMLFormatted() {
		if (hits > 0)
			return "" + hits;
		else
			return "&nbsp;";
	}

	public long getUtfortms() {
		return executeTime;
	}

	public void setHits(int i) {
		hits = i;
	}

	public void setUtfortms(long l) {
		executeTime = l;
	}

	/*
	 * public String toString() { String ret = ""; ret +=
	 * StringUtility.format("" + hits, 6, ' ', true, false, false) + " "; ret +=
	 * StringUtility.format("" + getAvg(), 6, ' ', true, false, false); return
	 * ret; }
	 */

}
