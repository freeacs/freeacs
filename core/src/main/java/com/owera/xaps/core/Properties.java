package com.owera.xaps.core;

import com.owera.common.db.ConnectionProperties;
import com.owera.common.log.Logger;
import com.owera.common.util.PropertyReader;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigObject;

import java.util.Optional;
import java.util.function.Supplier;

public class Properties {

	private static Config config = ConfigFactory.parseResources("xaps-core.conf");

	private static Logger logger = new Logger();

	private static int getInteger(String propertyKey, int defaultValue) {
		try {
			return config.getInt(propertyKey);
		} catch (Throwable t) {
			logger.warn("The value of " + propertyKey + " was not a number, instead using default value " + defaultValue);
			return defaultValue;
		}
	}

	private static long getLong(String propertyKey, long defaultValue) {
		try {
			return config.getLong(propertyKey);
		} catch (Throwable t) {
			logger.warn("The value of " + propertyKey + " was not a number, instead using default value " + defaultValue);
			return defaultValue;
		}
	}

	private static String getString(String propertyKey, String defaultValue) {
		String prop = config.getString(propertyKey);
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

	public static int getMaxConn(final String infix) {
		return getInteger("db." + infix + ".maxconn", ConnectionProperties.maxconn);
	}

	public static long getMaxAge(final String infix) {
		return getLong("db." + infix + ".maxage", ConnectionProperties.maxage);
	}

	public static String getUrl(final String infix) {
		return Optional.ofNullable(getString("db." + infix + ".url", null))
				.orElseGet(new Supplier<String>() {
					@Override
					public String get() {
						return getString("db." +infix, null);
					}
				});
	}

}
