package com.github.freeacs.stun;

import com.github.freeacs.common.db.ConnectionProperties;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.function.Supplier;

public class Properties {

	static Config config = ConfigFactory.load();

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

	private static boolean getBooleab(String propertyKey) {
		return config.getBoolean(propertyKey);
	}

	static boolean checkIfIpIsPublic() {
		return getBooleab("kick.check-public-ip");
	}

	static int getKickRescan() {
		return getInteger("kick.rescan", 60);
	}

	static int getKickInterval() {
		return getInteger("kick.interval", 1000);
	}

	static int getPrimaryPort() {
		return getInteger("primary.port", 3478);
	}

	static int getSecondayPort() {
		return getInteger("secondary.port", 3479);
	}

	static String getPrimaryIp() {
		return getString("primary.ip", null);
	}

	static String getSecondaryIp() {
		return getString("secondary.ip", null);
	}

	static boolean runWithStun() {
		return "true".equals(getString("test.runwithstun", "true").toLowerCase());
	}

	static int getMaxConn() {
		Integer maxConn = getInteger("db.max-connections", 10);
		if (maxConn < 5)
			maxConn = 5;
		return maxConn;
	}

	static boolean expectPortForwarding() {
		return "true".equals(getString("kick.expect-port-forwarding", "false").toLowerCase());
	}

	static int getMaxConn(final String infix) {
		return getInteger("db." + infix + ".maxconn", ConnectionProperties.maxconn);
	}

	static long getMaxAge(final String infix) {
		return getLong("db." + infix + ".maxage", ConnectionProperties.maxage);
	}

	static String getUrl(final String infix) {
		return Optional.ofNullable(getString("db." + infix + ".url", null))
				.orElseGet(new Supplier<String>() {
					@Override
					public String get() {
						return getString("db." +infix, null);
					}
				});
	}
	
}
