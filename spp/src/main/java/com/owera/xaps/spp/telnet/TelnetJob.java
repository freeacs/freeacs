package com.owera.xaps.spp.telnet;

import com.owera.xaps.dbi.Job;

public class TelnetJob {
	private Job job;
	private String unitId;
	private long jobStartedTms;

	public TelnetJob(Job job, String unitId, long jobStartedTms) {
		this.job = job;
		this.unitId = unitId;
		this.jobStartedTms = jobStartedTms;
	}

	public Job getJob() {
		return job;
	}

	public void setJob(Job job) {
		this.job = job;
	}

	public String getUnitId() {
		return unitId;
	}

	public void setUnitId(String unitId) {
		this.unitId = unitId;
	}

	public long getJobStartedTms() {
		return jobStartedTms;
	}

	public void setJobStartedTms(long jobStartedTms) {
		this.jobStartedTms = jobStartedTms;
	}
}
