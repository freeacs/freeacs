package com.github.freeacs.base.db;

import com.github.freeacs.base.Log;
import com.github.freeacs.dbi.*;
import com.github.freeacs.dbi.Unittype.ProvisioningProtocol;
import com.github.freeacs.dbi.util.SystemParameters;
import com.github.freeacs.tr069.SessionData;
import com.github.freeacs.tr069.exception.TR069DatabaseException;
import com.github.freeacs.tr069.exception.TR069Exception;
import com.github.freeacs.tr069.exception.TR069ExceptionShortMessage;
import com.github.freeacs.tr069.xml.ParameterValueStruct;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DBAccessSessionTR069 {
	private ACS acs;
	private DBAccessSession dbAccessSession;

	public DBAccessSessionTR069(ACS acs, DBAccessSession dbAccessSession) {
		this.acs = acs;
		this.dbAccessSession = dbAccessSession;
	}

	private static void debug(String message) {
		Log.debug(DBAccessSessionTR069.class, message);
	}

	public void writeUnittypeProfileUnit(SessionData sessionData, String unittypeName, String unitId) throws TR069Exception {
		// If no product class is specified in the inform:
		if (unittypeName == null || unittypeName.trim().equals(""))
			unittypeName = "OUI-" + unitId.substring(0, 6);
		try {
			Unittype ut = acs.getUnittype(unittypeName);
			if (ut == null) {
				sessionData.setUnittypeCreated(false);
				ut = new Unittype(unittypeName, unittypeName, "Auto-generated", ProvisioningProtocol.TR069);
				acs.getUnittypes().addOrChangeUnittype(ut, acs);
				debug("Have created a unittype with the name " + unittypeName +" in discovery mode");
			} else {
				sessionData.setUnittypeCreated(true);
				debug("Unittype " + unittypeName + " already exists, no need to create it in discovery mode");
			}

			Profile pr = ut.getProfiles().getByName("Default");
			if (pr == null) {
				pr = new Profile("Default", ut);
				ut.getProfiles().addOrChangeProfile(pr, acs);
				debug("Have created a profile with the name " + pr.getName() + " in discovery mode");
			}

			sessionData.setUnittype(ut);
			sessionData.setProfile(pr);
			
			ACSUnit acsUnit = DBAccess.getXAPSUnit(acs);
			List<String> unitIds = new ArrayList<String>();
			unitIds.add(unitId);
			acsUnit.addUnits(unitIds, pr);
			List<UnitParameter> unitParameters = new ArrayList<UnitParameter>();
			UnittypeParameter secretUtp = ut.getUnittypeParameters().getByName(SystemParameters.SECRET);
			UnitParameter up = new UnitParameter(secretUtp, unitId, sessionData.getSecret(), pr);
			unitParameters.add(up);
			acsUnit.addOrChangeUnitParameters(unitParameters, pr);
			Unit unit = dbAccessSession.readUnit(sessionData.getUnitId());
			sessionData.setUnit(unit);
			debug("Have created a unit:" + unitId + " with the obtained secret");
		} catch (Throwable t) {
			String errorMsg = "Exception while auto-generating unittype/profile/unit";
			if (t instanceof SQLException) {
				throw new TR069DatabaseException(errorMsg, t);
			} else {
				throw new TR069Exception(errorMsg, TR069ExceptionShortMessage.MISC, t);
			}
		}
	}

	public void writeUnitSessionParams(SessionData sessionData) throws TR069DatabaseException {
		try {
			List<ParameterValueStruct> parameterValuesToDB = sessionData.getToDB();
			Unittype unittype = sessionData.getUnittype();
			Profile profile = sessionData.getProfile();
			List<UnitParameter> unitSessionParameters = new ArrayList<UnitParameter>();
			for (ParameterValueStruct pvs : parameterValuesToDB) {
				UnittypeParameter utp = unittype.getUnittypeParameters().getByName(pvs.getName());
				if (utp != null) {
					UnitParameter up = new UnitParameter(utp, sessionData.getUnitId(), pvs.getValue(), profile);
					if (utp.getName().startsWith("Device.") || utp.getName().startsWith("InternetGatewayDevice."))
						unitSessionParameters.add(up);
				} else
					Log.warn(DBAccessSession.class, "\t" + pvs.getName() + " : does not exist, cannot write session value " + pvs.getValue());
			}
			if (unitSessionParameters.size() > 0) {
				ACSUnit acsUnit = DBAccess.getXAPSUnit(acs);
				acsUnit.addOrChangeSessionUnitParameters(unitSessionParameters, profile);
			}
		} catch (SQLException sqle) {
			throw new TR069DatabaseException("Not possible to write session parameters to database", sqle);
		}
	}

	public static void writeUnitParams(SessionData sessionData) {
		List<ParameterValueStruct> parameterValuesToDB = sessionData.getToDB();
		List<UnitParameter> unitParameters = new ArrayList<UnitParameter>();
		Unittype unittype = sessionData.getUnittype();
		Profile profile = sessionData.getProfile();
		Unit unit = sessionData.getUnit();
		for (ParameterValueStruct pvs : parameterValuesToDB) {
			UnittypeParameter utp = unittype.getUnittypeParameters().getByName(pvs.getName());
			if (utp != null) {
				unitParameters.add(new UnitParameter(utp, sessionData.getUnitId(), pvs.getValue(), profile));
			} else
				Log.warn(DBAccessSession.class, "\t" + pvs.getName() + " : does not exist, cannot write value " + pvs.getValue());
		}
		DBAccessStatic.queueUnitParameters(unit, unitParameters, profile);
	}
}
