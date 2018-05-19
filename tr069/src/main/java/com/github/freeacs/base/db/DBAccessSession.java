package com.github.freeacs.base.db;

import com.github.freeacs.base.Log;
import com.github.freeacs.base.SessionDataI;
import com.github.freeacs.dbi.*;

import java.sql.SQLException;
import java.util.List;

public class DBAccessSession {

	private final XAPS xaps;

	public DBAccessSession(XAPS xaps) {
		this.xaps = xaps;
	}

	public void writeUnittypeParameters(SessionDataI sessionData, List<UnittypeParameter> utpList) throws SQLException {
		String method = "writeUnittypeParameters";
		try {
			Unittype ut = sessionData.getUnittype();
			ut.getUnittypeParameters().addOrChangeUnittypeParameters(utpList, xaps);
			Log.debug(DBAccessSession.class, "Have written " + utpList.size() + " unittype parameters");
		} catch (Throwable t) {
			DBAccess.handleError(method, t);
		}
	}

	public Unit readUnit(String unitId) throws SQLException {
		String method = "readUnit";
		Unit unit;
		try {
			XAPSUnit xapsUnit = DBAccess.getXAPSUnit(xaps);
			unit = xapsUnit.getUnitById(unitId);
			if (unit != null)
				Log.debug(DBAccessSession.class, "Found unit " + unit.getId() + ", unittype " + unit.getUnittype().getName() + ", profile " + unit.getProfile().getName());
			return unit;
		} catch (Throwable t) {
			DBAccess.handleError(method, t);
		}
		return null; // unreachable code - compiler doesn't detect it!
	}

	public XAPS getXaps() {
		return xaps;
	}
}
