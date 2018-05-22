package com.github.freeacs.tr069.background;

import com.github.freeacs.common.scheduler.TaskDefaultImpl;
import com.github.freeacs.dbi.*;
import com.github.freeacs.dbi.util.SyslogClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.Map.Entry;

public class ActiveDeviceDetectionTask extends TaskDefaultImpl {

	private static Logger logger = LoggerFactory.getLogger(ActiveDeviceDetectionTask.class);

	private DBI dbi;

	public static Map<String, Long> activeDevicesMap = new HashMap<String, Long>();

	public ActiveDeviceDetectionTask(String taskName, DBI dbi) {
		super(taskName);
		this.dbi = dbi;

	}

	public static synchronized void addActiveDevice(String unitId, Long nextInformMs) {
		activeDevicesMap.put(unitId, System.currentTimeMillis() + nextInformMs);
	}

	public static int activeDevices() {
		return activeDevicesMap.size();
	}

	@Override
	public void runImpl() throws Throwable {
		Long anHourAgo = System.currentTimeMillis() - 60 * 60000;
		logger.info("ActiveDeviceDetectionTask: Will check if some devices scheduled to return before " + new Date(anHourAgo) + " are too late");
		Map<String, Long> inactiveUnits = cleanOld(anHourAgo);
		logger.info("ActiveDeviceDetectionTask: Have found " + inactiveUnits.size() + " inactive devices");
		for (Entry<String, Long> entry : inactiveUnits.entrySet()) {
			String unitId = entry.getKey();
			Syslog syslog = dbi.getAcs().getSyslog();
			SyslogFilter sf = new SyslogFilter();
			sf.setCollectorTmsStart(new Date(anHourAgo)); // look for syslog newer than 1 hour
			sf.setUnitId(unitId);
			boolean active = false;
			List<SyslogEntry> entries = syslog.read(sf, dbi.getAcs());
			for (SyslogEntry sentry : entries) {
				if (sentry.getFacility() < SyslogConstants.FACILITY_SHELL && !sentry.getContent().contains(Heartbeat.MISSING_HEARTBEAT_ID)) {
					logger.info("ActivceDeviceDetection: Found syslog activity for unit " + unitId + " at " + sentry.getCollectorTimestamp() + " : " + sentry.getContent());
					active = true;
					break;
				}
			}
			if (active) {
				SyslogClient.info(entry.getKey(), "ProvMsg: No provisioning at " + new Date(entry.getValue()) + " (as expected) or since, but device has been active since " + new Date(anHourAgo)
						+ ". TR-069 client may have stopped", dbi.getAcs().getSyslog());
				logger.info("ActivceDeviceDetection: Unit " + entry.getKey() + ": No provisioning at " + new Date(entry.getValue()) + " (as expected) or since, but device has been active "
						+ new Date(anHourAgo) + ". TR-069 client may have stopped");
			} else {
				logger.info("ActivceDeviceDetection: Unit " + entry.getKey() + ": No provisioning at " + new Date(entry.getValue()) + " (as expected) or since, but device may be inactive since "
						+ new Date(anHourAgo));
			}
		}
	}

	private static synchronized Map<String, Long> cleanOld(long tms) {
		Iterator<String> iterator = activeDevicesMap.keySet().iterator();
		Map<String, Long> removedUnitsMap = new HashMap<String, Long>();
		while (iterator.hasNext()) {
			String unitId = iterator.next();
			Long nextInformTms = activeDevicesMap.get(unitId);
			if (nextInformTms < tms) {
				iterator.remove();
				removedUnitsMap.put(unitId, nextInformTms);
			}
		}
		return removedUnitsMap;
	}

	public static synchronized void remove(String unitId) {
		activeDevicesMap.remove(unitId);
	}

	@Override
	public Logger getLogger() {
		return logger;
	}

}
