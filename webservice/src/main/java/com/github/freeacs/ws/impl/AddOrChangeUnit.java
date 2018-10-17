package com.github.freeacs.ws.impl;

import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.ACSUnit;
import com.github.freeacs.dbi.Profile;
import com.github.freeacs.dbi.UnitParameter;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.dbi.UnittypeParameter;
import com.github.freeacs.ws.xml.AddOrChangeUnitRequest;
import com.github.freeacs.ws.xml.AddOrChangeUnitResponse;
import com.github.freeacs.ws.xml.ObjectFactory;
import com.github.freeacs.ws.xml.Parameter;
import com.github.freeacs.ws.xml.Unit;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddOrChangeUnit {
  private static final Logger logger = LoggerFactory.getLogger(AddOrChangeUnit.class);

  private ACSFactory acsWS;
  private ACSUnit acsUnit;

  public AddOrChangeUnitResponse addOrChangeUnit(
      AddOrChangeUnitRequest aocur, DataSource xapsDs, DataSource syslogDs) throws RemoteException {
    try {
      acsWS = ACSWSFactory.getXAPSWS(aocur.getLogin(), xapsDs, syslogDs);
      ACS acs = acsWS.getAcs();
      acsUnit = acsWS.getXAPSUnit(acs);

      /*
       * We need to support these use cases
       * UC1 - Add unit:
       *  unittype (must exist in xAPS)
       *  profile (must exist in xAPS)
       *  unitId (should be <OUI>-<ProductClass>-<SerialNumber>
       *  secret (must match protocol pattern)
       * UC2 - Change unit:
       *  unittype (must exist in xAPS)
       *  profile (must exist in xAPS)
       * 	unitId (must match protocol pattern)
       * UC3 - Change unit:
       *  unittype (must exist in xAPS)
       *  profile (must exist in xAPS)
       * 	serialNumber (no requirements)
       *
       * To find out which UC it is, use following algorithm:
       * 1. If uniqueId is present -> UC2
       * 2. If serialNumber is present, but no unit Id is present -> UC4
       * 3. UC1 and UC3 are basically the same when it comes to logic performed
       *
       * Additional rules:
       * 1. unittype and profile must be specified and must exist in xAPS
       * 2. If a secret is specified it must adhere to the protocol pattern
       * 3. If a unit id is specified it must adhere to the protocol pattern
       */
      Unit unitWS = aocur.getUnit();
      if (unitWS.getUnittype() == null || unitWS.getProfile() == null) {
        throw ACSFactory.error(logger, "Unittype and/or Profile object are missing");
      }
      Profile profile =
          acsWS.getProfileFromXAPS(
              unitWS.getUnittype().getValue().getName(), unitWS.getProfile().getValue().getName());
      String unitId = validateUnitId(unitWS, profile.getUnittype(), profile);
      List<String> unitIds = new ArrayList<>();
      unitIds.add(unitId);
      List<UnitParameter> acParams =
          validateAddOrChangeUnitParameters(unitWS, profile.getUnittype(), profile);
      List<UnitParameter> dParams =
          validateDeleteUnitParameters(unitWS, profile.getUnittype(), profile);
      acsUnit.addUnits(unitIds, profile);
      acsUnit.addOrChangeUnitParameters(acParams, profile);
      acsUnit.deleteUnitParameters(dParams);
      AddOrChangeUnitResponse response = new AddOrChangeUnitResponse();
      response.setUnit(unitWS);
      return response;
    } catch (Throwable t) {
      if (t instanceof RemoteException) {
        throw (RemoteException) t;
      } else {
        throw ACSFactory.error(logger, t);
      }
    }
  }

  private String validateUnitId(Unit unitWS, Unittype unittype, Profile profile)
      throws SQLException, RemoteException {
    if (unitWS.getUnitId() == null) {
      if (unitWS.getSerialNumber() != null) {
        com.github.freeacs.dbi.Unit unitXAPS =
            acsWS.getUnitByMAC(acsUnit, unittype, profile, unitWS.getSerialNumber().getValue());
        if (unitXAPS != null) {
          ObjectFactory factory = new ObjectFactory();
          unitWS.setUnitId(factory.createUnitUnitId(unitXAPS.getId()));
        }
      }
      if (unitWS.getUnitId() == null) {
        throw ACSFactory.error(logger, "No unitId or serial number is supplied to the service");
      }
    }
    return unitWS.getUnitId().getValue();
  }

  private List<UnitParameter> validateDeleteUnitParameters(
      Unit unitWS, Unittype unittype, Profile profile) throws RemoteException {
    List<UnitParameter> unitParams = new ArrayList<>();
    List<Parameter> parameters = unitWS.getParameters().getValue().getParameterArray().getItem();
    for (Parameter p : parameters) {
      UnittypeParameter utp = unittype.getUnittypeParameters().getByName(p.getName());
      if (utp == null) {
        throw ACSFactory.error(
            logger,
            "Unittype parameter "
                + p.getName()
                + " is not found in unittype "
                + unittype.getName());
      } else if (p.getFlags() != null && "D".equals(p.getFlags().getValue())) {
        unitParams.add(
            new UnitParameter(
                utp, unitWS.getUnitId().getValue(), p.getValue().getValue(), profile));
      }
    }
    return unitParams;
  }

  private List<UnitParameter> validateAddOrChangeUnitParameters(
      Unit unitWS, Unittype unittype, Profile profile) throws RemoteException {
    List<UnitParameter> unitParams = new ArrayList<>();
    List<Parameter> parameters = unitWS.getParameters().getValue().getParameterArray().getItem();
    for (Parameter p : parameters) {
      UnittypeParameter utp = unittype.getUnittypeParameters().getByName(p.getName());
      if (utp == null) {
        throw ACSFactory.error(
            logger,
            "Unittype parameter "
                + p.getName()
                + " is not found in unittype "
                + unittype.getName());
      } else if (p.getFlags() == null || "AC".equals(p.getFlags().getValue())) {
        unitParams.add(
            new UnitParameter(
                utp, unitWS.getUnitId().getValue(), p.getValue().getValue(), profile));
      }
    }
    return unitParams;
  }
}
