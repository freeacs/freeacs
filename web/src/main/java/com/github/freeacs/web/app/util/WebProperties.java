package com.github.freeacs.web.app.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;


/**
 * Takes care of retrieving properties from a property file.
 * If the same property file is requested the previous WebProperties instance with this propertyfile will be returned.
 * Can easily be extended to control the property values by enforcing strong quality rules.
 * As an example: Like checking the consistency for a database connection url.
 *  
 * @author Jarl Andre Hubenthal
 *
 */
@Component
public class WebProperties {

	public static String KEYSTORE_PASS;
	public static String MONITOR_LOCATION;
	public static String LOCALE;
	public static boolean JAVASCRIPT_DEBUG;
	public static boolean IX_EDIT_ENABLED;
	public static boolean CONFIRM_CHANGES;
	public static String PROPERTIES;
	public static boolean UNIT_CONFIG_AUTOFILTER;
	public static boolean CONFIDENTIALS_RESTRICTED;
	public static boolean GZIP_ENABLED;
	public static boolean DEBUG;
	public static boolean SHOW_VOIP;
	public static boolean SHOW_HARDWARE;
	public static Integer SESSION_TIMEOUT;

	/**
	 * Gets the session timeout.
	 *
	 * @return the session timeout
	 */
	@Value("${session.timeout:30}")
	public void setSessionTimeout(Integer timeout) {
		SESSION_TIMEOUT = timeout;
	}

	/**
	 * Returns an indicator for if hardware syslog should be available
	 * @return
	 */
	@Value("${unit.dash.hardware:false}")
	public void setShowHardware(Boolean showHardware) {
		SHOW_HARDWARE = showHardware;
	}

	/**
	 * Returns an indicator for if voip syslog should be available
	 * @return
	 */
	@Value("${unit.dash.voip:false}")
	public void setShowVoip(Boolean showVoip) {
		SHOW_VOIP = showVoip;
	}

	@Value("${debug:false}")
	public void setDebug(Boolean debug) {
		DEBUG = debug;
	}

	@Value("${gzip.enabled:false}")
	public void setGzipEnabled(Boolean enabled) {
		GZIP_ENABLED = enabled;
	}

	@Value("${confidentials.restricted:false}")
	public void setConfidentialsRestricted(Boolean restricted) {
		CONFIDENTIALS_RESTRICTED = restricted;
	}

	@Value("${unit.config.autofilter:false}")
	public void setUnitConfigAutofilter(Boolean autofilter) {
		UNIT_CONFIG_AUTOFILTER = autofilter;
	}

	@Value("${properties:default}")
	public void setProperties(String properties) {
		PROPERTIES = properties;
	}

	@Value("${confirmchanges:false}")
	public void setConfirmChanges(Boolean confirmChanges) {
		CONFIRM_CHANGES = confirmChanges;
	}

	@Value("${ixedit.enabled:false}")
	public void setIxEditEnabled(Boolean enabled) {
		IX_EDIT_ENABLED = enabled;
	}

	@Value("${javascript.debug:false}")
	public void setJavascriptDebug(Boolean debug) {
		JAVASCRIPT_DEBUG = debug;
	}

	@Value("${locale:#{null}}")
	public void setLocale(String locale) {
		LOCALE = locale;
	}

	@Value("${monitor.location:#{null}}")
	public void setMonitorLocation(String monitorLocation) {
		MONITOR_LOCATION = monitorLocation;
	}

	@Value("${keystore.pass:changeit}")
	public void setKeyStorePass(String keyStorePass) {
		KEYSTORE_PASS = keyStorePass;
	}

	/**
	 * Gives the customization to the dash display as defined in the
	 * web properties file.
	 *
	 * @param unittypeName The unit type name to find settings for
	 * @return List of CustomDashDisplayProperty containing the settings
	 */
	public static Map<String, String> getCustomDash(String unittypeName) {
		// TODO should this be reimplemented?
		return Collections.emptyMap();
	}

}