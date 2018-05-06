package com.owera.xaps.tr069.methods;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.owera.common.db.NoAvailableConnectionException;
import com.owera.xaps.base.Log;
import com.owera.xaps.base.db.DBAccess;
import com.owera.xaps.base.db.DBAccessSessionTR069;
import com.owera.xaps.dbi.UnittypeParameter;
import com.owera.xaps.dbi.UnittypeParameters;
import com.owera.xaps.dbi.util.ProvisioningMessage.ProvOutput;
import com.owera.xaps.dbi.util.ProvisioningMode;
import com.owera.xaps.tr069.HTTPReqResData;
import com.owera.xaps.tr069.SessionData;
import com.owera.xaps.tr069.exception.TR069DatabaseException;
import com.owera.xaps.tr069.xml.ParameterValueStruct;

public class GPVDecisionExtraction {

	/**
	 * Extraction mode will read all parameters from the device and write them to the
	 * unit_param_session table. No data will be written to unit_param table (provisioned data).
	 * 
	 * @param reqRes
	 * @throws TR069DatabaseException 
	 * @throws SQLException
	 * @throws NoAvailableConnectionException
	 */
	protected static void processExtraction(HTTPReqResData reqRes) throws TR069DatabaseException {
		SessionData sessionData = reqRes.getSessionData();
		UnittypeParameters utps = sessionData.getUnittype().getUnittypeParameters();
		List<ParameterValueStruct> toDB = new ArrayList<ParameterValueStruct>();
		Log.info(GPVDecisionExtraction.class, "Provisioning in " + ProvisioningMode.READALL.toString() + " mode, " + sessionData.getFromCPE().size()
				+ " params from CPE may be copied to ACS session storage");
		//		Log.info(GPVDecisionExtraction.class, "Provisioning in EXTRACTION mode, " + sessionData.getFromCPE().size() + " params from CPE may be copied to ACS session storage");
		for (int i = 0; i < sessionData.getFromCPE().size(); i++) {
			ParameterValueStruct pvsCPE = sessionData.getFromCPE().get(i);
			UnittypeParameter utp = utps.getByName(pvsCPE.getName());
			if (utp == null) {
				Log.debug(GPVDecision.class,  pvsCPE.getName() + " could not be stored in ACS, since name was unrecognized in ACS");
				continue;
			}
			if (pvsCPE.getValue().equals("(null)")) {
				Log.debug(GPVDecision.class,  pvsCPE.getName() + " will not be stored in ACS, since value was '(null)' - indicating not implemented");
				continue;
			}
			toDB.add(pvsCPE);
		}
		sessionData.setToDB(toDB);
		try {
			DBAccessSessionTR069 dbAccessSessionTR069 = new DBAccessSessionTR069(DBAccess.getDBI(), sessionData.getDbAccess());
			dbAccessSessionTR069.writeUnitSessionParams(sessionData);
			//			reqRes.getSessionData().getUnit().toWriteQueue(SystemParameters.PROVISIONING_MODE, ProvisioningMode.PERIODIC.toString());
			Log.debug(GPVDecisionExtraction.class, toDB.size() + " params written to ACS session storage");
			//			Log.debug(GPVDecisionExtraction.class, toDB.size() + " params written to ACS session storage  - provisioning mode will be reset to " + ProvisioningMode.PERIODIC.toString());
			reqRes.getResponse().setMethod(TR069Method.EMPTY);
			sessionData.getProvisioningMessage().setProvOutput(ProvOutput.EMPTY);
		} catch (SQLException sqle) {
			throw new TR069DatabaseException(sqle);
		}
	}
}
