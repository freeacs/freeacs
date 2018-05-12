package com.owera.xaps.stun;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Properties {

	private static Config config = ConfigFactory.load();

	private static Logger logger = LoggerFactory.getLogger(Properties.class);

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

	private static String getString(String propertyKey, String defaultValue) {
		if (!config.hasPath(propertyKey)) {
			logger.warn("The value of " + propertyKey + " was not specified, instead using default value " + defaultValue);
			return defaultValue;
		}
		return config.getString(propertyKey);
	}

	public static int getKickRescan() {
		return getInteger("kick.rescan", 60);
	}

	public static int getKickInterval() {
		return getInteger("kick.interval", 1000);
	}

	public static int getPrimaryPort() {
		return getInteger("primary.port", 3478);
	}

	public static int getSecondayPort() {
		return getInteger("secondary.port", 3479);
	}

	public static String getPrimaryIp() {
		return getString("primary.ip", null);
	}

	public static String getSecondaryIp() {
		return getString("secondary.ip", null);
	}

	public static boolean runWithStun() {
		return "true".equals(getString("test.runwithstun", "true").toLowerCase());
	}

	public static int getMaxConn() {
		Integer maxConn = getInteger("db.max-connections", 10);
		if (maxConn < 5)
			maxConn = 5;
		return maxConn;
	}

	public static boolean expectPortForwarding() {
		return "true".equals(getString("kick.expect-port-forwarding", "false").toLowerCase());
	}

	
}
