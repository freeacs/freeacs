package com.owera.xaps.base;

import java.util.ArrayList;
import java.util.List;

import com.owera.xaps.base.db.DBAccessStatic;
import com.owera.xaps.dbi.UnitParameter;
import com.owera.xaps.dbi.Unittype;
import com.owera.xaps.dbi.UnittypeParameter;
import com.owera.xaps.dbi.util.SystemParameters;

public class Util {
	public static void resetReboot(SessionDataI sessionData) {
		Log.debug(Util.class, "The reboot parameter is reset to 0 and the reboot will be executed");
		Unittype unittype = sessionData.getUnittype();
		UnittypeParameter utp = unittype.getUnittypeParameters().getByName(SystemParameters.RESTART);
		List<UnitParameter> unitParameters = new ArrayList<UnitParameter>();
		UnitParameter up = new UnitParameter(utp, sessionData.getUnitId(), "0", sessionData.getProfile());
		unitParameters.add(up);
		DBAccessStatic.queueUnitParameters(sessionData.getUnit(), unitParameters, sessionData.getProfile());
	}

	public static void resetReset(SessionDataI sessionData) {
		Log.debug(Util.class, "The reset parameter is reset to 0 and the factory reset will be executed");
		Unittype unittype = sessionData.getUnittype();
		UnittypeParameter utp = unittype.getUnittypeParameters().getByName(SystemParameters.RESET);
		List<UnitParameter> unitParameters = new ArrayList<UnitParameter>();
		UnitParameter up = new UnitParameter(utp, sessionData.getUnitId(), "0", sessionData.getProfile());
		unitParameters.add(up);
		DBAccessStatic.queueUnitParameters(sessionData.getUnit(), unitParameters, sessionData.getProfile());
	}
}
