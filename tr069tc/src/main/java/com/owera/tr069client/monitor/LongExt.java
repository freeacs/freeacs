package com.owera.tr069client.monitor;

public class LongExt {
	private long current;
	private long last;

	public void backup() {
		last = current;
		current = 0;
	}

	public void addCurrent(long addition) {
		current += addition;
	}

	public long getCurrent() {
		return current;
	}

	public long getLast() {
		return last;
	}

	public long getDiff() {
		return current - last;
	}
}
