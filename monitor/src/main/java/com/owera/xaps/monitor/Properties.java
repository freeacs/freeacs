package com.owera.xaps.monitor;

import java.util.Map;

import com.owera.common.util.PropertyReader;

public class Properties {

	private static PropertyReader pr = new PropertyReader("xaps-monitor.properties");

	public static String get(String property) {
		return pr.getProperty(property);
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

	private static int getInteger(String propertyKey, int defaultValue) {
		String prop = pr.getProperty(propertyKey);
		try {
			return Integer.parseInt(prop);
		} catch (Throwable t) {
			return defaultValue;
		}
	}

	private static String getString(String propertyKey, String defaultValue) {
		String prop = pr.getProperty(propertyKey);
		if (prop == null) {
			return defaultValue;
		}
		return prop;
	}

	public static Map<String, Object> getPropertyMap() {
		return pr.getPropertyMap();
	}

}
