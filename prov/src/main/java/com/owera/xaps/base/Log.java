package com.owera.xaps.base;

import java.util.HashMap;
import java.util.Map;

import com.owera.common.log.Logger;
import com.owera.xaps.dbi.UnitParameter;
import com.owera.xaps.dbi.util.SystemParameters;

/**
 * This class handles all the logging in this server.
 */
@SuppressWarnings("rawtypes")
public class Log {

	private static Logger eventLog = new Logger("Event");

	private static Logger convLog = new Logger("Conversation");

	private static Logger debugLog = new Logger("Debug");

	private static Map<Class, Logger> loggers = new HashMap<Class, Logger>();

	public static void debug(Class c, String message) {
		log(c, com.owera.common.log.Log.DEBUG_INT, message, null);
	}

	public static void info(Class c, String message) {
		log(c, com.owera.common.log.Log.INFO_INT, message, null);
	}

	public static void notice(Class c, String message) {
		log(c, com.owera.common.log.Log.NOTICE_INT, message, null);
	}

	public static void warn(Class c, String message) {
		log(c, com.owera.common.log.Log.WARN_INT, message, null);
	}

	public static void warn(Class c, String message, Throwable t) {
		log(c, com.owera.common.log.Log.WARN_INT, message, t);
	}

	public static void error(Class c, String message) {
		log(c, com.owera.common.log.Log.ERROR_INT, message, null);
	}

	public static void error(Class c, String message, Throwable t) {
		log(c, com.owera.common.log.Log.ERROR_INT, message, t);
	}

	public static void fatal(Class c, String message) {
		log(c, com.owera.common.log.Log.FATAL_INT, message, null);
	}

	public static void fatal(Class c, String message, Throwable t) {
		log(c, com.owera.common.log.Log.FATAL_INT, message, t);
	}

	public static void conversation(SessionDataI sessionData, String message) {
		convLog.info(message);
		if (sessionData == null || sessionData.getUnit() == null || sessionData.getUnit().getUnitParameters() == null)
			return;
		UnitParameter debugUp = sessionData.getUnit().getUnitParameters().get(SystemParameters.DEBUG);
		if (debugUp != null && "1".equals(debugUp.getValue()))
			debugLog.info(message);
	}

	public static boolean isConversationLogEnabled() {
		if (convLog.isDebugEnabled() || convLog.isInfoEnabled())
			return true;
		return false;
	}

	public static void event(SessionDataI sessionData, String message) {
		eventLog.info(message);
	}

	private static void log(Class loggerId, int severity, String message, Throwable throwable) {
		if (loggers != null) {
			Logger logger = loggers.get(loggerId);
			if (logger == null) {
				logger = new Logger(loggerId);
				loggers.put(loggerId, logger);
			}
			//			String unitId = Context.get(Context.X);
			//			Unit unit = null;
			//			SessionDataI sessionData = BaseCache.getSessionDataSilent(unitId);
			//			if (sessionData != null)
			//				unit = sessionData.getUnit();

			logger.log(severity, message, throwable);
			//			if (unit != null && unit.getUnitParameters() != null) {
			//				UnitParameter debugUp = unit.getUnitParameters().get(SystemParameters.DEBUG);
			//				if (debugUp != null && "1".equals(debugUp.getValue())) {
			//					debugLog.log(severity, message);
			//				}
			//			}
		}
	}

}
