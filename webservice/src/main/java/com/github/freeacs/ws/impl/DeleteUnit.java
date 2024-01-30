package com.github.freeacs.ws.impl;

import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.ACSUnit;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.ws.xml.DeleteUnitRequest;
import com.github.freeacs.ws.xml.DeleteUnitResponse;
import com.github.freeacs.ws.xml.ObjectFactory;
import com.github.freeacs.ws.xml.Unit;
import java.rmi.RemoteException;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteUnit {
  private static final Logger logger = LoggerFactory.getLogger(DeleteUnit.class);

  private ACSFactory acsWS;

  public DeleteUnitResponse deleteUnit(
      DeleteUnitRequest dur, DataSource xapsDs, DataSource syslogDs) throws RemoteException {
    try {
      acsWS = ACSWSFactory.getXAPSWS(dur.getLogin(), xapsDs, syslogDs);
      ACS acs = acsWS.getAcs();
      ACSUnit acsUnit = acsWS.getXAPSUnit(acs);
      if (dur.getUnit() == null) {
        throw ACSFactory.error(logger, "No unit object is specified");
      }
      com.github.freeacs.dbi.Unit unitXAPS = validateUnitId(dur.getUnit(), acsUnit);
      if (unitXAPS == null) {
        return getDeleteUnitResponse(false);
      } else {
        int rowsDeleted = acsUnit.deleteUnit(unitXAPS);
        return getDeleteUnitResponse(rowsDeleted > 0);
      }
    } catch (Throwable t) {
      if (t instanceof RemoteException) {
        throw (RemoteException) t;
      } else {
        String msg = "An exception occurred: " + t.getMessage();
        logger.error(msg, t);
        throw new RemoteException(msg, t);
      }
    }
  }

  private DeleteUnitResponse getDeleteUnitResponse(boolean b) {
    DeleteUnitResponse response = new DeleteUnitResponse();
    response.setDeleted(b);
    return response;
  }

  private com.github.freeacs.dbi.Unit validateUnitId(Unit unitWS, ACSUnit acsUnit)
      throws SQLException, RemoteException {
    com.github.freeacs.dbi.Unit unitXAPS = null;
    if (unitWS.getUnitId() == null) {
      if (unitWS.getSerialNumber() != null) {
        Unittype unittype = acsWS.getUnittypeFromXAPS(unitWS.getUnittype().getValue().getName());
        unitXAPS = acsWS.getUnitByMAC(acsUnit, unittype, null, unitWS.getSerialNumber().getValue());
        ObjectFactory factory = new ObjectFactory();
        unitWS.setUnitId(factory.createUnitUnitId(unitXAPS.getId()));
      } else {
        ACSFactory.error(logger, "No unitId or serial number is supplied to the service");
      }
    } else {
      unitXAPS = acsUnit.getUnitById(unitWS.getUnitId().getValue());
      if (unitXAPS.getId() == null) {
        return null;
      }
    }
    acsWS.getProfileFromXAPS(unitXAPS.getUnittype().getName(), unitXAPS.getProfile().getName());
    return unitXAPS;
  }
}
