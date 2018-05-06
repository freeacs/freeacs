package com.owera.common.log;

public class LogObject {
	private int severity;
	private String coreMessage;
	private String completeMessage;
	private long tms;
	private String logName;
	private String simpleClassName;
	private long currentSecond;
	private String timestampStr;
	private Throwable throwable;

	public int getSeverity() {
		return severity;
	}

	public void setSeverity(int severity) {
		this.severity = severity;
	}

	public long getTms() {
		return tms;
	}

	public void setTms(long tms) {
		this.tms = tms;
	}

	public String getLogName() {
		return logName;
	}

	public void setLogName(String logName) {
		this.logName = logName;
	}

	public long getCurrentSecond() {
		return currentSecond;
	}

	public void setCurrentSecond(long currentSecond) {
		this.currentSecond = currentSecond;
	}

	public String getTimestampStr() {
		return timestampStr;
	}

	public void setTimestampStr(String timestampStr) {
		this.timestampStr = timestampStr;
	}

	public String getSimpleClassName() {
		return simpleClassName;
	}

	public void setSimpleClassName(String simpleClassName) {
		this.simpleClassName = simpleClassName;
	}

	public Throwable getThrowable() {
		return throwable;
	}

	public void setThrowable(Throwable throwable) {
		this.throwable = throwable;
	}

	public String getCoreMessage() {
		return coreMessage;
	}

	public void setCoreMessage(String coreMessage) {
		this.coreMessage = coreMessage;
	}

	public String getCompleteMessage() {
		return completeMessage;
	}

	public void setCompleteMessage(String completeMessage) {
		this.completeMessage = completeMessage;
	}

}
