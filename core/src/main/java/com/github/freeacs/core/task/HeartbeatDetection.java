package com.github.freeacs.core.task;

import com.github.freeacs.common.util.Cache;
import com.github.freeacs.common.util.CacheValue;
import com.github.freeacs.common.util.TimestampMap;
import com.github.freeacs.core.util.SyslogMessageMapContainer;
import com.github.freeacs.dbi.*;
import com.github.freeacs.dbi.util.SQLUtil;
import com.github.freeacs.dbi.util.SyslogClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

public class HeartbeatDetection extends DBIShare {

	public static int ACTIVE_DEVICE = -1; // Will not be at conflict with any Heartbeat-Id

	private static long MINUTE_MS = 60 * 1000;
	private static long HOUR_MS = 60 * MINUTE_MS;
	private static long OFFSET = MINUTE_MS;
	private static SimpleDateFormat sdf = new SimpleDateFormat("MMM dd HH:mm:ss", Locale.US);

	private ACS acs;
	private long lastTms;
	// Contains SyslogMessageMaps (which wraps InsertOrderMap) for each heartbeat
	private SyslogMessageMapContainer smmc = new SyslogMessageMapContainer();
	private TimestampMap activeDevices = new TimestampMap();
	private Cache sentMessages = new Cache();
	private static Logger logger = LoggerFactory.getLogger(HeartbeatDetection.class);

	public HeartbeatDetection(String taskName, DataSource mainDataSource, DataSource syslogDataSource) throws SQLException {
		super(taskName, mainDataSource, syslogDataSource);
	}

	@Override
	public void runImpl() throws Exception {
		acs = getLatestACS();
		// Set the tms back 60 sec, since we expect all writing 
		// to syslog table (with tms 60 sec old) to be finished
		Long tms = getLaunchTms() - OFFSET;

		// Clean old records from the activce device map and heartbeat maps
		removeOldEntriesFromActiveDevices(tms);
		removeOldEntriesFromSyslogMaps(tms);
		// Clean old heartbeat maps
		updateSyslogMessageMaps();

		// Find new active devices - update active devices map
		findActiveDevices(tms - 5 * MINUTE_MS, tms);
		// Find new heartbeats - update heartbeat maps
		findHeartbeats(lastTms, tms);

		// Find missing heartbeats (the difference between active device map and heartbeat maps)
		filterAndSendHeartbeats(tms);

		lastTms = tms;
	}

	private void findHeartbeats(long from, long to) throws SQLException {
		long orgTo = to;
		long orgFrom = from;
		if (from == 0)
			logger.info("HeartbeatDetection: FindHeartbeats: Parse syslog from the last hours (depending on heartbeat timeout) up to " + sdf.format(new Date(to)));
		else
			logger.info("HeartbeatDetection: FindHeartbeats: Parse syslog in the timespan " + sdf.format(new Date(from)) + " to " + sdf.format(new Date(to)));
		Connection c = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		DynamicStatement ds = null;
		try {
			c = getSyslogDataSource().getConnection();
			Unittype[] unittypes = acs.getUnittypes().getUnittypes();
			for (Unittype unittype : unittypes) {
				Heartbeat[] heartbeats = unittype.getHeartbeats().getHeartbeats();
				for (Heartbeat heartbeat : heartbeats) {
					to = orgTo;
					from = orgFrom;
					logger.debug("HeartbeatDetection: FindHeartbeats: Process unittype " + unittype.getName() + " and heartbeat " + heartbeat.getName() + " [" + heartbeat.getId()
							+ "] with expression " + heartbeat.getExpression());
					SyslogMessageMapContainer.SyslogMessageMap smm = smmc.getSyslogMessageMap(heartbeat.getId());
					if (smm == null) { // The heartbeat is new - happens at server-startup and if there's been detected some change to it (group/expression/etc)
						logger.debug("HeartbeatDetection: FindHeartbeats: Creating new syslog message map - because server-startup or heartbeat-change");
						smm = smmc.createSyslogMessageMap(heartbeat);
						from = new Date(to - (long) heartbeat.getTimeoutHours() * HOUR_MS).getTime();
						to = from + 5 * 60000;
					}
					while (to <= orgTo) { // Loop will only run more than once after server-startup, when we try to "catch up" (memory-structures are empty)
						ds = new DynamicStatement();
						ds.addSql("SELECT distinct(unit_id) FROM syslog WHERE ");
						//						if (from == 0)
						//							ds.addSqlAndArguments("collector_timestamp >= ? AND ", new Date(to - (long) heartbeat.getTimeoutHours() * HOUR_MS));
						//						else
						ds.addSqlAndArguments("collector_timestamp >= ? AND ", new Date(from));
						ds.addSqlAndArguments("collector_timestamp < ? AND ", new Date(to));
						ds.addSqlAndArguments("unit_type_name = ? AND ", unittype.getName());
						ds = SQLUtil.input2SQLCriteria(ds, "content", heartbeat.getExpression());
						ds.addSqlAndArguments(" AND content NOT LIKE ?", "%" + Heartbeat.MISSING_HEARTBEAT_ID + "%");
						ps = ds.makePreparedStatement(c);
						rs = ps.executeQuery();
						logger.debug("HeartbeatDetection: FindHeartbeats: " + ds.getDebugMessage());
						int counter = 0;
						Map<String, Unit> unitsInGroupMap = null;
						if (heartbeat.getGroup() != null) {
							ACSUnit acsUnit = new ACSUnit(acs.getConnectionProperties(), acs, acs.getSyslog());
							unitsInGroupMap = acsUnit.getUnits(heartbeat.getGroup());
						}
						while (rs.next()) {
							String unitId = rs.getString("unit_id");
							if (unitsInGroupMap == null)
								smm.append(unitId, to);
							else if (unitsInGroupMap != null && unitsInGroupMap.get(unitId) != null)
								smm.append(unitId, to);
							counter++;
						}
						logger.debug("HeartbeatDetection: FindHeartbeats: " + counter + " devices matching " + heartbeat.getExpression() + " [" + smm + "]");
						to += 5 * 60000;
						from += 5 * 60000;
					}
				}
			}
		} catch (SQLException sqlex) {
			logger.error("HeartbeatDetection: FindHeartbeats: SQL that failed: " + ds.getSqlQuestionMarksSubstituted(), sqlex);
			throw sqlex;
		} finally {
			if (rs != null)
				rs.close();
			if (ps != null)
				ps.close();
			if (c != null) {
				c.close();
			}
		}

	}

