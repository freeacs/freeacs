package com.github.freeacs.ws.impl;

import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.ACSUnit;
import com.github.freeacs.dbi.Profile;
import com.github.freeacs.dbi.Unit;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.dbi.UnittypeParameter;
import com.github.freeacs.ws.xml.ArrayOfParameter;
import com.github.freeacs.ws.xml.ArrayOfUnit;
import com.github.freeacs.ws.xml.GetUnitsRequest;
import com.github.freeacs.ws.xml.GetUnitsResponse;
import com.github.freeacs.ws.xml.ObjectFactory;
import com.github.freeacs.ws.xml.ParameterList;
import com.github.freeacs.ws.xml.UnitList;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetUnits {
  private static final Logger logger = LoggerFactory.getLogger(GetUnits.class);

  public GetUnitsResponse getUnits(GetUnitsRequest gur, DataSource xapsDs, DataSource syslogDs)
      throws RemoteException {
    try {
      ObjectFactory factory = new ObjectFactory();

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
      GetUnitIds.getUnits(acsUnit, unitWS, unittypeXAPS, profilesXAPS, useCase3, unitMap);

      /* Search is executed - now build response */
      boolean moreUnits = unitMap.size() > 50;
      UnitList ul = new UnitList();
      com.github.freeacs.ws.xml.Unit[] unitArray =
          new com.github.freeacs.ws.xml.Unit[unitMap.size()];
      ArrayOfUnit array = new ArrayOfUnit();
      ul.setUnitArray(array);
      int ucount = 0;
      for (Unit unit : unitMap.values()) {
        Unittype utXAPS = unit.getUnittype();
        com.github.freeacs.ws.xml.Unittype utWS = new com.github.freeacs.ws.xml.Unittype();
        utWS.setName(utXAPS.getName());
        com.github.freeacs.ws.xml.Profile profileWs = new com.github.freeacs.ws.xml.Profile();
        profileWs.setName(unit.getProfile().getName());
        com.github.freeacs.ws.xml.Unit unitWs = new com.github.freeacs.ws.xml.Unit();
        unitWs.setUnitId(factory.createUnitUnitId(unit.getId()));
        unitWs.setUnittype(factory.createUnitUnittype(utWS));
        unitWs.setProfile(factory.createUnitProfile(profileWs));
        ParameterList unitParams = new ParameterList();
        ArrayOfParameter unitParamsArray = new ArrayOfParameter();
        unitParamsArray
            .getItem()
            .addAll(
                unit.getParameters()
                    .entrySet()
                    .stream()
                    .map(
                        entry -> {
                          com.github.freeacs.ws.xml.Parameter p =
                              new com.github.freeacs.ws.xml.Parameter();
                          p.setName(entry.getKey());
                          p.setValue(factory.createParameterValue(entry.getValue()));
                          return p;
                        })
                    .collect(Collectors.toList()));
        unitParams.setParameterArray(unitParamsArray);
        unitWs.setParameters(factory.createUnitParameters(unitParams));
        unitArray[ucount++] = unitWs;
      }
      array.getItem().addAll(Arrays.asList(unitArray));
      getParameters(factory, unitMap, unitArray);
      GetUnitsResponse response = new GetUnitsResponse();
      response.setUnits(factory.createGetUnitsResponseUnits(ul));
      response.setMoreUnits(moreUnits);
      return response;
    } catch (RemoteException re) {
      throw re;
    } catch (Throwable t) {
      throw ACSFactory.error(logger, t);
    }
  }

  private void getParameters(
      ObjectFactory factory,
      Map<String, Unit> unitMap,
      com.github.freeacs.ws.xml.Unit[] unitArray) {
    int ucount = 0;
    for (Unit unit : unitMap.values()) {
      Unittype utXAPS = unit.getUnittype();
      com.github.freeacs.ws.xml.Unittype utWS = new com.github.freeacs.ws.xml.Unittype();
      utWS.setName(utXAPS.getName());
      utWS.setVendor(factory.createUnittypeVendor(utXAPS.getVendor()));
      utWS.setDescription(factory.createUnittypeDescription(utXAPS.getDescription()));
      utWS.setProtocol(factory.createUnittypeProtocol(utXAPS.getProtocol().toString()));
      Profile pXAPS = unit.getProfile();
      com.github.freeacs.ws.xml.Profile pWS = new com.github.freeacs.ws.xml.Profile();
      pWS.setName(pXAPS.getName());
      UnittypeParameter snUtp = getSerialNumberUtp(utXAPS);
      String serialNumber = null;
      Map<String, String> unitParams = unit.getParameters();
      if (snUtp != null) {
        serialNumber = unitParams.get(snUtp.getName());
      }
      com.github.freeacs.ws.xml.Parameter[] parameterArray =
          new com.github.freeacs.ws.xml.Parameter[unitParams.size()];
      int pcount = 0;
      for (Entry<String, String> entry : unitParams.entrySet()) {
        String flags = "U";
        if (unit.getUnitParameters().get(entry.getKey()) == null) {
          flags = "P";
        }
        com.github.freeacs.ws.xml.Parameter paramWS = new com.github.freeacs.ws.xml.Parameter();
        paramWS.setName(entry.getKey());
        paramWS.setValue(factory.createParameterValue(entry.getValue()));
        paramWS.setFlags(factory.createParameterFlags(flags));
        parameterArray[pcount++] = paramWS;
      }
      ArrayOfParameter arrayOfParameter = new ArrayOfParameter();
      arrayOfParameter.getItem().addAll(Arrays.asList(parameterArray));
      ParameterList parameterList = new ParameterList();
      parameterList.setParameterArray(arrayOfParameter);
      com.github.freeacs.ws.xml.Unit uWS = new com.github.freeacs.ws.xml.Unit();
      uWS.setUnitId(factory.createUnitUnitId(unit.getId()));
      uWS.setSerialNumber(factory.createUnitSerialNumber(serialNumber));
      uWS.setProfile(factory.createUnitProfile(pWS));
      uWS.setUnittype(factory.createUnitUnittype(utWS));
      uWS.setParameters(factory.createUnitParameters(parameterList));
      unitArray[ucount++] = uWS;
    }
  }

  private UnittypeParameter getSerialNumberUtp(Unittype unittype) {
    String snName = "InternetGatewayDevice.DeviceInfo.SerialNumber";
    UnittypeParameter serialNumberUtp = unittype.getUnittypeParameters().getByName(snName);
    if (serialNumberUtp == null) {
      snName = "Device.DeviceInfo.SerialNumber";
      serialNumberUtp = unittype.getUnittypeParameters().getByName(snName);
    }
    return serialNumberUtp;
  }
}
