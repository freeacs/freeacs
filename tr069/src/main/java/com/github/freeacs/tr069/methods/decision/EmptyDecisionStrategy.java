package com.github.freeacs.tr069.methods.decision;

import com.github.freeacs.base.Log;
import com.github.freeacs.base.db.DBAccess;
import com.github.freeacs.base.db.DBAccessSessionTR069;
import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.ACSUnit;
import com.github.freeacs.dbi.util.SystemParameters;
import com.github.freeacs.dbi.util.TimestampWrapper;
import com.github.freeacs.http.HTTPRequestResponseData;
import com.github.freeacs.tr069.InformParameters;
import com.github.freeacs.tr069.Properties;
import com.github.freeacs.tr069.SessionData;
import com.github.freeacs.tr069.methods.ProvisioningMethod;
import com.github.freeacs.tr069.xml.ParameterValueStruct;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class EmptyDecisionStrategy implements DecisionStrategy {
    private Properties properties;

    EmptyDecisionStrategy(Properties properties) {
        this.properties = properties;
    }

    @Override
    public void makeDecision(HTTPRequestResponseData reqRes) throws Exception {
        SessionData sessionData = reqRes.getSessionData();
        String prevResponseMethod = sessionData.getPreviousResponseMethod();
        if (prevResponseMethod == null) {
            Log.error(
                    EmptyDecisionStrategy.class,
                    "EM-Decision is EM since the CPE did not send an INFORM (or sessionId was not sent by client)");
            reqRes.getResponseData().setMethod(ProvisioningMethod.Empty.name());
        } else if (ProvisioningMethod.Empty.name().equals(prevResponseMethod)) {
            Log.info(EmptyDecisionStrategy.class, "EM-Decision is EM since two last responses from CPE was EM");
            reqRes.getResponseData().setMethod(ProvisioningMethod.Empty.name());
        } else if (ProvisioningMethod.Inform.name().equals(prevResponseMethod)
                || ProvisioningMethod.TransferComplete.name().equals(prevResponseMethod)
                || ProvisioningMethod.GetRPCMethodsResponse.name().equals(prevResponseMethod)) {
            if (sessionData.getUnittype() == null) {
                Log.info(EmptyDecisionStrategy.class, "EM-Decision is EM since unittype is not found");
                reqRes.getResponseData().setMethod(ProvisioningMethod.Empty.name());
            } else if (sessionData.discoverUnittype()) {
                writeSystemParameters(
                        reqRes,
                        Collections.singletonList(new ParameterValueStruct(SystemParameters.DISCOVER, "0")),
                        false);
                Log.info(EmptyDecisionStrategy.class, "EM-Decision is GPN since unit has DISCOVER parameter set to 1");
                reqRes.getResponseData().setMethod(ProvisioningMethod.GetParameterNames.name());
            } else if (properties.isDiscoveryMode()
                    && !sessionData.isUnittypeCreated()
                    && sessionData.isFirstConnect()) {
                writeSystemParameters(reqRes, null, false);
                Log.info(EmptyDecisionStrategy.class, "EM-Decision is GPN since ACS is in discovery-mode");
                reqRes.getResponseData().setMethod(ProvisioningMethod.GetParameterNames.name());
            } else if (properties.isDiscoveryMode()
                    && !sessionData.getUnittype().getUnittypeParameters().hasDeviceParameters()) {
                writeSystemParameters(reqRes);
                Log.info(
                        EmptyDecisionStrategy.class,
                        "EM-Decision is GPN since ACS is in discovery-mode and no device parameters found");
                reqRes.getResponseData().setMethod(ProvisioningMethod.GetParameterNames.name());
            } else {
                writeSystemParameters(reqRes);
                Log.info(
                        EmptyDecisionStrategy.class,
                        "EM-Decision is GPV since everything is normal and previous method was either IN or TC (updating LCT and possibly FCT)");
                reqRes.getResponseData().setMethod(ProvisioningMethod.GetParameterValues.name());
            }
        } else {
            Log.info(
                    EmptyDecisionStrategy.class,
                    "EM-Decision is EM since it is the default method choice (nothing else fits)");
            reqRes.getResponseData().setMethod(ProvisioningMethod.Empty.name());
        }
    }

    private static void writeSystemParameters(HTTPRequestResponseData reqRes) throws SQLException {
        writeSystemParameters(reqRes, null, true);
    }

    @SuppressWarnings("Duplicates")
    private static void writeSystemParameters(
            HTTPRequestResponseData reqRes, List<ParameterValueStruct> params, boolean queue) throws SQLException {
        SessionData sessionData = reqRes.getSessionData();
        List<ParameterValueStruct> toDB = new ArrayList<>();
        if (params != null) {
            toDB = new ArrayList<>(params);
        }
        String timestamp = TimestampWrapper.tmsFormat.format(new Date());
        toDB.add(new ParameterValueStruct(SystemParameters.LAST_CONNECT_TMS, timestamp));
        if (sessionData.getAcsParameters().getValue(SystemParameters.FIRST_CONNECT_TMS) == null) {
            toDB.add(new ParameterValueStruct(SystemParameters.FIRST_CONNECT_TMS, timestamp));
        }
        String currentIPAddress =
                sessionData.getUnit().getParameters().get(SystemParameters.IP_ADDRESS);
        String actualIPAddress = reqRes.getRealIPAddress();
        if (currentIPAddress == null || !currentIPAddress.equals(actualIPAddress)) {
            toDB.add(new ParameterValueStruct(SystemParameters.IP_ADDRESS, actualIPAddress));
        }
        String swVersion = sessionData.getUnit().getParameters().get(SystemParameters.SOFTWARE_VERSION);
        if (swVersion == null || !swVersion.equals(reqRes.getSessionData().getSoftwareVersion())) {
            toDB.add(
                    new ParameterValueStruct(
                            SystemParameters.SOFTWARE_VERSION, reqRes.getSessionData().getSoftwareVersion()));
        }
        String sn = sessionData.getUnit().getParameters().get(SystemParameters.SERIAL_NUMBER);
        if (sn == null || !sn.equals(sessionData.getSerialNumber())) {
            toDB.add(
                    new ParameterValueStruct(SystemParameters.SERIAL_NUMBER, sessionData.getSerialNumber()));
        }
        InformParameters ifmp = sessionData.getInformParameters();
        if (ifmp != null
                && ifmp.getCpeParams().get(ifmp.UDP_CONNECTION_URL) != null
                && ifmp.getCpeParams().get(ifmp.UDP_CONNECTION_URL).getValue() != null) {
            String udpUrl = ifmp.getCpeParams().get(ifmp.UDP_CONNECTION_URL).getValue();
            if (udpUrl != null
                    && !"".equals(udpUrl.trim())
                    && !udpUrl.equals(sessionData.getUnit().getParameterValue(ifmp.UDP_CONNECTION_URL))) {
                toDB.add(ifmp.getCpeParams().get(ifmp.UDP_CONNECTION_URL));
            }
        }
        sessionData.setToDB(toDB);
        DBAccessSessionTR069.writeUnitParams(
                sessionData); // queue-parameters - will be written at end-of-session
        if (!queue) { // execute changes immediately - since otherwise these parameters will be lost (in
            // the event of GPNRes.process())
            ACS acs = reqRes.getSessionData().getDbAccessSession().getAcs();
            ACSUnit acsUnit = DBAccess.getXAPSUnit(acs);
            acsUnit.addOrChangeQueuedUnitParameters(sessionData.getUnit());
        }
        sessionData.setToDB(null);
    }
}