	private void findActiveDevices(long from, long to) throws SQLException {
		logger.info("HeartbeatDetection: FindActiveDevices: Parse syslog in the timespan " + sdf.format(new Date(from)) + " to " + sdf.format(new Date(to)));
		Connection c = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		DynamicStatement ds = null;
		int counter = 0;
		try {
			c = getSyslogDataSource().getConnection();
			ds = new DynamicStatement();
			ds.addSql("SELECT distinct(unit_id) FROM syslog WHERE ");
			ds.addSqlAndArguments("collector_timestamp >= ? AND ", new Date(from));
			ds.addSqlAndArguments("collector_timestamp < ? AND ", new Date(to));
			ds.addSqlAndArguments("facility < ?", SyslogConstants.FACILITY_SHELL);
			ps = ds.makePreparedStatement(c);
			rs = ps.executeQuery();
			logger.debug("- " + ds.getDebugMessage());
			while (rs.next()) {
				String unitId = rs.getString("unit_id");
				activeDevices.put(unitId, to);
				counter++;
			}
			logger.debug("HeartbeatDetection: FindActiveDevices: Found " + counter + " devices (total number of active: " + activeDevices.size() + ")");
		} catch (SQLException sqlex) {
			logger.error("HeartbeatDetection: FindActiveDevices: SQL that failed: " + ds.getSqlQuestionMarksSubstituted(), sqlex);
			throw sqlex;
		} finally {
			if (rs != null)
				rs.close();
			if (ps != null)
				ps.close();
			if (c != null) {
				c.close();
			}
		}

	}

	private void filterAndSendHeartbeats(long to) throws SQLException, IOException {
		// Now process the maps and the "absence"-events to see if there's any units
		// missing and build a list of missing events from units
		ACSUnit acsUnit = new ACSUnit(getMainDataSource(), acs, getSyslog());
		for (SyslogMessageMapContainer.SyslogMessageMap smm : smmc.getContainerValues()) {
			logger.debug("HeartbeatDetection: FilterHeartbeats: Process " + smm);
			// The list contains the units in the group without the heartbeat message
			List<String> unitIdsAbsent = new ArrayList<String>();
			Heartbeat heartbeat = smm.getHeartbeat();
			Group group = heartbeat.getGroup();
			Map<String, Unit> groupUnits = acsUnit.getUnits(group);
			for (String unitId : groupUnits.keySet()) {
				if (smm.getUnitIdTmsMap().get(unitId) == null && sentMessages.get(heartbeat.getId() + ":" + unitId) == null)
					unitIdsAbsent.add(unitId);
			}
			logger.debug("HeartbeatDetection: FilterHeartbeats: Have found " + unitIdsAbsent.size() + " units for heartbeat " + heartbeat + " with missing heartbeats");

			int noHeartbeatNotActiveCounter = 0;
			int missingHeartbeatCounter = 0;
			int unitNotFoundCounter = 0;
			for (String unitIdMissing : unitIdsAbsent) {
				Unit unit = acsUnit.getUnitById(unitIdMissing);
				if (unit == null) {
					logger.debug("HeartbeatDetection: FilterHeartbeats: Unit " + unitIdMissing + " was not found in Fusion, will not generate syslog message for this unit");
					unitNotFoundCounter++;
					continue;
				}
				Long lastActivityTms = activeDevices.get(unit.getId());
				if (lastActivityTms != null) {
					sendHeartbeat(heartbeat, unitIdMissing, to);
					missingHeartbeatCounter++;
				} else
					noHeartbeatNotActiveCounter++;
			}
			logger.debug("HeartbeatDetection: SendHeartbeats: Found " + unitNotFoundCounter + " units not defined in Fusion with missing heartbeats (no missing heartbeat message created)");
			logger.debug("HeartbeatDetection: SendHeartbeats: Found " + noHeartbeatNotActiveCounter + " inactive units with missing heartbeats (no missing heartbeat message created)");
			logger.info("HeartbeatDetection: SendHeartbeats: Created " + missingHeartbeatCounter + " missing heartbeat syslog entries");
		}
	}

