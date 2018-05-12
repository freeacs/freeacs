package com.github.freeacs.common.util;

public class MemoryDebugger {

	public static void mem(String s) {

		System.gc();
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		long used = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		System.out.println("Used mem: " + used / 1024 + " KB, " + s);

	}

}
