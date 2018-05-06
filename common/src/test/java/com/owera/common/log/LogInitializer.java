package com.owera.common.log;

public class LogInitializer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Log.initialize("test-logs.properties");
		Thread t = null;
		for (int i = 0; i < 50; i++) {
			t = new Thread(new LoggingClass(i));
			t.start();
		}
		System.out.println("Init completed and 50 threads are started");
	}
}