	private void sendHeartbeat(Heartbeat heartbeat, String unitId, long tms) throws SQLException, IOException {
		String expression = heartbeat.getExpression();
		if (heartbeat.getExpression().startsWith("^"))
			expression = expression.substring(1);
		if (heartbeat.getExpression().endsWith("$"))
			expression = expression.substring(0, expression.length() - 1);
		String content = expression + " " + Heartbeat.MISSING_HEARTBEAT_ID + " for " + heartbeat.getTimeoutHours() + " hours";
		// set tms correctly - cancel the offset
		String msg = SyslogClient.makeMessage(SyslogConstants.SEVERITY_WARNING, new Date(tms + OFFSET), null, unitId, content, getSyslog());
		SyslogClient.send(msg);
		logger.debug("HeartbeatDetection: SendHeartbeats: Missing heartbeat registered for " + unitId + " : " + content);

		// Insert message into a map -> avoid resending heartbeat missing on every run of the heartbeat task
		Calendar timeout = Calendar.getInstance();
		timeout.setTime(new Date(tms - HOUR_MS - 1));
		CacheValue cv = new CacheValue(unitId);
		cv.setType(Cache.ABSOLUTE);
		cv.setTimeout(tms + HOUR_MS - 1 - cv.getCreated());
		sentMessages.put(heartbeat.getId() + ":" + unitId, cv);
	}

	private void removeOldEntriesFromActiveDevices(long to) {
		long tooOldTms = to - HOUR_MS;
		TimestampMap unitIdTmsMap = activeDevices;
		Map<String, Long> removedMap = unitIdTmsMap.removeOld(tooOldTms);
		logger.debug("HeartbeatDetection: RemoveOldEntries: Have removed " + removedMap.size() + " devices from active devices map");

	}

	private void removeOldEntriesFromSyslogMaps(long to) {
		// Now remove all old entries according the the timeout-rule
		for (SyslogMessageMapContainer.SyslogMessageMap smm : smmc.getContainerValues()) {
			// Any records older than this timestamp will be removed
			long tooOldTms = to - smm.getHeartbeat().getTimeoutHours() * HOUR_MS;
			TimestampMap unitIdTmsMap = smm.getUnitIdTmsMap();
			Map<String, Long> removedMap = unitIdTmsMap.removeOld(tooOldTms);
			logger.debug("HeartbeatDetection: RemoveOldEntries: Have removed " + removedMap.size() + " devices from syslog map [" + smm + "], entries were older than " + new Date(tooOldTms));
		}
	}

	// Check through all SyslogMessageMaps, and remove those which no longer
	// has a heartbeat-object with a live and working Unittype and Group.
	private void updateSyslogMessageMaps() {
		Iterator<Integer> keyIterator = smmc.getIterator();
		while (keyIterator.hasNext()) {
			Integer heartbeatId = keyIterator.next();
			SyslogMessageMapContainer.SyslogMessageMap smm = smmc.getSyslogMessageMap(heartbeatId);
			// This heartbeat object could be old/outdated - we
			Heartbeat heartbeat = smm.getHeartbeat();

			Unittype unittypeFreeacs = acs.getUnittype(heartbeat.getUnittype().getId());
			if (unittypeFreeacs == null) {
				keyIterator.remove();
				logger.debug("HeartbeatDetection: UpdateSyslogMessageMap: Unittype " + heartbeat.getUnittype().getName() + " could not be found, syslog message map is removed");
				continue;
			}
			Heartbeat heartbeatFreeacs = unittypeFreeacs.getHeartbeats().getById(heartbeatId);
			if (heartbeatFreeacs == null) {
				keyIterator.remove();
				logger.debug("HeartbeatDetection: UpdateSyslogMessageMap: Heartbeat " + heartbeat.getId() + " could not be found, syslog message map is removed");
				continue;
			}
			Group groupFreeacs = unittypeFreeacs.getGroups().getById(heartbeat.getGroup().getId());
			if (groupFreeacs == null) {
				keyIterator.remove();
				logger.debug("HeartbeatDetection: UpdateSyslogMessageMap: Group " + heartbeat.getGroup().getName() + " could not be found, syslog message map is removed");
				continue;
			}
			if (!heartbeat.getExpression().equals(heartbeatFreeacs.getExpression())) {
				keyIterator.remove();
				logger.debug("HeartbeatDetection: UpdateSyslogMessageMap: Heartbeat expression " + heartbeat.getExpression() + " has changed to " + heartbeatFreeacs.getExpression()
						+ ", we'll remove this syslog message map and start over");
				continue;
			}
			smm.setHeartbeat(heartbeatFreeacs);
		}
	}

	@Override
	public Logger getLogger() {
		return logger;
	}
}
