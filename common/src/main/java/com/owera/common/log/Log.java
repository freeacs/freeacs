package com.owera.common.log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.owera.common.util.PropertyReader;
import com.owera.common.util.PropertyReaderException;

/*
 * Requirements
 * 
 * 1. Log to console, file and database
 * 2. Log-output format can be specified (not as rich as log4j)
 * 3. Log-rotation can be specified
 * 4. Severity levels align with syslog levels
 * 		- debug, info, notice, warn, error, crit/fatal, alert, emerg
 * 5. Log will find necessary propertyfiles in classpath
 * 6. Log hierarchy will be the same as in log4j
 * 7. Should be possible to integrate with log4j  
 */

public class Log {

	private final static String DEBUG_STR = "DEBUG ";
	private final static String INFO_STR = "INFO  ";
	private final static String NOTICE_STR = "NOTICE";
	private final static String WARN_STR = "WARN  ";
	private final static String ERROR_STR = "ERROR ";
	private final static String CRITIC_STR = "CRITIC";
	private final static String FATAL_STR = "FATAL ";
	private final static String ALERT_STR = "ALERT ";
	private final static String EMERG_STR = "EMERG ";

	public final static int DEBUG_INT = 7;
	public final static int INFO_INT = 6;
	public final static int NOTICE_INT = 5;
	public final static int WARN_INT = 4;
	public final static int ERROR_INT = 3;
	public final static int CRITIC_INT = 2;
	public final static int FATAL_INT = 2;
	public final static int ALERT_INT = 1;
	public final static int EMERG_INT = 0;

	public final static String[] PADS = new String[] { " ", "  ", "   ", "    ", "     ", "      ", "       ", "        ", "         ", "          " };

	private final static Map<String, Integer> severityStr2IntMap = new HashMap<String, Integer>();
	private final static Map<Integer, String> severityInt2StrMap = new HashMap<Integer, String>();

	static {
		severityStr2IntMap.put(DEBUG_STR, DEBUG_INT);
		severityStr2IntMap.put(INFO_STR, INFO_INT);
		severityStr2IntMap.put(NOTICE_STR, NOTICE_INT);
		severityStr2IntMap.put(WARN_STR, WARN_INT);
		severityStr2IntMap.put(ERROR_STR, ERROR_INT);
		severityStr2IntMap.put(CRITIC_STR, CRITIC_INT);
		severityStr2IntMap.put(FATAL_STR, FATAL_INT);
		severityStr2IntMap.put(ALERT_STR, ALERT_INT);
		severityStr2IntMap.put(EMERG_STR, EMERG_INT);
		severityInt2StrMap.put(DEBUG_INT, DEBUG_STR);
		severityInt2StrMap.put(INFO_INT, INFO_STR);
		severityInt2StrMap.put(NOTICE_INT, NOTICE_STR);
		severityInt2StrMap.put(WARN_INT, WARN_STR);
		severityInt2StrMap.put(ERROR_INT, ERROR_STR);
		severityInt2StrMap.put(CRITIC_INT, FATAL_STR);
		severityInt2StrMap.put(FATAL_INT, FATAL_STR);
		severityInt2StrMap.put(ALERT_INT, ALERT_STR);
		severityInt2StrMap.put(EMERG_INT, EMERG_STR);
	}

	private static PropertyReader pr;

	private static Map<String, Configuration> configurationMap = new HashMap<String, Configuration>();
	private static Map<String, Appender> appenderMap = new Hashtable<String, Appender>();
	protected static String DEFAULT_APPENDER_NAME = "DEFAULT-APPENDER";
	private static int DEFAULT_APPENDER_LOGLEVEL = DEBUG_INT;
	private static boolean cheapInit = false;
	private static long lastSecond = 0;
	private static Calendar cal = Calendar.getInstance();
	private static String thisSecondStr;
	private static Object syncMonitor = new Object();

	private static Appender getDefaultAppender() {
		Appender appender = new ConsoleAppender();
		appender.setAppenderName(DEFAULT_APPENDER_NAME);
		appender.setPattern("%d %p %m%n");
		appender.preparePatternArray();
		return appender;
	}

