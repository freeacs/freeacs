package com.owera.xapsws.impl;

import com.owera.common.log.Logger;
import com.owera.common.util.PropertyReader;

public class Properties {

	private static PropertyReader pr = new PropertyReader("xaps-ws.properties");
	private static Logger logger = new Logger();

	public static String getCustomProperty(String customProperty) {
		return pr.getProperty(customProperty);
	}

	@SuppressWarnings("unused")
	private static int getInteger(String propertyKey, int defaultValue) {
		String prop = pr.getProperty(propertyKey);
		try {
			return Integer.parseInt(prop);
		} catch (Throwable t) {
			logger.warn("The value of " + propertyKey + " was not a number, instead using default value " + defaultValue, t);
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

	public static String getRedirectUrl() {
		return getString("redirect-url", null);
	}
}
