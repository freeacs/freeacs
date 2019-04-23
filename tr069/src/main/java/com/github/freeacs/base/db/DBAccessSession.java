package com.github.freeacs.base.db;

import com.github.freeacs.base.ACSParameters;
import com.github.freeacs.base.Log;
import com.github.freeacs.base.NoDataAvailableException;
import com.github.freeacs.base.SessionDataI;
import com.github.freeacs.dbi.*;
import com.github.freeacs.tr069.SessionData;
import com.github.freeacs.tr069.xml.ParameterValueStruct;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class DBAccessSession {
  private final ACS acs;

  public DBAccessSession(ACS acs) {
    this.acs = acs;
  }

  public void updateParametersFromDB(SessionData sessionData, boolean isDiscoveryMode) throws SQLException {
    if (sessionData.getFromDB() != null) {
      return;
    }

    Log.debug(SessionData.class, "Will load unit data");
    addUnitDataToSession(sessionData);

    if (sessionData.getFromDB().isEmpty()) {
      if (isDiscoveryMode) {
        Log.debug(
                SessionData.class,
                "No unit data found & discovery mode true -> first-connect = true, allow to continue");
        sessionData.setFirstConnect(true);
      } else {
        throw new NoDataAvailableException();
      }
    }

    if (!sessionData.getFromDB().isEmpty()) {
      if (sessionData.getAcsParameters() == null) {
        sessionData.setAcsParameters(new ACSParameters());
      }
      Iterator<String> i = sessionData.getFromDB().keySet().iterator();
      int systemParamCounter = 0;
      while (i.hasNext()) {
        String utpName = i.next();
        UnittypeParameter utp = sessionData.getUnittype().getUnittypeParameters().getByName(utpName);
        if (utp != null && utp.getFlag().isSystem()) {
          systemParamCounter++;
          sessionData.getAcsParameters().putPvs(utpName, sessionData.getFromDB().get(utpName));
          i.remove();
        }
      }
      Log.debug(
              SessionData.class,
              "Removed "
                      + systemParamCounter
                      + " system parameter from param-list, now contains "
                      + sessionData.getFromDB().size()
                      + " params");
    }
  }

  private void addUnitDataToSession(SessionData sessionData) throws SQLException {
    Unit unit = readUnit(sessionData.getUnitId());
    Map<String, ParameterValueStruct> valueMap = new TreeMap<>();
    if (unit != null) {
      sessionData.setUnit(unit);
      sessionData.setUnittype(unit.getUnittype());
      sessionData.setProfile(unit.getProfile());
      ProfileParameter[] pparams = unit.getProfile().getProfileParameters().getProfileParameters();
      for (ProfileParameter pp : pparams) {
        String utpName = pp.getUnittypeParameter().getName();
        valueMap.put(utpName, new ParameterValueStruct(utpName, pp.getValue()));
      }
      int overrideCounter = 0;
      for (Map.Entry<String, UnitParameter> entry : unit.getUnitParameters().entrySet()) {
        if (!entry.getValue().getParameter().valueWasNull()) {
          String utpName = entry.getKey();
          String value = entry.getValue().getValue();
          ParameterValueStruct pvs = new ParameterValueStruct(utpName, value);
          if (valueMap.containsKey(utpName)) {
            overrideCounter++;
          }
          valueMap.put(utpName, pvs);
        } else {
          System.out.println(entry.getKey() + " is probably a session-parameter");
        }
      }
      int alwaysCounter = 0;
      for (Map.Entry<Integer, UnittypeParameter> entry :
              unit.getUnittype().getUnittypeParameters().getAlwaysMap().entrySet()) {
        String utpName = entry.getValue().getName();
        if (!valueMap.containsKey(utpName)) {
          alwaysCounter++;
          valueMap.put(utpName, new ParameterValueStruct(utpName, ""));
        }
      }
      String msg = "Found unit in database - in total " + valueMap.size() + " params ";
      msg += "(" + unit.getUnitParameters().size() + " unit params, ";
      msg += pparams.length + " profile params (" + overrideCounter + " overridden), ";
      msg += alwaysCounter + " always read params added)";
      Log.debug(SessionData.class, msg);
    } else {
      Log.warn(SessionData.class, "Did not find unit in unit-table, nothing exists on this unit");
    }
    sessionData.setFromDB(valueMap);
  }

  public void writeUnittypeParameters(SessionDataI sessionData, List<UnittypeParameter> utpList) throws SQLException {
    try {
      Unittype ut = sessionData.getUnittype();
      ut.getUnittypeParameters().addOrChangeUnittypeParameters(utpList, acs);
      Log.debug(DBAccessSession.class, "Have written " + utpList.size() + " unittype parameters");
    } catch (Throwable t) {
      DBAccessErrorHandler.handleError("writeUnittypeParameters", t);
    }
  }

  Unit readUnit(String unitId) throws SQLException {
    Unit unit;
    try {
      ACSUnit acsUnit = new ACSUnit(acs.getDataSource(), acs, acs.getSyslog());
      unit = acsUnit.getUnitById(unitId);
      if (unit != null) {
        Log.debug(
            DBAccessSession.class,
            "Found unit "
                + unit.getId()
                + ", unittype "
                + unit.getUnittype().getName()
                + ", profile "
                + unit.getProfile().getName());
      }
      return unit;
    } catch (Throwable t) {
      DBAccessErrorHandler.handleError("readUnit", t);
      return null;
    }
  }

  public ACS getAcs() {
    return acs;
  }
}
