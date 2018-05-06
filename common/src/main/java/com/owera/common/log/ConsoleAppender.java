package com.owera.common.log;


public class ConsoleAppender extends Appender {
	
	public void log(LogObject lo) {
		System.out.print(lo.getCompleteMessage());
	}

	@Override
	public void constructor() {
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}

}
