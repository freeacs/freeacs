package com.owera.xaps.syslogserver;

import com.owera.common.db.ConnectionProperties;
import com.owera.common.log.Logger;
import com.owera.xaps.dbi.Syslog;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.Optional;
import java.util.function.Supplier;

public class Properties {

	private static final  Config config = ConfigFactory.load();

	private static Logger logger = new Logger(Properties.class);

	private static int getInteger(String propertyKey, int defaultValue) {
		if (!config.hasPath(propertyKey)) {
			logger.warn("The value of " + propertyKey + " was not specified, instead using default value " + defaultValue);
			return defaultValue;
		}
		try {
			return config.getInt(propertyKey);
		} catch (Throwable t) {
			logger.warn("The value of " + propertyKey + " was not a number, instead using default value " + defaultValue);
			return defaultValue;
		}
	}

	private static long getLong(String propertyKey, long defaultValue) {
		if (!config.hasPath(propertyKey)) {
			logger.warn("The value of " + propertyKey + " was not specified, instead using default value " + defaultValue);
			return defaultValue;
		}
		try {
			return config.getLong(propertyKey);
		} catch (Throwable t) {
			logger.warn("The value of " + propertyKey + " was not a number, instead using default value " + defaultValue);
			return defaultValue;
		}
	}


	private static String getString(String propertyKey, String defaultValue) {
		if (!config.hasPath(propertyKey)) {
			logger.warn("The value of " + propertyKey + " was not specified, instead using default value " + defaultValue);
			return defaultValue;
		}
		return config.getString(propertyKey);
	}

	public static boolean isSimulation() {
		return "true".equals(getString("simulation", "false"));
	}
	
	public static int getPort() {
		return getInteger("port", 9116);
	}

	public static int getFailoverProcessInterval() {
		return getInteger("failover-process-interval", 30);
	}

	public static int getReceiveBufferSize() {
		return getInteger("receive-buffer-size", 1024 * 10);
	}

	public static int getMaxMessagesInBuffer() {
		return getInteger("max-messages-in-buffer", 100000);
	}

	public static int getMaxMessagesInDuplicateBuffer() {
		return getInteger("max-message-in-duplicate-buffer", 100000);
	}

	public static int getMaxSyslogdbThreads() {
		return getInteger("max-syslogdb-threads", 1);
	}

	public static int getMaxFailoverMessageAge() {
		return getInteger("max-failover-message-age", 24);
	}

	public static String getUnknownUnitsAction() {
		return getString("unknown-units", "discard");
	}
	
	public static int getMinFreeDiskSpace() {
		return getInteger("min-free-disk-space", 100);
	}

	public static int getMaxMessagesPrMinute() {
		return getInteger("max-message-pr-minute", 10000);
	}

	public static String getDeviceIdPattern(int index) {
		return getString("deviceid-pattern." + index, null);
	}
	
	public static int getMaxDBCommitQueue() {
		return getInteger("max-syslog-db-commit-queue", Syslog.defaultMaxInsertCount);
	}
	
	public static int getMinDBCommitDelay() {
		return getInteger("min-syslog-db-commit-delay", Syslog.defaultMinTmsDelay);
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
