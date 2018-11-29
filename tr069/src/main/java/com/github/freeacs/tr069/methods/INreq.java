package com.github.freeacs.tr069.methods;

import com.github.freeacs.base.BaseCache;
import com.github.freeacs.base.JobLogic;
import com.github.freeacs.base.Log;
import com.github.freeacs.base.db.DBAccessSessionTR069;
import com.github.freeacs.dbi.Unit;
import com.github.freeacs.dbi.util.SystemParameters;
import com.github.freeacs.dbi.util.TimestampWrapper;
import com.github.freeacs.tr069.CPEParameters;
import com.github.freeacs.tr069.CommandKey;
import com.github.freeacs.http.HTTPRequestResponseData;
import com.github.freeacs.tr069.InformParameters;
import com.github.freeacs.tr069.ParameterKey;
import com.github.freeacs.tr069.SessionData;
import com.github.freeacs.tr069.background.ScheduledKickTask;
import com.github.freeacs.tr069.exception.TR069DatabaseException;
import com.github.freeacs.tr069.exception.TR069Exception;
import com.github.freeacs.tr069.exception.TR069ExceptionShortMessage;
import com.github.freeacs.tr069.xml.DeviceIdStruct;
import com.github.freeacs.tr069.xml.EventList;
import com.github.freeacs.tr069.xml.EventStruct;
import com.github.freeacs.tr069.xml.Header;
import com.github.freeacs.tr069.xml.ParameterList;
import com.github.freeacs.tr069.xml.ParameterValueStruct;
import com.github.freeacs.tr069.xml.Parser;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.lang3.StringUtils;

public class INreq {
  private static void parseEvents(Parser parser, SessionData sessionData) {
    EventList eventList = parser.getEventList();
    Set<Integer> eventCodeIntSet = new TreeSet<>();
    Set<String> eventCodeStrSet = new HashSet<>();

    sessionData.setCommandKey(new CommandKey());
    for (int i = 0; eventList != null && i < eventList.getEventList().size(); i++) {
      EventStruct es = eventList.getEventList().get(i);
      String[] tmpArr = es.getEventCode().split(" ");
      try {
        eventCodeIntSet.add(Integer.parseInt(tmpArr[0]));
      } catch (NumberFormatException nfe) {
        eventCodeStrSet.add(tmpArr[0]);
      }
      // if (es.getEventCode().startsWith("0")) sessionData.setFactoryReset(true);
      // if (es.getEventCode().startsWith("1")) sessionData.setBooted(true);
      if (es.getEventCode().startsWith("2")) {
        sessionData.setPeriodic(true);
      }
      // if (es.getEventCode().startsWith("4")) sessionData.setValueChange(true);
      // if (es.getEventCode().startsWith("6")) sessionData.setKicked(true);
      // if (es.getEventCode().startsWith("8")) sessionData.setDiagnosticsComplete(true);
      // This is a quick-and-easy impl. since, there can potentially be more than
      // one CommandKey. However, I don't think this will be the case in practice. (Morten May 2012)
      // TODO: This is surely not correct - Morten Jul 2012
      if (es.getCommandKey() != null && !"".equals(es.getCommandKey().trim())) {
        sessionData.getCommandKey().setCpeKey(es.getCommandKey());
      }
    }
    String eventCodes = StringUtils.join(eventCodeIntSet.iterator(), ",");
    if (!eventCodeStrSet.isEmpty()) {
      eventCodes += "," + StringUtils.join(eventCodeStrSet.iterator(), ",");
    }
    if (!eventCodes.isEmpty()) {
      sessionData.setEventCodes(eventCodes);
    }
  }

  private static String getUnitId(DeviceIdStruct deviceIdStruct)
      throws UnsupportedEncodingException {
    String unitId;
    if (deviceIdStruct.getProductClass() != null
        && !"".equals(deviceIdStruct.getProductClass().trim())) {
      unitId =
          deviceIdStruct.getOui()
              + "-"
              + deviceIdStruct.getProductClass()
              + "-"
              + deviceIdStruct.getSerialNumber();
    } else {
      unitId = deviceIdStruct.getOui() + "-" + deviceIdStruct.getSerialNumber();
    }
    return URLDecoder.decode(unitId, "UTF-8");
  }

  private static void parseParameters(SessionData sessionData, Parser parser)
      throws TR069Exception {
    ParameterList parameterList = parser.getParameterList();
    List<ParameterValueStruct> parameterValues = parameterList.getParameterValueList();
    String keyRoot = sessionData.getKeyRoot();
    CPEParameters cpeParams = null;
    InformParameters informParams = null;
    ParameterKey pk = new ParameterKey();
    sessionData.setParameterKey(pk);
    for (ParameterValueStruct pvs : parameterValues) {
      if (sessionData.getKeyRoot() == null) {
        String paramValue = pvs.getName();
        int keyRootEndPos = paramValue.indexOf('.');
        keyRoot = paramValue.substring(0, keyRootEndPos + 1);
        if ("Device.".equals(keyRoot) || "InternetGatewayDevice.".equals(keyRoot)) {
          sessionData.setKeyRoot(keyRoot);
          cpeParams = new CPEParameters(keyRoot);
          sessionData.setCpeParameters(cpeParams);
          informParams = new InformParameters(keyRoot);
          sessionData.setInformParameters(informParams);
        }
      }
      if (cpeParams != null) {
        if (pvs.getName().equals(cpeParams.SOFTWARE_VERSION)) {
          cpeParams.putPvs(cpeParams.SOFTWARE_VERSION, pvs);
          sessionData.setSoftwareVersion(pvs.getValue());
        }
        if (pvs.getName().equals(cpeParams.CONNECTION_URL)) {
          cpeParams.putPvs(cpeParams.CONNECTION_URL, pvs);
        }
        if (pvs.getName().equals(informParams.UDP_CONNECTION_URL)) {
          informParams.putPvs(informParams.UDP_CONNECTION_URL, pvs);
        }
      }
      if (pvs.getName().contains("ParameterKey")) {
        pk.setCpeKey(pvs.getValue());
      }
    }
    if (keyRoot == null) {
      throw new TR069Exception(
          "Parsed INreq params, but no keyroot could be found, most likely because no parameters were sent in ParameterList",
          TR069ExceptionShortMessage.MISC);
    } else {
      String msg =
          "Parsed INreq params, found keyroot:" + keyRoot + ", parameterkey:" + pk.getCpeKey();
      msg +=
          ", swver:"
              + (cpeParams == null ? "Unknown" : cpeParams.getValue(cpeParams.SOFTWARE_VERSION));
      Log.debug(INreq.class, msg);
    }
  }

