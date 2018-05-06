package com.owera.xaps.syslogserver;

import com.owera.common.log.Logger;
import com.owera.common.util.PropertyReader;
import com.owera.xaps.dbi.Syslog;

public class Properties {

	private static String propertyfile = "xaps-syslog.properties";

	private static PropertyReader pr = new PropertyReader(propertyfile);

	private static Logger logger = new Logger(Properties.class);

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

	public static String getCustomProperty(String customProperty) {
		return pr.getProperty(customProperty);
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

}
