package com.github.freeacs.base;

import com.github.freeacs.dbi.UnitParameter;
import com.github.freeacs.dbi.util.SystemParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * This class handles all the logging in this server.
 */
@SuppressWarnings("rawtypes")
public class Log {

    private static Logger eventLog = LoggerFactory.getLogger("Event");
    private static Logger convLog = LoggerFactory.getLogger("Conversation");
    private static Logger debugLog = LoggerFactory.getLogger("Debug");

	private static Map<Class, Logger> loggers = new HashMap<Class, Logger>();

	public static void debug(Class c, String message) {
		getLogger(c).debug(message);
	}

	public static void info(Class c, String message) {
        getLogger(c).info(message);
	}

	public static void notice(Class c, String message) {
        getLogger(c).info(message);
	}

	public static void warn(Class c, String message) {
        getLogger(c).warn(message);
	}

	public static void warn(Class c, String message, Throwable t) {
        getLogger(c).warn(message, t);
	}

	public static void error(Class c, String message) {
        getLogger(c).error(message);
	}

	public static void error(Class c, String message, Throwable t) {
        getLogger(c).error(message, t);
	}

	public static void fatal(Class c, String message) {
        getLogger(c).error(message);
	}

	public static void fatal(Class c, String message, Throwable t) {
        getLogger(c).error(message, t);
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

	public static void event(String message) {
		eventLog.info(message);
	}

	private static Logger getLogger(Class loggerId) {
        Logger logger = loggers.get(loggerId);
        if (logger == null) {
            logger = LoggerFactory.getLogger(loggerId);
            loggers.put(loggerId, logger);
        }
        return loggers.get(loggerId);
	}

}
