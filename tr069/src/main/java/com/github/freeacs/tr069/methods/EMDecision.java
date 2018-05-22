package com.github.freeacs.tr069.methods;

import com.github.freeacs.base.Log;
import com.github.freeacs.base.db.DBAccess;
import com.github.freeacs.base.db.DBAccessSessionTR069;
import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.Unit;
import com.github.freeacs.dbi.ACSUnit;
import com.github.freeacs.dbi.tr069.TestCase;
import com.github.freeacs.dbi.tr069.TestDB;
import com.github.freeacs.dbi.util.ProvisioningMode;
import com.github.freeacs.dbi.util.SystemParameters;
import com.github.freeacs.dbi.util.TimestampWrapper;
import com.github.freeacs.tr069.HTTPReqResData;
import com.github.freeacs.tr069.InformParameters;
import com.github.freeacs.tr069.Properties;
import com.github.freeacs.tr069.SessionData;
import com.github.freeacs.tr069.test.system1.TestDatabase;
import com.github.freeacs.tr069.test.system1.TestDatabaseObject;
import com.github.freeacs.tr069.test.system2.TestUnit;
import com.github.freeacs.tr069.test.system2.TestUnitCache;
import com.github.freeacs.tr069.test.system2.Util;
import com.github.freeacs.tr069.xml.ParameterValueStruct;

import java.sql.SQLException;
import java.util.*;

/**
 * EMDecision has to decide what to do after an EM-request. The rules are:
 * 
 * 1. If previous response from server was EM, then we are at the end of the conversation, thus return EM (only true if HoldRequests is set to 1)
 * 2. If the conversation was kicked and provisioning mode is not set accordingly, return EM (end conv.)
 * 3. If the conversation was not kicked and provisioning mode is not AUTO, return EM (end. conv.)
 * 4. If previous response from server was IN or TC, then return either GPN or GPV (depending upon a flag)
 * 5. Else return EM
 * @author Morten
 *
 */

/**
 * If a device is in INSPECTION mode, it should only allow kicked or booted
 * traffic, not regular periodic traffic. If a device is in  mode, all ways
 * of initiating the traffic is accepted, but will be treated as regular AUTO
 * provisioning.
 * 
 */

public class EMDecision {

	private static boolean testExecution(HTTPReqResData reqRes) {
		SessionData sessionData = reqRes.getSessionData();
		Unit unit = sessionData.getUnit();
		if (Util.testEnabled(reqRes, false)) {
			// TODO:TF - initiate test - completed
			reqRes.getSessionData().setTestMode(true); // This only last for one TR-069 session, not for the entire test (many test-cases and steps)
			TestUnit tu = TestUnitCache.get(sessionData.getUnitId());
			try {
				if (tu == null) {
					TestDB testDB = new TestDB(sessionData.getDbAccessSession().getAcs());
					List<TestCase> testCases = testDB.getCompleteTestCases(sessionData.getUnittype(), Util.getTestCaseMethod(unit), Util.getParamFilter(unit), Util.getTagFilter(unit));
					tu = new TestUnit(sessionData.getUnittype(), sessionData.getUnit(), testCases);
					TestUnitCache.put(sessionData.getUnitId(), tu);
					Log.notice(EMDecision.class, "A Test session has been initiated -  " + testCases.size() + " will be tested");
				}
				tu.next(); // Responsible for updating TestState
				if (tu.getTestState() == TestUnit.TestState.ENDTEST) {
					Log.notice(EMDecision.class, "A Test session has been completed - will return to " + ProvisioningMode.REGULAR + " provisioning");
					Log.info(EMDecision.class, "EM-Decision is " + TR069Method.EMPTY);
					Util.testDisable(reqRes);
					TestUnitCache.remove(tu.getUnit().getId());
					reqRes.getResponse().setMethod(TR069Method.EMPTY);

				} else {
					String tr069Method = Util.step2TR069Method(tu.getCurrentStep(), Util.getTestCaseMethod(tu.getUnit()));
					Log.info(EMDecision.class, "EM-Decision is " + tr069Method + " since ACS is in test-mode (new type) and test-state is " + tu.getTestState() + " and case-step is "
							+ tu.getCurrentCase().getId() + "," + tu.getCurrentStep());
					reqRes.getResponse().setMethod(tr069Method);
				}
			} catch (Exception te) {
				Log.error(EMDecision.class, "Test aborted", te);
				reqRes.getResponse().setMethod(TR069Method.EMPTY);
			}
			return true;
		} else {
			String row = TestDatabase.database.select(sessionData.getUnitId());
			if (row != null) {
				// TR069 Plugfest/Torture Test
				TestDatabaseObject tdo = new TestDatabaseObject(row);
				if (tdo.getRun().equals("true")) {
					reqRes.getSessionData().setTestMode(true);
					Log.info(EMDecision.class, "EM-Decision is CU since ACS is in test-mode and unitId is found in test-database and run=true");
					reqRes.getResponse().setMethod(TR069Method.CUSTOM);
					return true;
				}
			}
		}
		return false;
	}

	private static void writeSystemParameters(HTTPReqResData reqRes) throws SQLException {
		writeSystemParameters(reqRes, null, true);
	}