	@SuppressWarnings("rawtypes")
	public static synchronized Appender getAppender(String appenderName) {
		Appender appender = appenderMap.get(appenderName);
		if (appenderName.equals(DEFAULT_APPENDER_NAME)) {
			appender = getDefaultAppender();
		} else if (appender == null) {
			Class appenderClass = null;
			String appenderClassName = pr.getProperty(appenderName + ".class");
			if (appenderClassName == null) {
				appenderClass = ConsoleAppender.class;
				ConfigErrors.add("Appender class name for appender " + appenderName + " was not defined (expected " + appenderName + ".class=<classname>");
			} else {
				try {
					appenderClass = Class.forName(appenderClassName);
				} catch (ClassNotFoundException e) {
					appenderClass = ConsoleAppender.class;
					ConfigErrors.add("Appender class " + appenderClassName + " was not found.");
				}
			}
			try {
				appender = (Appender) appenderClass.newInstance();
			} catch (Throwable t) {
				appender = getDefaultAppender();
				ConfigErrors.add("Could not instantiate " + appenderClassName + ", possibly missing a public constructor like this: " + appenderClassName + "()");
			}
			String pattern = pr.getProperty(appenderName + ".pattern");
			if (pattern == null) {
				appender.setPattern("%m");
			} else
				appender.setPattern(pattern);
			appender.setPropertyReader(pr);
			appender.setAppenderName(appenderName);
			appender.constructor();
			appender.preparePatternArray();
			appenderMap.put(appenderName, appender);
		} else {
			String pattern = pr.getProperty(appenderName + ".pattern");
			if (pattern == null) {
				appender.setPattern("%m");
			} else
				appender.setPattern(pattern);
			appender.constructor();
			appender.preparePatternArray();
		}
		return appender;
	}

	private static Map<String, Configuration> populateConfigurations() {
		Map<String, Configuration> configMap = new HashMap<String, Configuration>();
		Map<String, Object> propertyMap = pr.getPropertyMap();
		boolean foundAppender = false;
		for (Entry<String, Object> entry : propertyMap.entrySet()) {
			if (entry.getKey().startsWith("log.")) {
				foundAppender = true;
				String logName = entry.getKey().substring(entry.getKey().indexOf(".") + 1);
				Integer severity = null;
				String[] values = ((String) entry.getValue()).split(",");
				if (values == null || values.length < 2 || values[0].length() < 2) {
					values = new String[] { "warn", "DEFAULT-APPENDER" };
					ConfigErrors.add("The log." + logName + " property expects minimum two values, separated with comma. The first value must be min. 2 char long");
				}
				String severityStr = values[0].trim().toUpperCase().substring(0, 2);
				for (Entry<String, Integer> entry2 : severityStr2IntMap.entrySet()) {
					if (entry2.getKey().startsWith(severityStr)) {
						severity = entry2.getValue();
						break;
					}
				}
				if (severity == null) {
					severity = WARN_INT;
					ConfigErrors.add("The log." + logName + " property defines severity-/loglevel to be " + severityStr + ", which is not recognized - using warn instead");
				}
				List<Appender> appenders = new ArrayList<Appender>();
				for (int i = 1; i < values.length; i++) {
					String appenderName = values[i].trim();
					Appender appender = getAppender(appenderName);
					appenders.add(appender);
				}
				Configuration configuration = new Configuration(logName, appenders, severity);
				configMap.put(logName, configuration);
			} else if (foundAppender)
				break; // the propertyMap is alphabetically ordered 
		}
		ConfigErrors.log();
		return configMap;
	}

	/*
	 * This method initializes the log framework properly. Using this
	 * method you can customize all parts of the logging using a 
	 * propertyfile.
	 */
	public static void initialize(String propertyfile) {
		try {
			pr = new PropertyReader(propertyfile);
			synchronized (syncMonitor) {
				configurationMap = populateConfigurations();
			}
		} catch (PropertyReaderException pre) {
			ConfigErrors.add("com.owera.common.log.Log.initialize(): " + pre.getMessage() + ", continue with default config");
		}
	}

	/*
	 * This is the cheap way of initializing the log framework. Using
	 * this method you will be able to control the overall loglevel, but
	 * everything else is not customizable. Thus everything will be logged
	 * to stdout, that is at the correct loglevel. 
	 */
	public static void initialize(int logLevel) {
		cheapInit = true;
		DEFAULT_APPENDER_LOGLEVEL = logLevel;
	}

	private static void pad(StringBuilder sb, String appendStr, int size) {
		sb.append(appendStr);
		int padsToGo = size - appendStr.length();
		if (padsToGo > 0) {
			while (padsToGo > 10) {
				sb.append(PADS[9]);
				padsToGo -= 10;
			}
			sb.append(PADS[padsToGo - 1]);
		}
	}

