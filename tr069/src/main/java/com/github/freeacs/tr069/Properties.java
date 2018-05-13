package com.github.freeacs.tr069;

import com.github.freeacs.base.Log;
import com.github.freeacs.common.db.ConnectionProperties;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.Optional;
import java.util.function.Supplier;

public class Properties {

	private static final Config config = ConfigFactory.load();

	private static String getUnittypeName(SessionData sessionData) {
		String unittypeName = null;
		if (sessionData != null && sessionData.getUnittype() != null)
			unittypeName = sessionData.getUnittype().getName();
		return unittypeName;

	}

	private static String getVersion(SessionData sessionData) {
		String version = null;
		if (sessionData != null && sessionData.getCpeParameters() != null)
			version = sessionData.getCpeParameters().getValue(sessionData.getCpeParameters().SOFTWARE_VERSION);
		return version;
	}

	public static boolean isParameterkeyQuirk(SessionData sessionData) {
		return isQuirk("parameterkey", getUnittypeName(sessionData), getVersion(sessionData));
	}

	public static boolean isUnitDiscovery(SessionData sessionData) {
		return isQuirk("unitdiscovery", getUnittypeName(sessionData), getVersion(sessionData));
	}

	public static boolean isTerminationQuirk(SessionData sessionData) {
		return isQuirk("termination", getUnittypeName(sessionData), getVersion(sessionData));
	}

	public static boolean isXmlCharFilterQuirk(SessionData sessionData) {
		return isQuirk("xmlcharfilter", getUnittypeName(sessionData), getVersion(sessionData));
	}

	public static boolean isPrettyPrintQuirk(SessionData sessionData) {
		return isQuirk("prettyprint", getUnittypeName(sessionData), getVersion(sessionData));
	}

	public static boolean isIgnoreVendorConfigFile(SessionData sessionData) {
		return isQuirk("ignorevendorconfigfile", getUnittypeName(sessionData), getVersion(sessionData));
	}
	
	public static boolean isNextLevel0InGPN(SessionData sessionData) {
	  return isQuirk("nextlevel0ingpn", getUnittypeName(sessionData), getVersion(sessionData));
	}

	private static boolean isQuirk(String quirkName, String unittypeName, String version) {
		if (unittypeName == null) {
			Log.debug(Properties.class, "The unittypename (" + unittypeName + ") could not be found. The quirk " + quirkName + " will return default false");
			return false;
		}
		for (String quirk : getQuirks(unittypeName, version)) {
			if (quirk.equals(quirkName))
				return true;
		}
		return false;
	}

	private static String[] getQuirks(String unittypeName, String version) {
		String quirks = null;
		if (version != null)
			quirks = getString("quirks." + unittypeName + "_" + version, null);
		if (quirks == null)
			quirks = getString("quirks." + unittypeName, null);
		if (quirks == null)
			return new String[0];
		else
			return quirks.split("\\s*,\\s*");

	}

	//	public static boolean isDiscoveryForce() {
	//		return getString("discovery.force", "false").equals("true");
	//	}

	public static boolean isDiscoveryMode() {
		return getString("discovery.mode", "false").equals("true");
	}

	public static String[] getDiscoveryBlocked() {
		String blocked = getString("discovery.block", null);
		if (blocked == null)
			return new String[0];
		else
			return blocked.split("\\s*,\\s*");
	}

	public static boolean isTestMode() {
		return getString("debug.test.mode", "false").equals("true");
	}

	private static int getInteger(String propertyKey, int defaultValue) {
		if (!config.hasPath(propertyKey)) {
			Log.warn(Properties.class, "The value of " + propertyKey + " was not specified, instead using default value " + defaultValue);
			return defaultValue;
		}
		try {
			return config.getInt(propertyKey);
		} catch (Throwable t) {
			Log.warn(Properties.class, "The value of " + propertyKey + " was not a number, instead using default value " + defaultValue, t);
			return defaultValue;
		}
	}

	private static long getLong(String propertyKey, long defaultValue) {
		if (!config.hasPath(propertyKey)) {
			Log.warn(Properties.class, "The value of " + propertyKey + " was not specified, instead using default value " + defaultValue);
			return defaultValue;
		}
		try {
			return config.getLong(propertyKey);
		} catch (Throwable t) {
			Log.warn(Properties.class, "The value of " + propertyKey + " was not a number, instead using default value " + defaultValue, t);
			return defaultValue;
		}
	}

	private static String getString(String propertyKey, String defaultValue) {
		if (!config.hasPath(propertyKey)) {
			Log.warn(Properties.class, "The value of " + propertyKey + " was not specified, instead using default value " + defaultValue);
			return defaultValue;
		}
		return config.getString(propertyKey);
	}

	public static String getAuthMethod() {
		return getString("auth.method", "none");
	}

	public static int getXAPSCacheTimeout() {
		return getInteger("xaps.cache.timeout", 300);
	}
	
	public static boolean isFileAuthUsed() {
	  return getString("file.auth.used", "false").equals("true");
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
