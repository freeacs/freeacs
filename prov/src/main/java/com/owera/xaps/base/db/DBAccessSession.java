package com.owera.xaps.base.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.owera.xaps.base.Log;
import com.owera.xaps.base.SessionDataI;
import com.owera.xaps.dbi.DBI;
import com.owera.xaps.dbi.Profile;
import com.owera.xaps.dbi.Unit;
import com.owera.xaps.dbi.UnitParameter;
import com.owera.xaps.dbi.Unittype;
import com.owera.xaps.dbi.UnittypeParameter;
import com.owera.xaps.dbi.XAPS;
import com.owera.xaps.dbi.XAPSUnit;

public class DBAccessSession {

	private XAPS xaps;

	public DBAccessSession(DBI dbi) {
		this.xaps = dbi.getXaps();
	}

	private static void debug(String message) {
		Log.debug(DBAccessSession.class, message);
	}

	public void writeProfileChange(String unitId, Profile newProfile) throws SQLException{
		long start = System.currentTimeMillis();
		String method = "writeProfileChange";
		try {
			XAPSUnit xapsUnit = DBAccess.getXAPSUnit(xaps);
			List<String> uList = new ArrayList<String>();
			uList.add(unitId);
			xapsUnit.addUnits(uList, newProfile);
		} catch (Throwable t) {
			DBAccess.handleError(method, start, t);
		}

	}

	public void writeUnittypeParameters(SessionDataI sessionData, List<UnittypeParameter> utpList) throws SQLException {
		long start = System.currentTimeMillis();
		String method = "writeUnittypeParameters";
		try {
			Unittype ut = sessionData.getUnittype();
			ut.getUnittypeParameters().addOrChangeUnittypeParameters(utpList, xaps);
			debug("Have written " + utpList.size() + " unittype parameters");
		} catch (Throwable t) {
			DBAccess.handleError(method, start, t);
		}
	}

	public void deleteUnitParameters(List<UnitParameter> unitParameters) throws SQLException {
		long start = System.currentTimeMillis();
		String action = "deleteUnitParameters";
		try {
			XAPSUnit xapsUnit = DBAccess.getXAPSUnit(xaps);
			xapsUnit.deleteUnitParameters(unitParameters);
			debug("Have deleted " + unitParameters.size() + " unit parameters");
		} catch (Throwable t) {
			DBAccess.handleError(action, start, t);
		}
	}

	//	public Map<String, JobParameter> readJobParameters(SessionDataI sessionData, Job job) throws SQLException {
	//		long start = System.currentTimeMillis();
	//		String method = "readJobParameters";
	//		Jobs jobs = sessionData.getUnittype().getJobs();
	//		//		XAPSJobs xapsJobs = DBAccess.getXAPSJobs(xaps);
	//		try {
	//			Map<String, JobParameter> jobParams = jobs.readJobParameters(job, new Unit(sessionData.getUnitId(), null, null), xaps);
	//			debug("Found " + jobParams.size() + " job parameters (job-id:" + job.getId() + ")");
	//			return jobParams;
	//		} catch (Throwable t) {
	//			DBAccess.handleError(method, start, t);
	//		}
	//		return null; // unreachable code - compiler doesn't detect it!
	//
	//	}

	public Unit readUnit(String unitId) throws SQLException {
		long start = System.currentTimeMillis();
		String method = "readUnit";
		Unit unit = null;
		try {
			XAPSUnit xapsUnit = DBAccess.getXAPSUnit(xaps);
			unit = xapsUnit.getUnitById(unitId);
			if (unit != null)
				debug("Found unit " + unit.getId() + ", unittype " + unit.getUnittype().getName() + ", profile " + unit.getProfile().getName());
			return unit;
		} catch (Throwable t) {
			DBAccess.handleError(method, start, t);
		}
		return null; // unreachable code - compiler doesn't detect it!
	}

	public XAPS getXaps() {
		return xaps;
	}
}
