package com.owera.xapsws.impl;

import com.owera.common.db.ConnectionProperties;
import com.owera.common.log.Logger;
import com.owera.common.util.PropertyReader;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.Optional;
import java.util.function.Supplier;

public class Properties {

	private static final Config config = ConfigFactory.parseResources("xaps-ws.conf");

	private static final Logger logger = new Logger();

	public static int getInteger(String propertyKey, int defaultValue) {
		if (!config.hasPath(propertyKey)) {
			logger.warn("The value of " + propertyKey + " was not specified, instead using default value " + defaultValue);
			return defaultValue;
		}
		try {
			return config.getInt(propertyKey);
		} catch (Throwable t) {
			logger.warn("The value of " + propertyKey + " was not a number, instead using default value " + defaultValue, t);
			return defaultValue;
		}
	}

	public static long getLong(String propertyKey, long defaultValue) {
		if (!config.hasPath(propertyKey)) {
			logger.warn("The value of " + propertyKey + " was not specified, instead using default value " + defaultValue);
			return defaultValue;
		}
		try {
			return config.getLong(propertyKey);
		} catch (Throwable t) {
			logger.warn("The value of " + propertyKey + " was not a number, instead using default value " + defaultValue, t);
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

	static String getRedirectUrl() {
		return getString("redirect-url", null);
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
