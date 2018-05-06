package com.owera.xaps.spp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.owera.common.util.PropertyReader;
import com.owera.xaps.base.Log;

public class Properties {

	private static PropertyReader pr = new PropertyReader("xaps-spp.properties");

	private static int getInteger(String propertyKey, int defaultValue) {
		String prop = pr.getProperty(propertyKey);
		try {
			return Integer.parseInt(prop);
		} catch (Throwable t) {
			Log.warn(Properties.class, "The value of " + propertyKey + " was not a number, instead using default value " + defaultValue, t);
			return defaultValue;
		}
	}

	private static String getString(String propertyKey, String defaultValue) {
		String prop = pr.getProperty(propertyKey);
		if (prop == null) {
			Log.warn(Properties.class, "The value of " + propertyKey + " was not specified, instead using default value " + defaultValue);
			return defaultValue;
		}
		return prop;
	}

	public static int getTelnetMaxClients() {
		return getInteger("telnet.max-clients", 10);
	}

	public static int getTFTPPort() {
		return getInteger("tftp.port", 1069);
	}

	public static String getProvisioningOutput(String modelName) {
		return getString(modelName + ".output", "SPA");
	}
	
	public static String getUpgradeOutput(String modelName) {
		return getString(modelName + ".upgrade-output", "Regular");
	}

	public static boolean isDiscoveryMode() {
		return getString("discovery.mode", "false").equals("true");
	}

	public static List<String> getReqFilePatterns(String param) {
		return getPropertyList(param, "reqfile", null);
	}

	public static List<String> getReqHeaderPatterns(String param, String headerName) {
		return getPropertyList(param, "reqheader", headerName);
	}

	public static List<String> getReqParamPatterns(String param, String reqParamName) {
		return getPropertyList(param, "reqparam", reqParamName);
	}

	private static List<String> getPropertyList(String param, String sourceType, String name) {
		List<String> propertyList = new ArrayList<String>();
		Map<String, Object> propertyMap = pr.getPropertyMap();
		String propertyPrefix = param + "." + sourceType;
		if (name != null)
			propertyPrefix += "." + name;
		for (String key : propertyMap.keySet()) {
			if (key.startsWith(propertyPrefix)) {
				String property = pr.getProperty(key);
				if (property != null)
					propertyList.add(property);
			}
		}
		return propertyList;
	}

	public static int getXAPSCacheTimeout() {
		return getInteger("xaps.cache.timeout", 300);
	}

	public static int getTelnetRescan() {
		return getInteger("telnet.rescan", 60);
	}

	public static String getAuthMethod() {
		return getString("auth.method", "none");
	}
	
	 public static boolean isFileAuthUsed() {
	    return getString("file.auth.used", "false").equals("true");
	  }

}
