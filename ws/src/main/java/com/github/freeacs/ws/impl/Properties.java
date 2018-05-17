package com.github.freeacs.ws.impl;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.function.Supplier;

public class Properties {

	private static final Config config = ConfigFactory.load();

	private static final Logger logger = LoggerFactory.getLogger(Properties.class);

	private static int getInteger(String propertyKey, int defaultValue) {
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

	private static long getLong(String propertyKey, long defaultValue) {
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
		return getInteger("db." + infix + ".maxconn", 20);
	}

	public static long getMaxAge(final String infix) {
		return getLong("db." + infix + ".maxage", 60000);
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
