package com.github.freeacs.ws.impl;

import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.ACSUnit;
import com.github.freeacs.dbi.SyslogConstants;
import com.github.freeacs.dbi.Unit;
import com.github.freeacs.ws.xml.KickUnitRequest;
import com.github.freeacs.ws.xml.KickUnitResponse;
import java.rmi.RemoteException;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KickUnit {
  private static final Logger LOG = LoggerFactory.getLogger(KickUnit.class);

  public KickUnitResponse kickUnit(KickUnitRequest gur, DataSource xapsDs, DataSource syslogDs)
      throws RemoteException {
    final ACSFactory acsWS = ACSWSFactory.getXAPSWS(gur.getLogin(), xapsDs, syslogDs);
    final ACS acs = acsWS.getAcs();
    final ACSUnit acsUnit = acsWS.getXAPSUnit(acs);
    final String unitId = gur.getUnitId();
    final Unit unit;
    try {
      unit = acsUnit.getUnitById(unitId);
    } catch (SQLException e) {
      LOG.error("Failed to find unit " + unitId, e);
      throw new RemoteException("Failed to find unit " + unitId);
    }
    acs.getDbi().publishKick(unit, SyslogConstants.FACILITY_STUN);
    return new KickUnitResponse();
  }
}
