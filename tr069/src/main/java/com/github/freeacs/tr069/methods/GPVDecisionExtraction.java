package com.github.freeacs.tr069.methods;

import com.github.freeacs.base.Log;
import com.github.freeacs.base.db.DBAccessSessionTR069;
import com.github.freeacs.dbi.UnittypeParameter;
import com.github.freeacs.dbi.UnittypeParameters;
import com.github.freeacs.dbi.util.ProvisioningMessage.ProvOutput;
import com.github.freeacs.dbi.util.ProvisioningMode;
import com.github.freeacs.tr069.HTTPReqResData;
import com.github.freeacs.tr069.SessionData;
import com.github.freeacs.tr069.exception.TR069DatabaseException;
import com.github.freeacs.tr069.xml.ParameterValueStruct;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GPVDecisionExtraction {

	/**
	 * Extraction mode will read all parameters from the device and write them to the
	 * unit_param_session table. No data will be written to unit_param table (provisioned data).
	 * 
	 * @param reqRes
	 * @throws TR069DatabaseException
	 * @throws SQLException
	 *
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
			DBAccessSessionTR069 dbAccessSessionTR069 = new DBAccessSessionTR069(reqRes.getDbAccess().getDBI().getAcs(), sessionData.getDbAccessSession());
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
