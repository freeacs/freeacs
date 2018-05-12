package com.github.freeacs.shell;

import com.github.freeacs.common.util.PropertyReader;
import com.github.freeacs.common.util.PropertyReaderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Properties {

	private static String propertyfile = "xaps-shell.properties";

	private static PropertyReader pr;
	static {
		try {
			pr = new PropertyReader(propertyfile);
		} catch (PropertyReaderException pre) {
			// If propertyfile is not present, use deault settings. Important for shell-as-deamon
		}
	}

	private static Logger logger = LoggerFactory.getLogger(Properties.class);

	@SuppressWarnings("unused")
	private static int getInteger(String propertyKey, int defaultValue) {
		try {
			String prop = pr.getProperty(propertyKey);
			return Integer.parseInt(prop);
		} catch (Throwable t) {
			logger.warn("The value of " + propertyKey + " was not a number, instead using default value " + defaultValue);
			return defaultValue;
		}
	}

	private static String getString(String propertyKey, String defaultValue) {
		try {
			String prop = pr.getProperty(propertyKey);
			if (prop == null) {
				logger.warn("The value of " + propertyKey + " was not specified, instead using default value " + defaultValue);
				return defaultValue;
			}
			return prop;
		} catch (Throwable t) {
			return defaultValue;
		}
	}

	public static boolean isRestricted() {
		return getString("restricted", "false").equals("true");
	}
	
	public static String getSyslogServer() {
		String ss = getString("syslog.server", "localhost:9116");
		int colonPos = ss.indexOf(":");
		if (colonPos == -1)
			return "localhost";
		else
			return ss.substring(0, colonPos);
	}

	public static int getSyslogServerPort() {
		String ss = getString("syslog.server", "localhost:9116");
		int colonPos = ss.indexOf(":");
		if (colonPos == -1)
			return 9116;
		else
			try {
				return Integer.parseInt(ss.substring(colonPos + 1));
			} catch (NumberFormatException nfe) {
				return 9116;
			}
	}


}
