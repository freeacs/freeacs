package com.owera.xapsws.impl;

import com.owera.common.db.NoAvailableConnectionException;
import com.owera.xaps.dbi.Unittype;
import com.owera.xaps.dbi.XAPS;
import com.owera.xaps.dbi.XAPSUnit;
import com.owera.xapsws.DeleteUnitRequest;
import com.owera.xapsws.DeleteUnitResponse;
import com.owera.xapsws.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.sql.SQLException;

public class DeleteUnit {
	private static final Logger logger = LoggerFactory.getLogger(DeleteUnit.class);

	private XAPS xaps;
	private XAPSWS xapsWS;

	public DeleteUnitResponse deleteUnit(DeleteUnitRequest dur) throws RemoteException {
		try {
			
			xapsWS = XAPSWSFactory.getXAPSWS(dur.getLogin());
			xaps = xapsWS.getXAPS();
			XAPSUnit xapsUnit = xapsWS.getXAPSUnit(xaps);
			if (dur.getUnit() == null)
				throw XAPSWS.error(logger, "No unit object is specified");
			com.owera.xaps.dbi.Unit unitXAPS = validateUnitId(dur.getUnit(), xapsUnit);
			if (unitXAPS == null) {
				return new DeleteUnitResponse(false);
			} else {
				int rowsDeleted = xapsUnit.deleteUnit(unitXAPS);
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

	private com.owera.xaps.dbi.Unit validateUnitId(Unit unitWS, XAPSUnit xapsUnit) throws SQLException, NoAvailableConnectionException, RemoteException {
		com.owera.xaps.dbi.Unit unitXAPS = null;
		if (unitWS.getUnitId() == null) {
			if (unitWS.getSerialNumber() != null) {
				Unittype unittype = xapsWS.getUnittypeFromXAPS(unitWS.getUnittype().getName());
				unitXAPS = xapsWS.getUnitByMAC(xapsUnit, unittype, null, unitWS.getSerialNumber());
				unitWS.setUnitId(unitXAPS.getId());
			} else {
				XAPSWS.error(logger, "No unitId or serial number is supplied to the service");
			}
		} else {
			unitXAPS = xapsUnit.getUnitById(unitWS.getUnitId());
			if (unitXAPS.getId() == null)
				return null;
		}
		xapsWS.getProfileFromXAPS(unitXAPS.getUnittype().getName(), unitXAPS.getProfile().getName());
		return unitXAPS;
	}

}
