package com.github.freeacs.base.db;

import com.github.freeacs.base.Log;
import com.github.freeacs.base.SessionDataI;
import com.github.freeacs.dbi.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DBAccessSession {

	private final DBAccess dbAccess;
	private XAPS xaps;

	public DBAccessSession(DBAccess dbAccess) throws SQLException {
		this.dbAccess = dbAccess;
		this.xaps = dbAccess.getDBI().getXaps();
	}

	private static void debug(String message) {
		Log.debug(DBAccessSession.class, message);
	}

	public void writeProfileChange(String unitId, Profile newProfile) throws SQLException{
		long start = System.currentTimeMillis();
		String method = "writeProfileChange";
		try {
			XAPSUnit xapsUnit = dbAccess.getXAPSUnit(xaps);
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
			XAPSUnit xapsUnit = dbAccess.getXAPSUnit(xaps);
			xapsUnit.deleteUnitParameters(unitParameters);
			debug("Have deleted " + unitParameters.size() + " unit parameters");
		} catch (Throwable t) {
			DBAccess.handleError(action, start, t);
		}
	}

	public Unit readUnit(String unitId) throws SQLException {
		long start = System.currentTimeMillis();
		String method = "readUnit";
		Unit unit = null;
		try {
			XAPSUnit xapsUnit = dbAccess.getXAPSUnit(xaps);
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

	public DBAccess getDbAccess() {
		return dbAccess;
	}
}
