package com.owera.xaps.core;

import com.owera.common.log.Logger;
import com.owera.common.util.PropertyReader;

public class Properties {

	private static String propertyfile = "xaps-core.properties";

	private static PropertyReader pr = new PropertyReader(propertyfile);

	private static Logger logger = new Logger();

	private static int getInteger(String propertyKey, int defaultValue) {
		String prop = pr.getProperty(propertyKey);
		try {
			return Integer.parseInt(prop);
		} catch (Throwable t) {
			logger.warn("The value of " + propertyKey + " was not a number, instead using default value " + defaultValue);
			return defaultValue;
		}
	}

	private static String getString(String propertyKey, String defaultValue) {
		String prop = pr.getProperty(propertyKey);
		if (prop == null) {
			logger.warn("The value of " + propertyKey + " was not specified, instead using default value " + defaultValue);
			return defaultValue;
		}
		return prop;
	}

	public static String getSyslogCleanup() {
		return getString("syslog.cleanup", "normal");
	}

	public static int getCompletedJobLimit() {
		return getInteger("completed.job.limit", 48);
	}

	//	public static int getNotProvisionedReportLimit() {
	//		return getInteger("notprovisioned.report.limit", 48);
	//	}

	public static int getSyslogSeverityLimit(int severity) {
		int defaultLimit = 7;
		if (severity <= 3)
			defaultLimit = 90;
		if (severity == 4)
			defaultLimit = 60;
		if (severity == 5)
			defaultLimit = 30;
		if (severity == 6)
			defaultLimit = 7;
		if (severity > 6)
			defaultLimit = 4;
		return getInteger("syslog.severity." + severity + ".limit", defaultLimit);
	}

	public static String getCustomProperty(String customProperty) {
		return pr.getProperty(customProperty);
	}

	public static String getReports() {
		return getString("reports", "Basic");
	}
	
	public static boolean isStaging() {
		return getString("staging", "false").equals("true");
	}

	public static Integer getShellScriptPoolSize() {
		return getInteger("shellscript.poolsize", 4);
	}

	public static Integer getShellScriptLimit() {
		return getInteger("shellscript.limit", 7);
	}

}
