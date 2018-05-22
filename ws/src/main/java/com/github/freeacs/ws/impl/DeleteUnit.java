package com.github.freeacs.ws.impl;

import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.dbi.ACSUnit;
import com.github.freeacs.ws.DeleteUnitRequest;
import com.github.freeacs.ws.DeleteUnitResponse;
import com.github.freeacs.ws.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.rmi.RemoteException;
import java.sql.SQLException;

public class DeleteUnit {
	private static final Logger logger = LoggerFactory.getLogger(DeleteUnit.class);

	private ACS ACS;
	private ACSWS xapsWS;

	public DeleteUnitResponse deleteUnit(DeleteUnitRequest dur, DataSource xapsDs, DataSource syslogDs) throws RemoteException {
		try {
			
			xapsWS = ACSWSFactory.getXAPSWS(dur.getLogin(), xapsDs, syslogDs);
			ACS = xapsWS.getXAPS();
			ACSUnit ACSUnit = xapsWS.getXAPSUnit(ACS);
			if (dur.getUnit() == null)
				throw ACSWS.error(logger, "No unit object is specified");
			com.github.freeacs.dbi.Unit unitXAPS = validateUnitId(dur.getUnit(), ACSUnit);
			if (unitXAPS == null) {
				return new DeleteUnitResponse(false);
			} else {
				int rowsDeleted = ACSUnit.deleteUnit(unitXAPS);
				if (rowsDeleted > 0)
					return new DeleteUnitResponse(true);
				else
					return new DeleteUnitResponse(false);
			}
		} catch (Throwable t) {
			if (t instanceof RemoteException)
				throw (RemoteException) t;
			else {
				String msg = "An exception occurred: " + t.getMessage();
				logger.error(msg, t);
				throw new RemoteException(msg, t);
			}
		}

	}

	private com.github.freeacs.dbi.Unit validateUnitId(Unit unitWS, ACSUnit ACSUnit) throws SQLException, RemoteException {
		com.github.freeacs.dbi.Unit unitXAPS = null;
		if (unitWS.getUnitId() == null) {
			if (unitWS.getSerialNumber() != null) {
				Unittype unittype = xapsWS.getUnittypeFromXAPS(unitWS.getUnittype().getName());
				unitXAPS = xapsWS.getUnitByMAC(ACSUnit, unittype, null, unitWS.getSerialNumber());
				unitWS.setUnitId(unitXAPS.getId());
			} else {
				ACSWS.error(logger, "No unitId or serial number is supplied to the service");
			}
		} else {
			unitXAPS = ACSUnit.getUnitById(unitWS.getUnitId());
			if (unitXAPS.getId() == null)
				return null;
		}
		xapsWS.getProfileFromXAPS(unitXAPS.getUnittype().getName(), unitXAPS.getProfile().getName());
		return unitXAPS;
	}

}
