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
	private PropertyReader pr;
	
	/** The log. */
	private static Logger log = new Logger();

	/** The map. */
	private static Map<String, WebProperties> map = new HashMap<String, WebProperties>();

	/**
	 * Instantiates a new web properties.
	 */
	private WebProperties(){
		
	}
	
	/**
	 * Instantiates a new web properties.
	 *
	 * @param file the file
	 */
	private WebProperties(String file) {
		pr = new PropertyReader(file);
		map.put(file, this);
	}

	/**
	 * Gets the reader.
	 *
	 * @param file the file
	 * @return the reader
	 */
	public static WebProperties getReader(String file) {
		WebProperties prop = map.get(file);
		if (prop != null)
			return prop;
		return new WebProperties(file);
	}
	
	/**
	 * Gets the web properties.
	 *
	 * @return the web properties
	 */
	public static WebProperties getWebProperties() {
		WebProperties prop = map.get("xaps-web"+SessionCache.CONTEXT_PATH+".properties");
		if (prop != null)
			return prop;
		return new WebProperties("xaps-web"+SessionCache.CONTEXT_PATH+".properties");
	}
	
	/**
	 * Gets the log properties.
	 *
	 * @return the log properties
	 */
	public static WebProperties getLogProperties() {
		WebProperties prop = map.get("xaps-web"+SessionCache.CONTEXT_PATH+"-logs.properties");
		if (prop != null)
			return prop;
		return new WebProperties("xaps-web"+SessionCache.CONTEXT_PATH+"-logs.properties");
	}
	
	/**
	 * Gets the ldap properties.
	 *
	 * @return the ldap properties
	 */
	public static WebProperties getLdapProperties() {
		WebProperties prop = map.get("xaps-web"+SessionCache.CONTEXT_PATH+"-ldap.properties");
		if (prop != null)
			return prop;
		return new WebProperties("xaps-web"+SessionCache.CONTEXT_PATH+"-ldap.properties");
	}
	
	/**
	 * Gets the roles properties.
	 *
	 * @return the roles properties
	 */
	public static WebProperties getRolesProperties() {
		WebProperties prop = map.get("xaps-web"+SessionCache.CONTEXT_PATH+"-roles.properties");
		if (prop != null)
			return prop;
		return new WebProperties("xaps-web"+SessionCache.CONTEXT_PATH+"-roles.properties");
	}

	/**
	 * Gets the property.
	 *
	 * @param customProperty the custom property
	 * @return the property
	 */
	public String getProperty(String customProperty) {
		return pr.getProperty(customProperty);
	}

	/**
	 * Gets the integer.
	 *
	 * @param propertyKey the property key
	 * @param defaultValue the default value
	 * @return the integer
	 */
	public int getInteger(String propertyKey, int defaultValue) {
		String prop = getProperty(propertyKey);
		if(prop==null)
			return defaultValue;
		try {
			return Integer.parseInt(prop);
		} catch (NumberFormatException t) {
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
	public long getLong(String propertyKey, long defaultValue) {
		String prop = getProperty(propertyKey);
		if(prop==null)
			return defaultValue;
		try {
			return Long.parseLong(prop);
		} catch (NumberFormatException t) {
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
	public String getString(String propertyKey, String defaultValue) {
		String prop = getProperty(propertyKey);
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
	public boolean getBoolean(String propertyKey) {
		return getBoolean(propertyKey, false);
	}
	
	/**
	 * Gets the boolean.
	 *
	 * @param propertyKey the property key
	 * @return the boolean
	 */
	public Boolean getBoolean(String propertyKey, Boolean def) {
		String prop = getProperty(propertyKey);
		if(prop==null)
			return def;
		try {
			return Boolean.parseBoolean(prop);
		} catch (NumberFormatException t) {
			log.debug("The value of " + propertyKey + " was not a boolean, instead returning false", t);
			return def;
		}
	}
	
	/**
	 * Gets the properties.
	 *
	 * @return the properties
	 */
	public Set<Entry<String, Object>> getProperties(){
		return pr.getPropertyMap().entrySet();
	}

	/**
	 * Gets the session timeout.
	 *
	 * @return the session timeout
	 */
	public int getSessionTimeout() {
		return getInteger("session.timeout", 30);
	}

	/**
	 * Gets the login auth.
	 *
	 * @return the login auth
	 */
	public String getLoginAuth() {
		return getString("login.auth", "none");
	}
	
	/**
	 * Returns an indicator for if hardware syslog should be available
	 * @return
	 */
	public Boolean getShowHardware() {
		return getBoolean("unit.dash.hardware", false);
	}
	
	/**
	 * Returns an indicator for if voip syslog should be available
	 * @return
	 */
	public Boolean getShowVoip() {
		return getBoolean("unit.dash.voip", false);
	}
	
	/**
	 * Gives the customization to the dash display as defined in the
	 * web properties file.
	 * 
	 * @param unittypeName The unit type name to find settings for
	 * @return List of CustomDashDisplayProperty containing the settings
	 */
	public Map<String, String> getCustomDash(String unittypeName) {
		String regex = "^custom\\.dash\\.\\*.*";	
		if (unittypeName != null && !unittypeName.isEmpty())
			regex += "|^custom\\.dash\\." + unittypeName + ".*";
		
		Map<String, String> configDisplay = new LinkedHashMap<String, String>();
		
		for (String key : getFilteredKeys(regex)) {
			String[] parts = getProperty(key).split("\\;");
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
	private List<String> getFilteredKeys(String regex) {
		List<String> keys = new LinkedList<String>();
		
		Pattern pattern = Pattern.compile(regex);
		
		for (String key : pr.getPropertyMap().keySet())
			if (pattern.matcher(key).matches())
				keys.add(key);
		
		Collections.sort(keys);
		return keys;
	}
}