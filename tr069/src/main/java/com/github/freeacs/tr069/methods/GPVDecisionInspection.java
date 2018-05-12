package com.github.freeacs.tr069.methods;


public class GPVDecisionInspection {
//	protected static void processInspection(HTTPReqResData reqRes) throws TR069DatabaseException {
//		SessionData sessionData = reqRes.getSessionData();
//		Unit u = sessionData.getUnit();
//		ProvisioningState state = u.getProvisioningState();
//		try {
//			populateToCollections(sessionData);
//			if (state == ProvisioningState.LOADING) {
//				Unittype unittype = sessionData.getUnittype();
//				UnittypeParameter utp = unittype.getUnittypeParameters().getByName(SystemParameters.PROVISIONING_STATE);
//				List<UnitParameter> unitParameters = new ArrayList<UnitParameter>();
//				UnitParameter up = new UnitParameter(utp, sessionData.getUnitId(), ProvisioningState.READY.toString(), sessionData.getProfile());
//				unitParameters.add(up);
//				Log.debug(GPVDecision.class, "Reset from " + u.getProvisioningState() + " to READY state");
//				DBAccessStatic.queueUnitParameters(u, unitParameters, sessionData.getProfile());
//				Log.debug(GPVDecision.class, "Write changes to ACS");
//				DBAccessSessionTR069 dbAccessSessionTR069 = new DBAccessSessionTR069(DBAccess.getDBI(), sessionData.getDbAccess());
//				dbAccessSessionTR069.writeUnitSessionParams(sessionData);
//				reqRes.getResponse().setMethod(TR069Method.EMPTY);
//				sessionData.getProvisioningMessage().setProvOutput(ProvOutput.EMPTY);
//			} else if (state == ProvisioningState.STORING) {
//				DBAccessSessionTR069 dbAccessSessionTR069 = new DBAccessSessionTR069(DBAccess.getDBI(), sessionData.getDbAccess());
//				dbAccessSessionTR069.writeUnitSessionParams(sessionData);
//				reqRes.getResponse().setMethod(TR069Method.SET_PARAMETER_VALUES);
//				sessionData.getProvisioningMessage().setProvOutput(ProvOutput.CONFIG);
//			} else {
//				reqRes.getResponse().setMethod(TR069Method.EMPTY);
//				sessionData.getProvisioningMessage().setProvOutput(ProvOutput.EMPTY);
//			}
//		} catch (SQLException sqle) {
//			throw new TR069DatabaseException(sqle);
//		}
//	}
//
//	private static String msg(UnittypeParameter utp, String cpeValue, String acsValue, String action, String cause) {
//		if (utp.getFlag().isConfidential())
//			return "-" + String.format("%-15s", action) + utp.getName() + " CPE:[*****] ACS:[*****] Flags:[" + utp.getFlag().toString() + "] Cause:[" + cause + "]";
//		else
//			return "-" + String.format("%-15s", action) + utp.getName() + " CPE:[" + cpeValue + "] ACS:[" + acsValue + "] Flags:[" + utp.getFlag().toString() + "] Cause:[" + cause + "]";
//	}
//
//	private static void populateToCollections(SessionData sessionData) throws SQLException, NoAvailableConnectionException {
//		UnittypeParameters utps = sessionData.getUnittype().getUnittypeParameters();
//		ParameterList toCPE = new ParameterList();
//		List<ParameterValueStruct> toDB = new ArrayList<ParameterValueStruct>();
//		Unit unit = sessionData.getUnit();
//		ProvisioningState state = unit.getProvisioningState();
//		if (state == ProvisioningState.LOADING)
//			Log.info(GPVDecisionInspection.class, "Provisioning in INSPECTION mode and " + state + " state, " + sessionData.getFromCPE().size() + " params from CPE may be copied to ACS session storage");
//		else if (state == ProvisioningState.STORING)
//			Log.info(GPVDecisionInspection.class, "Provisioning in INSPECTION mode and " + state + " state, " + sessionData.getFromCPE().size() + " params maybe exchanged between CPE and ACS");
//		for (int i = 0; i < sessionData.getFromCPE().size(); i++) {
//			ParameterValueStruct pvsCPE = sessionData.getFromCPE().get(i);
//			UnittypeParameter utp = utps.getByName(pvsCPE.getName());
//			if (utp == null) {
//				Log.debug(GPVDecisionInspection.class, "The parameter name " + pvsCPE.getName() + " was not recognized in ACS, could happen if a GPV on all params was issued.");
//				continue;
//			}
//			if (state == ProvisioningState.LOADING)
//				toDB.add(pvsCPE);
//			else if (state == ProvisioningState.STORING) {
//				ParameterValueStruct pvsDB = (ParameterValueStruct) sessionData.getFromDB().get(pvsCPE.getName());
//				String cpeV = pvsCPE.getValue();
//				String acsV = null;
//				if (pvsDB != null)
//					acsV = pvsDB.getValue();
//				if (acsV != null && !acsV.equals(cpeV)) {
//					if (utp.getFlag().isReadWrite()) {
//						Log.debug(GPVDecision.class, msg(utp, cpeV, acsV, "ACS->CPE", "Param has ReadWrite-flag"));
//						pvsDB.setType(pvsCPE.getType());
//						toCPE.addParameterValueStruct(pvsDB);
//					}
//					toDB.add(pvsCPE);
//				}
//			}
//		}
//		sessionData.setToCPE(toCPE);
//		sessionData.setToDB(toDB);
//		Log.debug(GPVDecisionInspection.class, toCPE.getParameterValueList().size() + " params to CPE, " + toDB.size() + " params to ACS");
//	}
}
