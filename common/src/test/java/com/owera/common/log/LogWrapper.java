package com.owera.common.log;

import java.util.HashMap;
import java.util.Map;

/**
 * This class handles all the logging in this server.
 */
@SuppressWarnings("rawtypes")
public class LogWrapper {

	private static Map<Class, Logger> loggers = new HashMap<Class, Logger>();

	public static void debug(Class c, String message) {
		log(c, Log.DEBUG_INT, message, null);
	}

	public static void info(Class c, String message) {
		log(c, Log.INFO_INT, message, null);
	}

	public static void notice(Class c, String message) {
		log(c, Log.NOTICE_INT, message, null);
	}

	public static void warn(Class c, String message) {
		log(c, Log.WARN_INT, message, null);
	}

	public static void warn(Class c, String message, Throwable t) {
		log(c, Log.WARN_INT, message, t);
	}

	public static void error(Class c, String message) {
		log(c, Log.ERROR_INT, message, null);
	}

	public static void error(Class c, String message, Throwable t) {
		log(c, Log.ERROR_INT, message, t);
	}

	public static void fatal(Class c, String message) {
		log(c, Log.FATAL_INT, message, null);
	}

	public static void fatal(Class c, String message, Throwable t) {
		log(c, Log.FATAL_INT, message, t);
	}

	private static void log(Class loggerId, int severity, String message, Throwable throwable) {
		if (loggers != null) {
			Logger logger = loggers.get(loggerId);
			if (logger == null) {
				logger = new Logger(loggerId);
				loggers.put(loggerId, logger);
			}
			logger.log(severity, message, throwable);
		}
	}

}
