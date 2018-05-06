package com.owera.xaps.stun;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.owera.common.db.ConnectionProperties;
import com.owera.common.db.NoAvailableConnectionException;
import com.owera.common.log.Logger;
import com.owera.common.scheduler.TaskDefaultImpl;
import com.owera.common.util.TimestampMap;
import com.owera.xaps.dbi.DBI;
import com.owera.xaps.dbi.Heartbeat;
import com.owera.xaps.dbi.Syslog;
import com.owera.xaps.dbi.SyslogConstants;
import com.owera.xaps.dbi.SyslogEntry;
import com.owera.xaps.dbi.SyslogFilter;
import com.owera.xaps.dbi.Unit;
import com.owera.xaps.dbi.XAPSUnit;
import com.owera.xaps.dbi.util.SyslogClient;

import de.javawi.jstun.test.demo.StunServer;

public class ActiveDeviceDetection extends TaskDefaultImpl {

	private static Logger logger = new Logger();
	private ConnectionProperties xapsCp;
	private DBI dbi;
	private TimestampMap activeDevicesLogged = new TimestampMap();

	public ActiveDeviceDetection(ConnectionProperties xapsCp, DBI dbi, String taskName) {
		super(taskName);
		this.xapsCp = xapsCp;
		this.dbi = dbi;
	}

	private void logActiveDevices(TimestampMap activeDevices, TimestampMap sentSyslogMap) throws NoAvailableConnectionException, SQLException {
		// Process 1/60 of all active devices each time this method is called
		// Note that process 1/60 of the activeDevices map will be too much, since
		// some of the units will be more than 5 minutes old - and we cannot log
		// as "actually active" if the device timestamp is more than 5 minutes old.
		// Hence a portion of the activeDevices map (perhaps 1/10?) will be older
		// than 5 minutes, and then those 1/60 will actually process a larger share
		// than strictly required. This is not a problem - the most important
		// thing is to process all devices within an hour and to distribute the
		// load over time. Worst case scenario is that no units will be processed
		// at the end of a 60-minute cycle.
		int unitsToProcess = activeDevices.size() / 60;
		long fiveMinAgo = getThisLaunchTms() - 5 * 60000;
		long oneHourAgo = getThisLaunchTms() - 60 * 60000;
		XAPSUnit xapsUnit = new XAPSUnit(xapsCp, dbi.getXaps(), dbi.getXaps().getSyslog());

		// this will force units which haven't been processed the last hour to
		// be processed again.
		Map<String, Long> oldDevices = sentSyslogMap.removeOld(oneHourAgo);
		logger.info("ActiveDeviceDetection: Have removed " + oldDevices.size() + " devices from sentSyslog map - should be approx 1/60 of activeDevices.size() (" + activeDevices.size() + ")");

		int processCount = 0;
		int loggedCount = 0;
		for (Entry<String, Long> entry : activeDevices.getMap().entrySet()) {
			String address = entry.getKey();
			if (processCount > unitsToProcess)
				break;
			if (entry.getValue() > fiveMinAgo) {
				if (sentSyslogMap.get(address) == null) {
					processCount++;
					sentSyslogMap.put(address, getThisLaunchTms());
					Unit unit = xapsUnit.getUnitByValue(address, null, null);
					if (unit != null) {
						loggedCount++;
						SyslogClient.notice(unit.getId(), "StunMsg/TR-111: Requests from " + address + " within last 5 minutes", 16, null, null);
					} else {
						logger.info("ActiveDeviceDetection: Stun request from " + address
								+ ", but the address was not recorded in Fusion - consider adding A-flag to UDPConnectionRequestAddress in all unittypes");
					}
				}
			}
		}
		logger.notice("ActiveDeviceDetection: Processed " + processCount + " active device syslog messages to the syslog server. Sent " + loggedCount + " syslog messages. SentSyslogMap.size() = "
				+ sentSyslogMap.size() + ", ActiveDevices.size() = " + activeDevices.size());
	}

	private void logInactiveDevices(TimestampMap activeDevices) throws NoAvailableConnectionException, SQLException {
		long tooOldTms = getThisLaunchTms() - 3600 * 1000;
		logger.info("Will check for inactive STUN clients (map size before check: " + activeDevices.size() + ")");
		Map<String, Long> tooOldMap = activeDevices.removeOldSync(tooOldTms);
		for (Entry<String, Long> entry : tooOldMap.entrySet()) {
			String address = entry.getKey();
			XAPSUnit xapsUnit = new XAPSUnit(xapsCp, dbi.getXaps(), dbi.getXaps().getSyslog());
			Unit unit = xapsUnit.getUnitByValue(address, null, null);
			if (unit != null) {
				Syslog syslog = dbi.getXaps().getSyslog();
				SyslogFilter sf = new SyslogFilter();
				sf.setCollectorTmsStart(new Date(tooOldTms)); // look for syslog newer than 1 hour
				sf.setUnitId(unit.getId());
				boolean active = false;
				List<SyslogEntry> entries = syslog.read(sf, dbi.getXaps());
				for (SyslogEntry sentry : entries) {
					String c = sentry.getContent();
					if (sentry.getFacility() < SyslogConstants.FACILITY_SHELL && !c.contains(Heartbeat.MISSING_HEARTBEAT_ID) && !c.startsWith("StunMsg/TR-111")) {
						logger.notice("ActivceDeviceDetection: Found syslog activity for unit " + unit.getId() + " at " + sentry.getCollectorTimestamp() + " : " + sentry.getContent());
						active = true;
						break;
					}
				}
				if (active) {
					logger.notice("ActiveDeviceDection: No STUN request from " + address + " (unit: " + unit.getId() + ") since " + new Date(tooOldTms));
					SyslogClient.notice(unit.getId(), "StunMsg/TR-111: No request from " + address + " since " + new Date(tooOldTms) + " - but device has been active since then", dbi.getXaps()
							.getSyslog());
				} else {
					logger.notice("ActiveDeviceDection: No STUN request from " + address + " (unit: " + unit.getId() + ") since " + new Date(tooOldTms) + " - but the device may not be active");
				}
			} else {
				logger.notice("ActiveDeviceDection: No STUN request from " + address + " for more than 60 minutes (the device may have changed IP).");
			}

		}
		logger.info("ActiveDeviceDection: Have removed " + tooOldMap.size() + " devices from active devices map");
	}

	@Override
	public void runImpl() throws Throwable {
		TimestampMap activeDevices = StunServer.getActiveStunClients();
		logInactiveDevices(activeDevices); // will clean out old and inactive devices from activeDevices map (and log new inactive devices)
		logActiveDevices(activeDevices, activeDevicesLogged); // will update list of devices logged to syslog (and log new ones)
	}

	@Override
	public Logger getLogger() {
		return logger;
	}

}
