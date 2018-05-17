package com.github.freeacs.syslogserver;

import com.github.freeacs.common.scheduler.TaskDefaultImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StateLogger extends TaskDefaultImpl {

	public StateLogger(String taskName) {
		super(taskName);
	}

	private static Logger logger = LoggerFactory.getLogger(StateLogger.class);
	private static Logger stability = LoggerFactory.getLogger("Stability");
	private static int summaryHeaderCount = 0;

	private String getUsedMemory() {
		long total = Runtime.getRuntime().totalMemory();
		long free = Runtime.getRuntime().freeMemory();
		long used = total - free;
		return "" + used / (1024 * 1024);
	}

	@Override
	public void runImpl() throws Throwable {
		if (summaryHeaderCount == 0) {
			stability.info("MemoryUsed (MB) | FreeDisk (MB) | Duplicates | UnitCacheSize | DB conn ");
			stability.info("-----------------------------------------------------------------------");
		}
		summaryHeaderCount++;
		if (summaryHeaderCount == 20)
			summaryHeaderCount = 0;

		String message = "";
		message += String.format("%15s | ", getUsedMemory());
		message += String.format("%13s | ", DiskSpaceCheck.getFreeSpace());
		message += String.format("%10s | ", DuplicateCheck.getDuplicateSize());
		message += String.format("%13s | ", Syslog2DB.getUnitCacheSize());
		stability.info(message);
	}

	@Override
	public Logger getLogger() {
		return logger;
	}

}
