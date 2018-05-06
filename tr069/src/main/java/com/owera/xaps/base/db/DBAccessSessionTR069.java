package com.owera.xaps.base.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.owera.common.db.NoAvailableConnectionException;
import com.owera.xaps.base.Log;
import com.owera.xaps.dbi.DBI;
import com.owera.xaps.dbi.Profile;
import com.owera.xaps.dbi.Unit;
import com.owera.xaps.dbi.UnitParameter;
import com.owera.xaps.dbi.Unittype;
import com.owera.xaps.dbi.Unittype.ProvisioningProtocol;
import com.owera.xaps.dbi.UnittypeParameter;
import com.owera.xaps.dbi.XAPS;
import com.owera.xaps.dbi.XAPSUnit;
import com.owera.xaps.dbi.util.SystemParameters;
import com.owera.xaps.tr069.SessionData;
import com.owera.xaps.tr069.exception.TR069DatabaseException;
import com.owera.xaps.tr069.exception.TR069Exception;
import com.owera.xaps.tr069.exception.TR069ExceptionShortMessage;
import com.owera.xaps.tr069.xml.ParameterValueStruct;

public class DBAccessSessionTR069 {
	private XAPS xaps;
	private DBAccessSession dbAccessSession;

	public DBAccessSessionTR069(DBI dbi, DBAccessSession dbAccessSession) {
		this.xaps = dbi.getXaps();
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
			Unittype ut = xaps.getUnittype(unittypeName);
			if (ut == null) {
				sessionData.setUnittypeCreated(false);
				ut = new Unittype(unittypeName, unittypeName, "Auto-generated", ProvisioningProtocol.TR069);
				xaps.getUnittypes().addOrChangeUnittype(ut, xaps);
				debug("Have created a unittype with the name " + unittypeName +" in discovery mode");
			} else {
				sessionData.setUnittypeCreated(true);
				debug("Unittype " + unittypeName + " already exists, no need to create it in discovery mode");
			}
			//			List<UnittypeParameter> unittypeParameters = new ArrayList<UnittypeParameter>();
			//			for (Entry<String, UnittypeParameterFlag> entry : SystemParameters.commonParameters.entrySet()) {
			//				UnittypeParameter utp = ut.getUnittypeParameters().getByName(entry.getKey());
			//				if (utp == null) {
			//					utp = new UnittypeParameter(ut, entry.getKey(), entry.getValue());
			//					unittypeParameters.add(utp);
			//				}
			//			}
			//			UnittypeParameter[] crParams = new UnittypeParameter[3];
			//			CPEParameters cpeParams = sessionData.getCpeParameters();
			//
			//			crParams[0] = new UnittypeParameter(ut, cpeParams.CONNECTION_URL, new UnittypeParameterFlag("ADR"));
			//			crParams[1] = new UnittypeParameter(ut, cpeParams.CONNECTION_USERNAME, new UnittypeParameterFlag("RW"));
			//			crParams[2] = new UnittypeParameter(ut, cpeParams.CONNECTION_PASSWORD, new UnittypeParameterFlag("RW"));
			//			for (UnittypeParameter crParam : crParams) {
			//				UnittypeParameter utp = ut.getUnittypeParameters().getByName(crParam.getName());
			//				if (utp == null) {
			//					unittypeParameters.add(crParam);
			//				}
			//			}
			//			ut.getUnittypeParameters().addOrChangeUnittypeParameters(unittypeParameters, xaps);

			Profile pr = ut.getProfiles().getByName("Default");
			if (pr == null) {
				pr = new Profile("Default", ut);
				ut.getProfiles().addOrChangeProfile(pr, xaps);
				debug("Have created a profile with the name " + pr.getName() + " in discovery mode");
			}

			//			ProfileParameter ppUser = pr.getProfileParameters().getByName(cpeParams.CONNECTION_USERNAME);
			//			if (ppUser == null) {
			//				ppUser = new ProfileParameter(pr, ut.getUnittypeParameters().getByName(cpeParams.CONNECTION_USERNAME), "username");
			//				pr.getProfileParameters().addOrChangeProfileParameter(ppUser, xaps);
			//			}
			//			ProfileParameter ppPass = pr.getProfileParameters().getByName(cpeParams.CONNECTION_PASSWORD);
			//			if (ppPass == null) {
			//				ppPass = new ProfileParameter(pr, ut.getUnittypeParameters().getByName(cpeParams.CONNECTION_PASSWORD), "password");
			//				pr.getProfileParameters().addOrChangeProfileParameter(ppPass, xaps);
			//			}

			sessionData.setUnittype(ut);
			sessionData.setProfile(pr);
			
			XAPSUnit xapsUnit = DBAccess.getXAPSUnit(xaps);
			List<String> unitIds = new ArrayList<String>();
			unitIds.add(unitId);
			xapsUnit.addUnits(unitIds, pr);
			List<UnitParameter> unitParameters = new ArrayList<UnitParameter>();
			UnittypeParameter secretUtp = ut.getUnittypeParameters().getByName(SystemParameters.SECRET);
			UnitParameter up = new UnitParameter(secretUtp, unitId, sessionData.getSecret(), pr);
			unitParameters.add(up);
			xapsUnit.addOrChangeUnitParameters(unitParameters, pr);
			Unit unit = dbAccessSession.readUnit(sessionData.getUnitId());
			sessionData.setUnit(unit);
			debug("Have created a unit:" + unitId + " with the obtained secret");
		} catch (Throwable t) {
			String errorMsg = "Exception while auto-generating unittype/profile/unit";
			if (t instanceof NoAvailableConnectionException || t instanceof SQLException) {
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
				XAPSUnit xapsUnit = DBAccess.getXAPSUnit(xaps);
				xapsUnit.addOrChangeSessionUnitParameters(unitSessionParameters, profile);
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
