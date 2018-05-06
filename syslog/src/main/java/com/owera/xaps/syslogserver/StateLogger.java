package com.owera.xaps.syslogserver;

import java.sql.Connection;
import java.util.Map;

import com.owera.common.db.ConnectionProvider;
import com.owera.common.log.Logger;
import com.owera.common.scheduler.TaskDefaultImpl;

public class StateLogger extends TaskDefaultImpl {

	public StateLogger(String taskName) {
		super(taskName);
	}

	private Logger logger = new Logger(); // Logging of internal matters - if necessary
	private static Logger stability = new Logger("Stability");
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

		Map<Connection, Long> syslogUsedConn = ConnectionProvider.getUsedConnCopy(ConnectionProvider.getConnectionProperties("xaps-syslog.properties", "db.syslog"));
		String message = "";
		message += String.format("%15s | ", getUsedMemory());
		message += String.format("%13s | ", DiskSpaceCheck.getFreeSpace());
		message += String.format("%10s | ", DuplicateCheck.getDuplicateSize());
		message += String.format("%13s | ", Syslog2DB.getUnitCacheSize());
		if (syslogUsedConn != null)
			message += String.format("%7s ", syslogUsedConn.size());
		else
			message += String.format("%7s ", 0);
		stability.info(message);
	}

	@Override
	public Logger getLogger() {
		return logger;
	}

}
