package com.owera.xaps.stun;

import com.owera.common.log.Logger;
import com.owera.common.util.PropertyReader;

public class Properties {

	private static String propertyfile = "xaps-stun.properties";

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
		return pr.getProperty("primary.ip");
	}

	public static String getSecondaryIp() {
		return pr.getProperty("secondary.ip");
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
