package com.owera.xaps.core;

import java.util.HashMap;
import java.util.Map;

import com.owera.common.log.Logger;

/**
 * This class handles all the logging in this server.
 */
public class Log {

	private static Map<String, Logger> loggers = new HashMap<String, Logger>();

	//	private static double GIGA = 1024 * 1024 * 1024;
	//	private static double MEGA = 1024 * 1024;
	//	private static double KILO = 1024;

	//	public static void log(Class c, Level level, String message, Throwable throwable) {
	//		log(c.getName(), level, message, throwable);
	//	}
	//
	//	public static void log(String loggerId, Level level, String message) {
	//		log(loggerId, level, message, null);
	//	}
	//
	//	public static void log(Class c, Level level, String message) {
	//		log(c.getName(), level, message, null);
	//	}

	public static void log(String loggerId, int logLevel, String message, Throwable throwable) {

		//		message = "[" + getMemConsumption() + "] " + message;
		Logger logger = loggers.get(loggerId);
		if (logger == null) {
			logger = new Logger(loggerId);
			loggers.put(loggerId, logger);
		}
		logger.log(logLevel, message, throwable);

	}

	//	private static String getMemConsumption() {
	//		Runtime runtime = Runtime.getRuntime();
	//		long free = runtime.freeMemory();
	//		long total = runtime.totalMemory();
	//		double used = total - free;
	//		String usedStr = null;
	//		if (used > GIGA)
	//			usedStr = String.format("%6.1f GB", (used / GIGA));
	//		else if (used > MEGA)
	//			usedStr = String.format("%6.1f MB", (used / MEGA));
	//		else
	//			usedStr = String.format("%6.1f KB", (used / KILO));
	//		return usedStr;
	//	}
}