	protected static void makeCompleteMessage(Appender appender, LogObject lo) {
		StringBuilder message = new StringBuilder();
		for (String messagePart : appender.getPatternArray()) {
			if (messagePart == null)
				break;
			if (messagePart.charAt(0) == '%') {
				if (messagePart.charAt(1) == 'd')
					message.append(lo.getTimestampStr());
				else if (messagePart.charAt(1) == 'm')
					message.append(lo.getCoreMessage());
				else if (messagePart.charAt(1) == 'n')
					message.append("\n");
				else if (messagePart.charAt(1) == 'p')
					message.append(severityInt2StrMap.get(lo.getSeverity()));
				else if (messagePart.charAt(1) == 'c') {
					pad(message, lo.getSimpleClassName(), 25);
				} else if (messagePart.charAt(1) == 'x') {
					String context = Context.get(Context.X);
					if (context != null)
						pad(message, context, 35);
				}
			} else
				message.append(messagePart);
		}
		if (lo.getThrowable() != null) {
			message.append(lo.getThrowable().getClass());
			message.append(":");
			message.append(lo.getThrowable().getMessage());
			message.append("\n");
			StackTraceElement[] steArr = lo.getThrowable().getStackTrace();
			for (StackTraceElement ste : steArr) {
				message.append("\t");
				message.append(ste.toString());
				message.append("\n");
			}
		}
		lo.setCompleteMessage(message.toString());
	}

	private static void normalize(StringBuilder sb, int number, String trailingChar) {
		if (number < 10)
			sb.append("0");
		sb.append(number);
		sb.append(trailingChar);
	}

	protected static void computeDate(LogObject lo) {
		long now = System.currentTimeMillis();
		long currentSecond = now / 1000;
		if (currentSecond != lastSecond) {
			StringBuilder currentSecondStr = new StringBuilder();
			cal.setTimeInMillis(now);
			normalize(currentSecondStr, cal.get(Calendar.YEAR), "-");
			normalize(currentSecondStr, cal.get(Calendar.MONTH) + 1, "-");
			normalize(currentSecondStr, cal.get(Calendar.DAY_OF_MONTH), " ");
			normalize(currentSecondStr, cal.get(Calendar.HOUR_OF_DAY), ":");
			normalize(currentSecondStr, cal.get(Calendar.MINUTE), ":");
			normalize(currentSecondStr, cal.get(Calendar.SECOND), ".");
			thisSecondStr = currentSecondStr.toString();
			lastSecond = currentSecond;
		}
		lo.setTms(now);
		lo.setCurrentSecond(currentSecond);
		String millis = "" + now % 1000;
		while (millis.length() < 3)
			millis = "0" + millis;
		lo.setTimestampStr(thisSecondStr + millis);
		//		lo.setTimestampStr("1001-01-01 23:23:23.949");
	}

	public static long getCurrentSecond() {
		return System.currentTimeMillis() / 1000;
	}

	private static Configuration getConfiguration(String logName) {
		if (getCurrentSecond() != lastSecond) {
			synchronized (syncMonitor) {
				if (pr != null && pr.isRefreshRequired()) {
					PropertyReader.refreshCache(pr.getPropertyfile());
					configurationMap = populateConfigurations();
				}
			}
		}
		Configuration config = null;
		synchronized (syncMonitor) {
			config = configurationMap.get(logName);
		}
		if (config == null) {
			String modifiedLogName = logName;
			while (config == null && modifiedLogName.indexOf(".") > -1) {
				modifiedLogName = modifiedLogName.substring(0, modifiedLogName.lastIndexOf("."));
				synchronized (syncMonitor) {
					config = configurationMap.get(modifiedLogName);
					configurationMap.put(logName, config);
				}
			}
		}
		if (config == null) {
			List<Appender> appenders = new ArrayList<Appender>();
			appenders.add(getAppender(DEFAULT_APPENDER_NAME));
			config = new Configuration(logName, appenders, DEFAULT_APPENDER_LOGLEVEL);
			synchronized (syncMonitor) {
				configurationMap.put(logName, config);
			}
			if (!cheapInit) {
				if (pr == null)
					ConfigErrors.add("NB! NB! No propertyfile found or Log.initialize() not run - using default configuration");
				ConfigErrors.add("NB! NB! No appender defined for logger " + logName + ", using default appender which logs to System.out on DEBUG level");
			}
		}
		return config;
	}

	protected static List<Appender> getAppenders(String logName) {
		Configuration config = getConfiguration(logName);
		return config.getAppenders();
	}

	protected static int getLogLevel(String logName) {
		Configuration config = getConfiguration(logName);
		return config.getSeverityLevel();
	}

	public static void log(LogObject lo) {
		String logName = lo.getLogName();
		Configuration config = getConfiguration(logName);
		if (lo.getSeverity() <= config.getSeverityLevel()) {
			computeDate(lo);
			List<Appender> appenders = config.getAppenders();
			for (Appender appender : appenders) {
				makeCompleteMessage(appender, lo);
				try {
					appender.log(lo);
				} catch (Exception e) {
					ConfigErrors.add("Exception ocurred in " + appender.getAppenderName() + "s log-method:" + e);
				}
			}
		}
		if (ConfigErrors.size() > 0)
			ConfigErrors.log();
	}
}