package com.owera.xaps.monitor;

import java.util.Map;

import com.owera.common.util.PropertyReader;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class Properties {

	private static Config config = ConfigFactory.parseResources("xaps-monitor.conf");

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

}
