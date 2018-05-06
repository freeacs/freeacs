package com.owera.common.log;

import java.util.ArrayList;
import java.util.List;

public class ConfigErrors {
	private static List<String> errors = new ArrayList<String>();

	public synchronized static void add(String msg) {
		errors.add(msg);
	}

	public synchronized static void log() {
		try {
			for (String errorMsg : errors) {
				Appender defaultAppender = Log.getAppender(Log.DEFAULT_APPENDER_NAME);
				LogObject lo = new LogObject();
				lo.setLogName("morten.log.Console");
				lo.setSimpleClassName("Console");
				lo.setSeverity(Log.ERROR_INT);
				lo.setCoreMessage(errorMsg);
				Log.computeDate(lo);
				Log.makeCompleteMessage(defaultAppender, lo);
				defaultAppender.log(lo);
			}
			errors.clear();
		} catch (Throwable t) {
			System.err.println("An error ocurred when printing error-messages:" + t);
			t.printStackTrace();
		}
	}

	public static int size() {
		return errors.size();
	}

}