	private static void writeSystemParameters(HTTPReqResData reqRes, List<ParameterValueStruct> params, boolean queue) throws SQLException {
		SessionData sessionData = reqRes.getSessionData();
		List<ParameterValueStruct> toDB = new ArrayList<ParameterValueStruct>();
		if (params != null)
			toDB = new ArrayList<ParameterValueStruct>(params);
		String timestamp = TimestampWrapper.tmsFormat.format(new Date());
		toDB.add(new ParameterValueStruct(SystemParameters.LAST_CONNECT_TMS, timestamp));
		if (sessionData.getAcsParameters().getValue(SystemParameters.FIRST_CONNECT_TMS) == null)
			toDB.add(new ParameterValueStruct(SystemParameters.FIRST_CONNECT_TMS, timestamp));
		String ipAddress = sessionData.getUnit().getParameters().get(SystemParameters.IP_ADDRESS);
		if (ipAddress == null || !ipAddress.equals(reqRes.getReq().getRemoteHost()))
			toDB.add(new ParameterValueStruct(SystemParameters.IP_ADDRESS, reqRes.getReq().getRemoteHost()));
		String swVersion = sessionData.getUnit().getParameters().get(SystemParameters.SOFTWARE_VERSION);
		if (swVersion == null || !swVersion.equals(reqRes.getSessionData().getSoftwareVersion()))
			toDB.add(new ParameterValueStruct(SystemParameters.SOFTWARE_VERSION, reqRes.getSessionData().getSoftwareVersion()));
		//		String serialNumber = sessionData.getUnitId().substring(sessionData.getUnitId().lastIndexOf("-") + 1);
		//		String mac = sessionData.getUnit().getParameters().get(SystemParameters.MAC);
		//		if (mac == null || !mac.equals(serialNumber))
		//		toDB.add(new ParameterValueStruct(SystemParameters.MAC, serialNumber));
		String sn = sessionData.getUnit().getParameters().get(SystemParameters.SERIAL_NUMBER);
		if (sn == null || !sn.equals(sessionData.getSerialNumber()))
			toDB.add(new ParameterValueStruct(SystemParameters.SERIAL_NUMBER, sessionData.getSerialNumber()));
		InformParameters ifmp = sessionData.getInformParameters();
		if (ifmp != null && ifmp.getPvs(ifmp.UDP_CONNECTION_URL) != null && ifmp.getPvs(ifmp.UDP_CONNECTION_URL).getValue() != null) {
			String udpUrl = ifmp.getPvs(ifmp.UDP_CONNECTION_URL).getValue();
			if (udpUrl != null && !udpUrl.trim().equals("") && !udpUrl.equals(sessionData.getUnit().getParameterValue(ifmp.UDP_CONNECTION_URL))) {
				toDB.add(ifmp.getPvs(ifmp.UDP_CONNECTION_URL));
			}
		}
		sessionData.setToDB(toDB);
		DBAccessSessionTR069.writeUnitParams(sessionData); // queue-parameters - will be written at end-of-session
		if (!queue) { // execute changes immediately - since otherwise these parameters will be lost (in the event of GPNRes.process())
			ACS acs = reqRes.getSessionData().getDbAccessSession().getAcs();
			ACSUnit acsUnit = DBAccess.getXAPSUnit(acs);
			acsUnit.addOrChangeQueuedUnitParameters(sessionData.getUnit());
		}
		sessionData.setToDB(null);
	}

	public static void process(HTTPReqResData reqRes) throws SQLException {
		SessionData sessionData = reqRes.getSessionData();
		String prevResponseMethod = sessionData.getPreviousResponseMethod();
		if (prevResponseMethod == null) {
			Log.error(EMDecision.class, "EM-Decision is EM since the CPE did not send an INFORM (or sessionId was not sent by client)");
			reqRes.getResponse().setMethod(TR069Method.EMPTY);
		} else if (prevResponseMethod.equals(TR069Method.EMPTY)) {
			Log.info(EMDecision.class, "EM-Decision is EM since two last responses from CPE was EM");
			reqRes.getResponse().setMethod(TR069Method.EMPTY);
		} else if (prevResponseMethod.equals(TR069Method.INFORM) || prevResponseMethod.equals(TR069Method.TRANSFER_COMPLETE) || prevResponseMethod.equals(TR069Method.GET_RPC_METHODS_RES)) {
			if (sessionData.getUnittype() == null) {
				Log.info(EMDecision.class, "EM-Decision is EM since unittype is not found");
				reqRes.getResponse().setMethod(TR069Method.EMPTY);
			} else if (Properties.DEBUG_TEST_MODE && testExecution(reqRes)) {
				writeSystemParameters(reqRes);
            } else if (sessionData.discoverUnittype()) {
				writeSystemParameters(reqRes, Collections.singletonList(new ParameterValueStruct(SystemParameters.DISCOVER, "0")), false);
				Log.info(EMDecision.class, "EM-Decision is GPN since unit has DISCOVER parameter set to 1");
				reqRes.getResponse().setMethod(TR069Method.GET_PARAMETER_NAMES);
			} else if ((Properties.DISCOVERY_MODE && !sessionData.isUnittypeCreated() && sessionData.isFirstConnect())) {
				writeSystemParameters(reqRes, null, false);
				Log.info(EMDecision.class, "EM-Decision is GPN since ACS is in discovery-mode");
				reqRes.getResponse().setMethod(TR069Method.GET_PARAMETER_NAMES);
			} else if (Properties.DISCOVERY_MODE && !sessionData.getUnittype().getUnittypeParameters().hasDeviceParameters()) {
				writeSystemParameters(reqRes);
				Log.info(EMDecision.class, "EM-Decision is GPN since ACS is in discovery-mode and no device parameters found");
				reqRes.getResponse().setMethod(TR069Method.GET_PARAMETER_NAMES);
			} else {
				writeSystemParameters(reqRes);
				Log.info(EMDecision.class, "EM-Decision is GPV since everything is normal and previous method was either IN or TC (updating LCT and possibly FCT)");
				reqRes.getResponse().setMethod(TR069Method.GET_PARAMETER_VALUES);
			}
		} else {
			Log.info(EMDecision.class, "EM-Decision is EM since it is the default method choice (nothing else fits)");
			reqRes.getResponse().setMethod(TR069Method.EMPTY);
		}
	}
}
