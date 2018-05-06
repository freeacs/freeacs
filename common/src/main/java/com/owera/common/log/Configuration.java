package com.owera.common.log;

import java.util.List;

public class Configuration {
	private String logName;
	private List<Appender> appenders;
	private int severityLevel;

	public Configuration(String logName, List<Appender> appenders, int severityLevel) {
		this.logName = logName;
		this.appenders = appenders;
		this.severityLevel = severityLevel;
	}

	public String getLogName() {
		return logName;
	}

	public List<Appender> getAppenders() {
		return appenders;
	}

	public int getSeverityLevel() {
		return severityLevel;
	}

	public String toString() {
		String str = "LogName: " + logName + ", SeverityLevel: " + severityLevel + "\n";
		int count = 1;
		for (Appender appender : appenders) {
			str += "\tAppender-" + count + ": " + appender + "\n";
			count++;
		}
		return str;

	}

}
