package com.github.freeacs.tr069.background;

import com.github.freeacs.base.http.Authenticator;
import com.github.freeacs.base.http.ThreadCounter;
import com.github.freeacs.common.scheduler.TaskDefaultImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StabilityTask extends TaskDefaultImpl {

	private static Logger logger = LoggerFactory.getLogger(StabilityTask.class);

	private int lineCounter = 0;

	private boolean serverStart = true;
	private long startTms;

	private static Logger log = LoggerFactory.getLogger("Stability");
	
	public StabilityTask(String taskName) {
		super(taskName);
	}

	// returns 16-char string
	private static String getTimeSinceStart(long timeSinceStart) {
		long hours = timeSinceStart / (3600l * 1000l);
		long min = (timeSinceStart - (hours * 3600l * 1000l)) / 60000l;
		long days = timeSinceStart / (3600l * 1000l * 24l);
		return String.format("(%4sd) %5s:%02d", days, hours, min);
	}
	
	private static String getUsedMemory() {
		//		Runtime.getRuntime().gc();
		long total = Runtime.getRuntime().totalMemory();
		long free = Runtime.getRuntime().freeMemory();
		long used = total - free;
		return "" + used / (1024 * 1024);

	}

	@Override
	public void runImpl() throws Throwable {
		if (serverStart) {
			log.error("The server starts...");
			serverStart = false;
			startTms = getThisLaunchTms();
		}
		if (lineCounter == 20)
			lineCounter = 0;
		if (lineCounter == 0)
			log.info("  TimeSinceStart | Memory (MB) | ActiveSessions | ActiveDevices | Blocked | DB-CONN (followed by a list of sec. used for each conn.)");
		lineCounter++;
		if (log != null && log.isInfoEnabled()) {
			String message = "";
			message += getTimeSinceStart(getThisLaunchTms() - startTms) + " | "; // 16-char string
			message += String.format("%11s | ", getUsedMemory());
			message += String.format("%14s | ", ThreadCounter.currentSessionsCount());
			message += String.format("%13s | ", ActiveDeviceDetectionTask.activeDevicesMap.size());
			message += String.format("%7s | ", Authenticator.getAndResetBlockedClientsCount());
			log.info(message);
		}
	}

	@Override
	public Logger getLogger() {
		return logger;
	}

}
