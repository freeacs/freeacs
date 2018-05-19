package com.github.freeacs.base.db;

import com.github.freeacs.base.BaseCache;
import com.github.freeacs.base.Log;
import com.github.freeacs.dbi.*;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class DBAccessStatic {

	private static void debug(String message) {
		Log.debug(DBAccessStatic.class, message);
	}

	private static void error(String message) {
		Log.error(DBAccessStatic.class, message);
	}

	public static byte[] readFirmwareImage(File firmwareFresh) throws SQLException {
		long start = System.currentTimeMillis();
		String action = "readFirmwareImage";
		try {
			File firmwareCache = BaseCache.getFirmware(firmwareFresh.getName(), firmwareFresh.getUnittype().getName());
			final File firmwareReturn;
			if (firmwareCache != null && Objects.equals(firmwareFresh.getId(), firmwareCache.getId()))
				firmwareReturn = firmwareCache;
			else {
				firmwareFresh.setBytes(firmwareFresh.getContent());
				BaseCache.putFirmware(firmwareFresh.getName(), firmwareFresh.getUnittype().getName(), firmwareFresh);
				firmwareReturn = firmwareFresh;
			}
			return firmwareReturn.getContent();
		} catch (Throwable t) {
			DBAccess.handleError(action, t);
		}
		return null; // Unreachable code - compiler doesn't detect it
	}

	public static void startUnitJob(String unitId, Integer jobId, DataSource xapsDataSource) throws SQLException {
		long start = System.currentTimeMillis();
		String action = "startUnitJob";
		try {
			UnitJobs unitJobs = new UnitJobs(xapsDataSource);
			UnitJob uj = new UnitJob(unitId, jobId);
			uj.setStartTimestamp(new Date());
			boolean updated = unitJobs.start(uj);
			if (updated) {
				debug("Have started unit-job (job " + jobId + ")");
			} else {
				error("The unit-job couldn't be started. The reason might it is already COMPLETED_OK state");
			}
		} catch (Throwable t) {
			DBAccess.handleError(action, t);
		}
	}

	public static void stopUnitJob(String unitId, Integer jobId, String unitJobStatus, DataSource xapsDataSource) throws SQLException {
		long start = System.currentTimeMillis();
		String action = "stopUnitJob";
		try {
			UnitJobs unitJobs = new UnitJobs(xapsDataSource);
			UnitJob uj = new UnitJob(unitId, jobId);
			uj.setEndTimestamp(new Date());
			uj.setStatus(unitJobStatus);
			boolean stopped = unitJobs.stop(uj);
			if (stopped) {
				debug("Have stopped unit-job (job " + jobId + "), status set to " + unitJobStatus);
			} else {
				error("The unit-job couldn't be stopped. The reason might be it is deleted or maybe even in COMPLETED_OK state already");
			}
		} catch (Throwable t) {
			DBAccess.handleError(action, t);
		}
	}

	// Write to queue, will be written to DB at the end of TR-069-session.
	public static void queueUnitParameters(Unit unit, List<UnitParameter> unitParameters, Profile profile) {
		for (UnitParameter up : unitParameters) {
			unit.toWriteQueue(up);	
		}
	}
}
