package com.github.freeacs.dbi;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class SyslogConstants {
	public final static int SEVERITY_EMERGENCY = 0;
	public final static int SEVERITY_ALERT = 1;
	public final static int SEVERITY_CRITICAL = 2;
	public final static int SEVERITY_ERROR = 3;
	public final static int SEVERITY_WARNING = 4;
	public final static int SEVERITY_NOTICE = 5;
	public final static int SEVERITY_INFO = 6;
	public final static int SEVERITY_DEBUG = 7;

	public final static int FACILITY_SYSLOGCLIENT_TEST = 23;
	public final static int FACILITY_SHELL = 50;
	public final static int FACILITY_WEB = 51;
	public final static int FACILITY_WEBSERVICE = 52;
	public final static int FACILITY_CORE = 53;
	public final static int FACILITY_TR069 = 54;
	public final static int FACILITY_OPP = 55;
	public final static int FACILITY_SYSLOG_REPORTER = 56;
	public final static int FACILITY_STUN = 57;
	public final static int FACILITY_SPP = 58;
	public final static int FACILITY_TFTP = 59;
	public final static int FACILITY_MONITOR = 60;
	public final static int FACILITY_SHELL_TEST = 150;
	public final static int FACILITY_WEB_TEST = 151;
	public final static int FACILITY_WEBSERVICE_TEST = 152;
	public final static int FACILITY_CORE_TEST = 153;
	public final static int FACILITY_TR069_TEST = 154;
	public final static int FACILITY_OPP_TEST = 155;
	public final static int FACILITY_STUN_TEST = 157;
	public final static int FACILITY_SPP_TEST = 158;
	public final static int FACILITY_TFTP_TEST = 159;
	public final static int FACILITY_MONITOR_TEST = 160;
	public final static int FACILITY_DBLIB_TEST = 200; // has no production counter part

	public static final int EVENT_DEFAULT = 0;

	public static Map<Integer, String> eventMap = new HashMap<Integer, String>();
	static {
		eventMap.put(EVENT_DEFAULT, "Default");
	}

	public static Map<Integer, String> severityMap = new TreeMap<Integer, String>();
	static {
		severityMap.put(SEVERITY_EMERGENCY, "Emergency");
		severityMap.put(SEVERITY_ALERT, "Alert");
		severityMap.put(SEVERITY_CRITICAL, "Critical");
		severityMap.put(SEVERITY_ERROR, "Error");
		severityMap.put(SEVERITY_WARNING, "Warning");
		severityMap.put(SEVERITY_NOTICE, "Notice");
		severityMap.put(SEVERITY_INFO, "Info");
		severityMap.put(SEVERITY_DEBUG, "Debug");
	}

	public static Map<String, Integer> severityStrMap = new TreeMap<String, Integer>();
	static {
		severityStrMap.put("Emergency", SEVERITY_EMERGENCY);
		severityStrMap.put("Alert", SEVERITY_ALERT);
		severityStrMap.put("Critical", SEVERITY_CRITICAL);
		severityStrMap.put("Error", SEVERITY_ERROR);
		severityStrMap.put("Warning", SEVERITY_WARNING);
		severityStrMap.put("Notice", SEVERITY_NOTICE);
		severityStrMap.put("Info", SEVERITY_INFO);
		severityStrMap.put("Debug", SEVERITY_DEBUG);
	}

	public static Map<Integer, String> facilityMap = new TreeMap<Integer, String>();
	static {
		facilityMap.put(0, "Device-Kernel");
		facilityMap.put(1, "Device-User");
		facilityMap.put(2, "Device-Mail");
		facilityMap.put(3, "Device-System");
		facilityMap.put(4, "Device-Sec/Auth");
		facilityMap.put(5, "Device-Syslog Server Intern");
		facilityMap.put(6, "Device-Printer");
		facilityMap.put(7, "Device-News");
		facilityMap.put(8, "Device-UUCP");
		facilityMap.put(9, "Device-Clock");
		facilityMap.put(10, "Device-Sec/Auth");
		facilityMap.put(11, "Device-FTP");
		facilityMap.put(12, "Device-NTP");
		facilityMap.put(13, "Device-Log Audit");
		facilityMap.put(14, "Device-Log Alert");
		facilityMap.put(15, "Device-Clock");
		facilityMap.put(16, "Device-Local0");
		facilityMap.put(17, "Device-Local1");
		facilityMap.put(18, "Device-Local2");
		facilityMap.put(19, "Device-Local3");
		facilityMap.put(20, "Device-Local4");
		facilityMap.put(21, "Device-Local5");
		facilityMap.put(22, "Device-Local6");
		facilityMap.put(23, "Device-Local7");
		facilityMap.put(FACILITY_SHELL, "Shell");
		facilityMap.put(FACILITY_WEB, "Web");
		facilityMap.put(FACILITY_WEBSERVICE, "WebService");
		facilityMap.put(FACILITY_CORE, "Core");
		facilityMap.put(FACILITY_TR069, "TR069");
		facilityMap.put(FACILITY_OPP, "OPP");
		facilityMap.put(FACILITY_STUN, "STUN");
		facilityMap.put(FACILITY_SPP, "SPP");
		facilityMap.put(FACILITY_TFTP, "TFTP");
		facilityMap.put(FACILITY_MONITOR, "Monitor");
		facilityMap.put(FACILITY_SHELL_TEST, "Shell Test");
		facilityMap.put(FACILITY_WEB_TEST, "Web Test");
		facilityMap.put(FACILITY_WEBSERVICE_TEST, "WebService Test");
		facilityMap.put(FACILITY_CORE_TEST, "Core Test");
		facilityMap.put(FACILITY_TR069_TEST, "TR069 Test");
		facilityMap.put(FACILITY_OPP_TEST, "OPP Test");
		facilityMap.put(FACILITY_STUN_TEST, "STUN Test");
		facilityMap.put(FACILITY_SPP_TEST, "HTTP Test");
		facilityMap.put(FACILITY_TFTP_TEST, "TFTP Test");
		facilityMap.put(FACILITY_MONITOR_TEST, "Monitor Test");
		facilityMap.put(FACILITY_DBLIB_TEST, "DBLib Test");

	}

	public static String getFacilityName(int facility) {
		String name = facilityMap.get(facility);
		if (name != null)
			return name;
		return "UNKNOWN";
	}

	public static Integer getSeverityInt(String severityName) {
		Integer i = severityStrMap.get(severityName);
		if (i != null)
			return i;
		return SEVERITY_DEBUG;
	}

	public static String getSeverityName(int severity) {
		String name = severityMap.get(severity);
		if (name != null)
			return name;
		return "UNKNOWN";
	}

	public static String getEventName(int event) {
		String name = eventMap.get(event);
		if (name != null)
			return name;
		return "UNKNOWN";
	}
}
