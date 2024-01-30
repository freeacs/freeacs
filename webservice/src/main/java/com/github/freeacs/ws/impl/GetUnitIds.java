package com.github.freeacs.ws.impl;

import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.ACSUnit;
import com.github.freeacs.dbi.Parameter;
import com.github.freeacs.dbi.Parameter.Operator;
import com.github.freeacs.dbi.Parameter.ParameterDataType;
import com.github.freeacs.dbi.Profile;
import com.github.freeacs.dbi.Unit;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.dbi.UnittypeParameter;
import com.github.freeacs.ws.xml.ArrayOfUnitId;
import com.github.freeacs.ws.xml.GetUnitIdsRequest;
import com.github.freeacs.ws.xml.GetUnitIdsResponse;
import com.github.freeacs.ws.xml.ObjectFactory;
import com.github.freeacs.ws.xml.UnitIdList;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetUnitIds {
  private static final Logger logger = LoggerFactory.getLogger(GetUnitIds.class);

  public GetUnitIdsResponse getUnits(GetUnitIdsRequest gur, DataSource xapsDs, DataSource syslogDs)
      throws RemoteException {
    try {
      ACSFactory acsWS = ACSWSFactory.getXAPSWS(gur.getLogin(), xapsDs, syslogDs);
      ACS acs = acsWS.getAcs();
      ACSUnit acsUnit = acsWS.getXAPSUnit(acs);

      com.github.freeacs.ws.xml.Unit unitWS = gur.getUnit();

      /* Validate input - only allow permitted unittypes/profiles for this login */
      Unittype unittypeXAPS = null;
      List<Profile> profilesXAPS = new ArrayList<>();
      if (unitWS.getUnittype() != null && unitWS.getUnittype().getName() != null) {
        unittypeXAPS = acsWS.getUnittypeFromXAPS(unitWS.getUnittype().getValue().getName());
        if (unitWS.getProfile() != null && unitWS.getProfile().getName() != null) {
          profilesXAPS.add(
              acsWS.getProfileFromXAPS(
                  unittypeXAPS.getName(), unitWS.getProfile().getValue().getName()));
        } else {
          profilesXAPS = Arrays.asList(unittypeXAPS.getProfiles().getProfiles());
        }
      }
      boolean useCase3 =
          unitWS.getParameters() != null
              && !unitWS.getParameters().getValue().getParameterArray().getItem().isEmpty();
      if (useCase3 && profilesXAPS.isEmpty()) {
        throw ACSFactory.error(
            logger,
            "Unittype and profiles are not specified, not possible to execute parameter-search");
      }

      /* Input is validated - now execute searches */
      Map<String, Unit> unitMap = new TreeMap<>();
      getUnits(acsUnit, unitWS, unittypeXAPS, profilesXAPS, useCase3, unitMap);

      String[] unitIdArray = new String[unitMap.size()];
      unitMap.keySet().toArray(unitIdArray);
      GetUnitIdsResponse response = new GetUnitIdsResponse();
      UnitIdList unitIdList = new UnitIdList();
      ArrayOfUnitId arrayOfUnitId = new ArrayOfUnitId();
      arrayOfUnitId.getUnitId().addAll(Arrays.asList(unitIdArray));
      unitIdList.setUnitIdArray(arrayOfUnitId);
      ObjectFactory factory = new ObjectFactory();
      response.setUnits(factory.createGetUnitIdsResponseUnits(unitIdList));
      return response;
    } catch (RemoteException re) {
      throw re;
    } catch (Throwable t) {
      throw ACSFactory.error(logger, t);
    }
  }

  public static void getUnits(
      ACSUnit acsUnit,
      com.github.freeacs.ws.xml.Unit unitWS,
      Unittype unittypeXAPS,
      List<Profile> profilesXAPS,
      boolean useCase3,
      Map<String, Unit> unitMap)
      throws SQLException, RemoteException {
    if (unitWS.getUnitId() != null) { // Use-case 1
      Unit unitXAPS = acsUnit.getUnitById(unitWS.getUnitId().getValue());
      if (unitXAPS != null) {
        unitMap.put(unitWS.getUnitId().getValue(), unitXAPS);
      }
    } else if (useCase3) { // Use-case 3, expect parameters and unittype
      List<Parameter> upList = validateParameters(unitWS, profilesXAPS);
      Map<String, Unit> tmpMap = acsUnit.getUnits(unittypeXAPS, profilesXAPS, upList, 51);
      for (Unit unitXAPS : tmpMap.values()) {
        unitMap.put(unitXAPS.getId(), acsUnit.getUnitById(unitXAPS.getId()));
      }
    } else { // Use-case 2
      Map<String, Unit> tmpMap =
          acsUnit.getUnits(unitWS.getSerialNumber().getValue(), profilesXAPS, 51);
      for (Unit unitXAPS : tmpMap.values()) {
        unitMap.put(unitXAPS.getId(), acsUnit.getUnitById(unitXAPS.getId()));
      }
    }
  }

  private static Unittype getUnittypeForParameters(List<Profile> allowedProfiles)
      throws RemoteException {
    Unittype unittype = allowedProfiles.get(0).getUnittype();
    for (Profile p : allowedProfiles) {
      if (!p.getUnittype().getName().equals(unittype.getName())) {
        throw ACSFactory.error(
            logger, "Cannot specify parameters or SerialNumber without specifying Unittype");
      }
    }
    return unittype;
  }

  private static List<Parameter> validateParameters(
      com.github.freeacs.ws.xml.Unit unitWS, List<Profile> allowedProfiles) throws RemoteException {
    if (allowedProfiles == null || allowedProfiles.isEmpty()) {
      throw ACSFactory.error(
          logger, "Unittype and profiles are not specified, not possible to make parameter-search");
    }
    List<Parameter> parameters = new ArrayList<>();
    if (unitWS.getParameters() != null
        && unitWS.getParameters().getValue().getParameterArray() != null) {
      Unittype unittype = getUnittypeForParameters(allowedProfiles);
      for (com.github.freeacs.ws.xml.Parameter pWS :
          unitWS.getParameters().getValue().getParameterArray().getItem()) {
        UnittypeParameter utp = unittype.getUnittypeParameters().getByName(pWS.getName());
        if (utp == null) {
          throw ACSFactory.error(
              logger,
              "Unittype parameter "
                  + pWS.getName()
                  + " is not found in unittype "
                  + unittype.getName());
        }
        //				boolean equal = true;
        ParameterDataType pdt = ParameterDataType.TEXT;
        Operator op = Operator.EQ;
        if (pWS.getFlags() != null) {
          String[] opTypeArr = pWS.getFlags().getValue().split(",");
          try {
            op = Operator.getOperatorFromLiteral(opTypeArr[0]);
            if (opTypeArr.length == 2) {
              pdt = ParameterDataType.getDataType(opTypeArr[1]);
            }
          } catch (IllegalArgumentException iae) {
            throw ACSFactory.error(
                logger, "An error occurred in flag (" + pWS.getFlags() + "): " + iae.getMessage());
          }
        }
        Parameter pXAPS = new Parameter(utp, pWS.getValue().getValue(), op, pdt);
        parameters.add(pXAPS);
      }
    }
    return parameters;
  }
}
