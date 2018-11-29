package com.github.freeacs.tr069.methods;

import com.github.freeacs.base.Log;
import com.github.freeacs.base.db.DBAccess;
import com.github.freeacs.base.db.DBAccessSessionTR069;
import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.ACSUnit;
import com.github.freeacs.dbi.util.SystemParameters;
import com.github.freeacs.dbi.util.TimestampWrapper;
import com.github.freeacs.tr069.HTTPRequestResponseData;
import com.github.freeacs.tr069.InformParameters;
import com.github.freeacs.tr069.SessionData;
import com.github.freeacs.tr069.xml.ParameterValueStruct;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/*
 * EMDecision has to decide what to do after an EM-request. The rules are:
 *
 * <p>1. If previous response from server was EM, then we are at the end of the conversation, thus
 * return EM (only true if HoldRequests is set to 1) 2. If the conversation was kicked and
 * provisioning mode is not set accordingly, return EM (end conv.) 3. If the conversation was not
 * kicked and provisioning mode is not AUTO, return EM (end. conv.) 4. If previous response from
 * server was IN or TC, then return either GPN or GPV (depending upon a flag) 5. Else return EM
 *
 * @author Morten
 */

/**
 * If a device is in INSPECTION mode, it should only allow kicked or booted traffic, not regular
 * periodic traffic. If a device is in mode, all ways of initiating the traffic is accepted, but
 * will be treated as regular AUTO provisioning.
 */
public class EMDecision {
  private static void writeSystemParameters(HTTPRequestResponseData reqRes) throws SQLException {
    writeSystemParameters(reqRes, null, true);
  }

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
        && ifmp.getPvs(ifmp.UDP_CONNECTION_URL) != null
        && ifmp.getPvs(ifmp.UDP_CONNECTION_URL).getValue() != null) {
      String udpUrl = ifmp.getPvs(ifmp.UDP_CONNECTION_URL).getValue();
      if (udpUrl != null
          && !"".equals(udpUrl.trim())
          && !udpUrl.equals(sessionData.getUnit().getParameterValue(ifmp.UDP_CONNECTION_URL))) {
        toDB.add(ifmp.getPvs(ifmp.UDP_CONNECTION_URL));
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

  public static void process(HTTPRequestResponseData reqRes, boolean isDiscoveryMode) throws SQLException {
    SessionData sessionData = reqRes.getSessionData();
    String prevResponseMethod = sessionData.getPreviousResponseMethod();
    if (prevResponseMethod == null) {
      Log.error(
          EMDecision.class,
          "EM-Decision is EM since the CPE did not send an INFORM (or sessionId was not sent by client)");
      reqRes.getResponseData().setMethod(TR069Method.EMPTY);
    } else if (TR069Method.EMPTY.equals(prevResponseMethod)) {
      Log.info(EMDecision.class, "EM-Decision is EM since two last responses from CPE was EM");
      reqRes.getResponseData().setMethod(TR069Method.EMPTY);
    } else if (TR069Method.INFORM.equals(prevResponseMethod)
        || TR069Method.TRANSFER_COMPLETE.equals(prevResponseMethod)
        || TR069Method.GET_RPC_METHODS_RES.equals(prevResponseMethod)) {
      if (sessionData.getUnittype() == null) {
        Log.info(EMDecision.class, "EM-Decision is EM since unittype is not found");
        reqRes.getResponseData().setMethod(TR069Method.EMPTY);
      } else if (sessionData.discoverUnittype()) {
        writeSystemParameters(
            reqRes,
            Collections.singletonList(new ParameterValueStruct(SystemParameters.DISCOVER, "0")),
            false);
        Log.info(EMDecision.class, "EM-Decision is GPN since unit has DISCOVER parameter set to 1");
        reqRes.getResponseData().setMethod(TR069Method.GET_PARAMETER_NAMES);
      } else if (isDiscoveryMode
          && !sessionData.isUnittypeCreated()
          && sessionData.isFirstConnect()) {
        writeSystemParameters(reqRes, null, false);
        Log.info(EMDecision.class, "EM-Decision is GPN since ACS is in discovery-mode");
        reqRes.getResponseData().setMethod(TR069Method.GET_PARAMETER_NAMES);
      } else if (isDiscoveryMode
          && !sessionData.getUnittype().getUnittypeParameters().hasDeviceParameters()) {
        writeSystemParameters(reqRes);
        Log.info(
            EMDecision.class,
            "EM-Decision is GPN since ACS is in discovery-mode and no device parameters found");
        reqRes.getResponseData().setMethod(TR069Method.GET_PARAMETER_NAMES);
      } else {
        writeSystemParameters(reqRes);
        Log.info(
            EMDecision.class,
            "EM-Decision is GPV since everything is normal and previous method was either IN or TC (updating LCT and possibly FCT)");
        reqRes.getResponseData().setMethod(TR069Method.GET_PARAMETER_VALUES);
      }
    } else {
      Log.info(
          EMDecision.class,
          "EM-Decision is EM since it is the default method choice (nothing else fits)");
      reqRes.getResponseData().setMethod(TR069Method.EMPTY);
    }
  }
}
