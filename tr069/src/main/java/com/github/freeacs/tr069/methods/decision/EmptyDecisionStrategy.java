package com.github.freeacs.tr069.methods.decision;

import com.github.freeacs.tr069.base.DBIActions;
import com.github.freeacs.dbi.DBI;
import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.ACSUnit;
import com.github.freeacs.dbi.util.SystemParameters;
import com.github.freeacs.dbi.util.TimestampWrapper;
import com.github.freeacs.tr069.http.HTTPRequestResponseData;
import com.github.freeacs.tr069.InformParameters;
import com.github.freeacs.tr069.Properties;
import com.github.freeacs.tr069.SessionData;
import com.github.freeacs.tr069.methods.ProvisioningMethod;
import com.github.freeacs.tr069.xml.ParameterValueStruct;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Slf4j
public class EmptyDecisionStrategy implements DecisionStrategy {
    private final DBI dbi;
    private final Properties properties;

    EmptyDecisionStrategy(Properties properties, DBI dbi) {
        this.properties = properties;
        this.dbi = dbi;
    }

    @Override
    public void makeDecision(HTTPRequestResponseData reqRes) throws Exception {
        SessionData sessionData = reqRes.getSessionData();
        String prevResponseMethod = sessionData.getPreviousResponseMethod();
        if (prevResponseMethod == null) {
            log.error("EM-Decision is EM since the CPE did not send an INFORM (or sessionId was not sent by client)");
            reqRes.getResponseData().setMethod(ProvisioningMethod.Empty.name());
        } else if (ProvisioningMethod.Empty.name().equals(prevResponseMethod)) {
            log.info("EM-Decision is EM since two last responses from CPE was EM");
            reqRes.getResponseData().setMethod(ProvisioningMethod.Empty.name());
        } else if (ProvisioningMethod.Inform.name().equals(prevResponseMethod)
                || ProvisioningMethod.TransferComplete.name().equals(prevResponseMethod)
                || ProvisioningMethod.GetRPCMethods.name().equals(prevResponseMethod)) {
            if (sessionData.getUnittype() == null) {
                log.info("EM-Decision is EM since unittype is not found");
                reqRes.getResponseData().setMethod(ProvisioningMethod.Empty.name());
            } else if (sessionData.discoverUnittype()) {
                writeSystemParameters(
                        reqRes,
                        Collections.singletonList(new ParameterValueStruct(SystemParameters.DISCOVER, "0")),
                        false);
                log.info("EM-Decision is GPN since unit has DISCOVER parameter set to 1");
                reqRes.getResponseData().setMethod(ProvisioningMethod.GetParameterNames.name());
            } else if (properties.isDiscoveryMode()
                    && !sessionData.isUnittypeCreated()
                    && sessionData.isFirstConnect()) {
                writeSystemParameters(reqRes, null, false);
                log.info("EM-Decision is GPN since ACS is in discovery-mode");
                reqRes.getResponseData().setMethod(ProvisioningMethod.GetParameterNames.name());
            } else if (properties.isDiscoveryMode()
                    && !sessionData.getUnittype().getUnittypeParameters().hasDeviceParameters()) {
                writeSystemParameters(reqRes);
                log.info("EM-Decision is GPN since ACS is in discovery-mode and no device parameters found");
                reqRes.getResponseData().setMethod(ProvisioningMethod.GetParameterNames.name());
            } else {
                writeSystemParameters(reqRes);
                log.info("EM-Decision is GPV since everything is normal and previous method was either IN or TC (updating LCT and possibly FCT)");
                reqRes.getResponseData().setMethod(ProvisioningMethod.GetParameterValues.name());
            }
        } else {
            log.info("EM-Decision is EM since it is the default method choice (nothing else fits)");
            reqRes.getResponseData().setMethod(ProvisioningMethod.Empty.name());
        }
    }

    private void writeSystemParameters(HTTPRequestResponseData reqRes) throws SQLException {
        writeSystemParameters(reqRes, null, true);
    }

    @SuppressWarnings("Duplicates")
    private void writeSystemParameters(
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
        DBIActions.writeUnitParams(sessionData); // queue-parameters - will be written at end-of-session
        if (!queue) { // execute changes immediately - since otherwise these parameters will be lost (in the event of GPNRes.process())
            dbi.getACSUnit().addOrChangeQueuedUnitParameters(sessionData.getUnit());
        }
        sessionData.setToDB(null);
    }
}