  private static void logPeriodicInformTiming(SessionData sessionData) {
    try {
      if (sessionData.getUnit() != null && sessionData.isPeriodic()) {
        Unit unit = sessionData.getUnit();
        if (unit.getUnitParameters() != null) {
          String PII = unit.getParameterValue(SystemParameters.PERIODIC_INTERVAL, false);
          String LCT = unit.getParameterValue(SystemParameters.LAST_CONNECT_TMS, false);
          if (PII != null && LCT != null) {
            long shouldConnectTms =
                TimestampWrapper.tmsFormat.parse(LCT).getTime() + Integer.parseInt(PII) * 1000;
            long diff = System.currentTimeMillis() - shouldConnectTms;
            if (diff > -5000 && diff < 5000) {
              Log.info(
                  INreq.class,
                  "Periodic Inform recorded on time   ("
                      + diff / 1000
                      + " sec). Deviation: "
                      + (diff / 10) / Integer.parseInt(PII)
                      + " %");
            } else if (diff >= 5000) {
              Log.info(
                  INreq.class,
                  "Periodic Inform recorded too late  ("
                      + diff / 1000
                      + " sec). Deviation: "
                      + (diff / 10) / Integer.parseInt(PII)
                      + " %");
            } else {
              Log.info(
                  INreq.class,
                  "Periodic Inform recorded too early ("
                      + diff / 1000
                      + " sec). Deviation: "
                      + (diff / 10) / Integer.parseInt(PII)
                      + " %");
            }
          }
        }
      }
    } catch (Throwable t) {
      Log.warn(
          INreq.class, "LogPeriodicInformTiming failed - no consequence for provisioning: ", t);
    }
  }

  public static void process(HTTPRequestResponseData reqRes, boolean isDiscoveryMode) throws TR069Exception {
    try {
      reqRes.getRequestData().setMethod(TR069Method.INFORM);
      Parser parser = new Parser(reqRes.getRequestData().getXml());
      SessionData sessionData = reqRes.getSessionData();
      Header header = parser.getHeader();
      reqRes.setTR069TransactionID(header.getId());
      DeviceIdStruct deviceIdStruct = parser.getDeviceIdStruct();
      // If unit is authenticated, the unitId is already found
      String unitId = sessionData.getUnitId();
      if (unitId == null) {
        unitId = getUnitId(deviceIdStruct);
      }
      BaseCache.putSessionData(unitId, sessionData);
      sessionData.setUnitId(unitId);
      sessionData.setSerialNumber(deviceIdStruct.getSerialNumber());
      parseEvents(parser, sessionData);
      parseParameters(sessionData, parser);
      sessionData.updateParametersFromDB(
          unitId, isDiscoveryMode); // Unit-object is read and populated in SessionData
      logPeriodicInformTiming(sessionData);
      ScheduledKickTask.removeUnit(unitId);
      if (isDiscoveryMode && sessionData.isFirstConnect()) {
        DBAccessSessionTR069 dbAccessSessionTR069 =
            new DBAccessSessionTR069(
                reqRes.getDbAccess().getDBI().getAcs(), sessionData.getDbAccessSession());
        dbAccessSessionTR069.writeUnittypeProfileUnit(
            sessionData, deviceIdStruct.getProductClass(), unitId);
        sessionData.setFromDB(null);
        sessionData.setAcsParameters(null);
        sessionData.updateParametersFromDB(unitId, isDiscoveryMode);
        Log.debug(
            INreq.class,
            "Unittype, profile and unit is created, since discovery mode is enabled and this is the first connect");
      }
      sessionData.getCommandKey().setServerKey(reqRes);
      sessionData.getParameterKey().setServerKey(reqRes);
      boolean jobOk = JobLogic.checkJobOK(sessionData, isDiscoveryMode);
      sessionData.setJobUnderExecution(!jobOk);
    } catch (SQLException e) {
      throw new TR069DatabaseException(e);
    } catch (UnsupportedEncodingException uee) {
      throw new TR069Exception(
          "Not possible to decode the Unit id", TR069ExceptionShortMessage.MISC, uee);
    } catch (NoSuchAlgorithmException e) {
      throw new TR069Exception(
          "Not possible to make a parameter key", TR069ExceptionShortMessage.MISC, e);
    }
  }
}
