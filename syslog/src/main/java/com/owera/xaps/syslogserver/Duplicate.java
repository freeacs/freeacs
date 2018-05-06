package com.owera.xaps.syslogserver;

import com.owera.xaps.dbi.SyslogEntry;


public class Duplicate {
	private SyslogEntry entry;
	private long timeout;
	private int count;

	public Duplicate(SyslogEntry entry, long timeout) {
		this.entry = entry;
		this.timeout = timeout;
	}

	public SyslogEntry getEntry() {
		return entry;
	}

	public long getTimeout() {
		return timeout;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public void incCount() {
		this.count++;
	}

}
