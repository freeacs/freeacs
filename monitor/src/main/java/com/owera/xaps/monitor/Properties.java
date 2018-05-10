package com.owera.xaps.monitor;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import com.owera.common.db.ConnectionProperties;
import com.owera.common.util.PropertyReader;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import static java.lang.Long.getLong;

public class Properties {

	private static final Config config = ConfigFactory.load();

	public static String get(String property) {
		if (config.hasPath(property)) {
			return config.getString(property);
		}
		return null;
	}

	/**
	 * Derived from fusion.urlbase property
	 * @return
	 */
	public static String getFusionHostname() {
		String fusionURLBase = getFusionURLBase();
		String fusionHostname = fusionURLBase.substring(fusionURLBase.indexOf("//") + 2, fusionURLBase.lastIndexOf("/"));
		int colonPos = fusionHostname.indexOf(":");
		if (colonPos > -1)
			fusionHostname = fusionHostname.substring(0, colonPos);
		return fusionHostname;

	}

	public static String getFusionURLBase() {
		String urlBase = getString("fusion.urlbase", "http://localhost/");
		if (urlBase.equals("http://localhost/")) // backward-compatibility
			urlBase = getString("fusion.url", "http://localhost/");
		if (!urlBase.endsWith("/"))
			urlBase += "/";
		return urlBase;
	}

	public static String getMonitorURLBase() {
		String urlBase = getString("monitor.urlbase", "http://localhost/");
		if (!urlBase.endsWith("/"))
			urlBase += "/";
		return urlBase;
	}

	public static long getRetrySeconds() {
		return (long) getInteger("monitor.retrysec", 300);
	}

	public static int getInteger(String propertyKey, int defaultValue) {
		if (!config.hasPath(propertyKey)) {
			return defaultValue;
		}
		try {
			return config.getInt(propertyKey);
		} catch (Throwable t) {
			return defaultValue;
		}
	}

	public static String getString(String propertyKey, String defaultValue) {
		if (!config.hasPath(propertyKey)) {
			return defaultValue;
		}
		String prop = config.getString(propertyKey);
		if (prop == null) {
			return defaultValue;
		}
		return prop;
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
