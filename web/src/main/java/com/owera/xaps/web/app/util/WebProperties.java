package com.owera.xaps.web.app.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import com.owera.common.log.Logger;
import com.owera.common.util.PropertyReader;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;


/**
 * Takes care of retrieving properties from a property file.
 * If the same property file is requested the previous WebProperties instance with this propertyfile will be returned.
 * Can easily be extended to control the property values by enforcing strong quality rules.
 * As an example: Like checking the consistency for a database connection url.
 *  
 * @author Jarl Andre Hubenthal
 *
 */
public class WebProperties {

	/** The pr. */
	private static Config config = ConfigFactory.parseResources("xaps-web.conf");
	
	/** The log. */
	private static Logger log = new Logger();

	/**
	 * Gets the integer.
	 *
	 * @param propertyKey the property key
	 * @param defaultValue the default value
	 * @return the integer
	 */
	public static int getInteger(String propertyKey, int defaultValue) {
		if (!config.hasPath(propertyKey)) {
			return defaultValue;
		}
		try {
			return config.getInt(propertyKey);
		} catch (Exception t) {
			log.debug("The value of " + propertyKey + " was not a number, instead using default value " + defaultValue, t);
			return defaultValue;
		}
	}
	
	/**
	 * Gets the long.
	 *
	 * @param propertyKey the property key
	 * @param defaultValue the default value
	 * @return the long
	 */
	public static long getLong(String propertyKey, long defaultValue) {
		if (!config.hasPath(propertyKey)) {
			return defaultValue;
		}
		try {
			return config.getLong(propertyKey);
		} catch (Throwable t) {
			log.debug("The value of " + propertyKey + " was not a number, instead using default value " + defaultValue, t);
			return defaultValue;
		}
	}

	/**
	 * Gets the string.
	 *
	 * @param propertyKey the property key
	 * @param defaultValue the default value
	 * @return the string
	 */
	public static String getString(String propertyKey, String defaultValue) {
		if (!config.hasPath(propertyKey)) {
			return defaultValue;
		}
		String prop = config.getString(propertyKey);
		if (prop == null) {
			log.debug("The value of " + propertyKey + " was not specified, instead using default value " + defaultValue);
			return defaultValue;
		}
		return prop;
	}
	
	/**
	 * Gets the boolean.
	 *
	 * @param propertyKey the property key
	 * @return the boolean
	 */
	public static boolean getBoolean(String propertyKey) {
		return getBoolean(propertyKey, false);
	}
	
	/**
	 * Gets the boolean.
	 *
	 * @param propertyKey the property key
	 * @return the boolean
	 */
	public static Boolean getBoolean(String propertyKey, Boolean def) {
		if (!config.hasPath(propertyKey)) {
			return def;
		}
		try {
			return config.getBoolean(propertyKey);
		} catch (Throwable t) {
			log.debug("The value of " + propertyKey + " was not a boolean, instead returning false", t);
			return def;
		}
	}

	/**
	 * Gets the session timeout.
	 *
	 * @return the session timeout
	 */
	public static int getSessionTimeout() {
		return getInteger("session.timeout", 30);
	}

	/**
	 * Gets the login auth.
	 *
	 * @return the login auth
	 */
	public static String getLoginAuth() {
		return getString("login.auth", "none");
	}

	/**
	 * Returns an indicator for if hardware syslog should be available
	 * @return
	 */
	public static Boolean getShowHardware() {
		return getBoolean("unit.dash.hardware", false);
	}

	/**
	 * Returns an indicator for if voip syslog should be available
	 * @return
	 */
	public static Boolean getShowVoip() {
		return getBoolean("unit.dash.voip", false);
	}

	/**
	 * Gives the customization to the dash display as defined in the
	 * web properties file.
	 *
	 * @param unittypeName The unit type name to find settings for
	 * @return List of CustomDashDisplayProperty containing the settings
	 */
	public static Map<String, String> getCustomDash(String unittypeName) {
		String regex = "^custom\\.dash\\.\\*.*";
		if (unittypeName != null && !unittypeName.isEmpty())
			regex += "|^custom\\.dash\\." + unittypeName + ".*";

		Map<String, String> configDisplay = new LinkedHashMap<String, String>();

		for (String key : getFilteredKeys(regex)) {
			String[] parts = getString(key, null).split("\\;");
			if (parts.length > 1)
				configDisplay.put(parts[0].trim(), parts[1].trim());
			else
				configDisplay.put(parts[0].trim(), null);
		}

		return configDisplay;
	}

	/**
	 * Gets the keys in the web properties that matches a certain
	 * regular expression.
	 *
	 * @param regex
	 * @return A list containing the resulting keys
	 */
	private static List<String> getFilteredKeys(String regex) {
		List<String> keys = new LinkedList<String>();

		Pattern pattern = Pattern.compile(regex);

		for (String key : config.root().unwrapped().keySet())
			if (pattern.matcher(key).matches())
				keys.add(key);

		Collections.sort(keys);
		return keys;
	}

}