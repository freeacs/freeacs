package com.owera.tr069client;

import java.util.Random;

public class GPValue {
	private String value = null;
	private int min = 0;
	private int max = 0;
	private static Random random = new Random();

	public GPValue(String value) {
		this.value = value;
	}

	public GPValue(int min, int max) {
		this.min = min;
		this.max = max;
	}

	public String getValue() {
		if (value != null)
			return value;
		else {
			return "" + (random.nextInt(max - min) + min);
		}
	}
}
