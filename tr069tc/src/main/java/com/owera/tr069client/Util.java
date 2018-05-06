package com.owera.tr069client;

import java.util.Random;

public class Util {

	private static Random random = new Random();

	public static long getRetrySleep(int executionCount) {
		long delay = 2500;
		for (int i = 0; i < executionCount; i++) {
			delay = delay * 2;
		}
		delay = delay + random.nextInt((int) delay);
		return delay;
	}
}
