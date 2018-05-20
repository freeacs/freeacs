package com.github.freeacs.core;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class Properties {

	public static String REPORTS;
	public static boolean STAGING;
	public static Integer SHELL_SCRIPT_LIMIT;
	public static Integer COMPLETED_JOB_LIMIT;
	public static Integer SHELL_SCRIPT_POOL_SIZE;
	public static String SYSLOG_CLEANUP;
	private static final Map<Integer, Integer> SYSLOG_SEVERITY_LIMIT = new HashMap<>();

	@Value("${syslog.cleanup:normal}")
	public void setSyslogCleanup(String syslogCleanup) {
		SYSLOG_CLEANUP = syslogCleanup;
	}

	@Value("${completed.job.limit:40}")
	public void setCompletedJobLimit(Integer completedJobLimit) {
		COMPLETED_JOB_LIMIT = completedJobLimit;
	}

	@Value("${reports:Basic}")
	public void setReports(String reports) {
		REPORTS = reports;
	}

	@Value("${staging:false}")
	public void setStaging(Boolean staging) {
		STAGING = staging;
	}

	@Value("${shellscript.limit:7}")
	public void setShellScriptLimit(Integer shellScriptLimit) {
		SHELL_SCRIPT_LIMIT = shellScriptLimit;
	}

	@Value("${shellscript.poolsize:4}")
	public void setShellScriptPoolSize(Integer shellScriptPoolSize) {
		SHELL_SCRIPT_POOL_SIZE = shellScriptPoolSize;
	}

	@Value("${syslog.severity.0.limit:90}")
	public void setSyslogSeverity0Limit(Integer severityLimit) {
		SYSLOG_SEVERITY_LIMIT.put(0, severityLimit);
	}

	@Value("${syslog.severity.1.limit:90}")
	public void setSyslogSeverity1Limit(Integer severityLimit) {
		SYSLOG_SEVERITY_LIMIT.put(1, severityLimit);
	}

	@Value("${syslog.severity.2.limit:90}")
	public void setSyslogSeverity2Limit(Integer severityLimit) {
		SYSLOG_SEVERITY_LIMIT.put(2, severityLimit);
	}

	@Value("${syslog.severity.3.limit:90}")
	public void setSyslogSeverity3Limit(Integer severityLimit) {
		SYSLOG_SEVERITY_LIMIT.put(3, severityLimit);
	}

	@Value("${syslog.severity.4.limit:60}")
	public void setSyslogSeverity4Limit(Integer severityLimit) {
		SYSLOG_SEVERITY_LIMIT.put(4, severityLimit);
	}

	@Value("${syslog.severity.5.limit:30}")
	public void setSyslogSeverity5Limit(Integer severityLimit) {
		SYSLOG_SEVERITY_LIMIT.put(5, severityLimit);
	}

	@Value("${syslog.severity.6.limit:7}")
	public void setSyslogSeverity6Limit(Integer severityLimit) {
		SYSLOG_SEVERITY_LIMIT.put(6, severityLimit);
	}

	@Value("${syslog.severity.7.limit:4}")
	public void setSyslogSeverity7Limit(Integer severityLimit) {
		SYSLOG_SEVERITY_LIMIT.put(7, severityLimit);
	}

	public static Integer getSyslogSeverityLimit(int severity) {
		return SYSLOG_SEVERITY_LIMIT.get(severity);
	}

}
