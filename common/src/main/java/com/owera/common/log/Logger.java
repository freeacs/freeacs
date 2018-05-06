package com.owera.common.log;

import java.util.List;

public class Logger {
	private String logName;
	private String simpleClassName;

	public Logger() {
		StackTraceElement ste = new Exception().getStackTrace()[1];
		logName = ste.getClassName();
		this.simpleClassName = getSimpleClassName(logName);
	}

	public Logger(String logName) {
		StackTraceElement ste = new Exception().getStackTrace()[1];
		this.logName = logName;
		this.simpleClassName = getSimpleClassName(ste.getClassName());
	}

	@SuppressWarnings("rawtypes")
	public Logger(Class clazz) {
		this.logName = clazz.getName();
		this.simpleClassName = getSimpleClassName(logName);
	}

	private String getSimpleClassName(String logName) {
		if (logName.indexOf(".") > -1)
			return logName.substring(logName.lastIndexOf(".") + 1);
		return logName;
	}

	public void log(int logLevel, String message, Throwable t) {
		LogObject lo = new LogObject();
		lo.setLogName(logName);
		lo.setSimpleClassName(simpleClassName);
		lo.setSeverity(logLevel);
		lo.setCoreMessage(message);
		lo.setThrowable(t);
		Log.log(lo);
	}

	public void log(int logLevel, String message) {
		log(logLevel, message, null);
	}

	public void debug(String message) {
		log(Log.DEBUG_INT, message);
	}

	public void debug(String message, Throwable t) {
		log(Log.DEBUG_INT, message, t);
	}

	public void info(String message) {
		log(Log.INFO_INT, message);
	}

	public void info(String message, Throwable t) {
		log(Log.INFO_INT, message, t);
	}

	public void notice(String message) {
		log(Log.NOTICE_INT, message);
	}

	public void notice(String message, Throwable t) {
		log(Log.NOTICE_INT, message, t);
	}

	public void warn(String message) {
		log(Log.WARN_INT, message);
	}

	public void warn(String message, Throwable t) {
		log(Log.WARN_INT, message, t);
	}

	public void error(String message) {
		log(Log.ERROR_INT, message);
	}

	public void error(String message, Throwable t) {
		log(Log.ERROR_INT, message, t);
	}

	public void critic(String message) {
		log(Log.CRITIC_INT, message);
	}

	public void critic(String message, Throwable t) {
		log(Log.CRITIC_INT, message, t);
	}

	public void fatal(String message) {
		log(Log.FATAL_INT, message);
	}

	public void fatal(String message, Throwable t) {
		log(Log.FATAL_INT, message, t);
	}

	public void alert(String message) {
		log(Log.ALERT_INT, message);
	}

	public void alert(String message, Throwable t) {
		log(Log.ALERT_INT, message, t);
	}

	public void emerg(String message) {
		log(Log.EMERG_INT, message);
	}

	public void emerg(String message, Throwable t) {
		log(Log.EMERG_INT, message, t);
	}

	public boolean isDebugEnabled() {
		if (Log.getLogLevel(logName) == Log.DEBUG_INT)
			return true;
		return false;
	}

	public boolean isInfoEnabled() {
		if (Log.getLogLevel(logName) <= Log.INFO_INT)
			return true;
		return false;
	}

	public List<Appender> getAppenders() {
		return Log.getAppenders(logName);
	}

}
