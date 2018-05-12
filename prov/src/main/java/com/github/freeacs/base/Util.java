package com.github.freeacs.base;

import com.github.freeacs.base.db.DBAccessStatic;
import com.github.freeacs.dbi.UnitParameter;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.dbi.UnittypeParameter;
import com.github.freeacs.dbi.util.SystemParameters;

import java.util.ArrayList;
import java.util.List;

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
