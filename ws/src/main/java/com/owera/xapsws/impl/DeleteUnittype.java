package com.owera.xapsws.impl;

import com.owera.xaps.dbi.Unittype;
import com.owera.xaps.dbi.XAPS;
import com.owera.xapsws.DeleteUnittypeRequest;
import com.owera.xapsws.DeleteUnittypeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;

public class DeleteUnittype {

	private static final Logger logger = LoggerFactory.getLogger(DeleteUnittype.class);

	private XAPS xaps;
	private XAPSWS xapsWS;

	public DeleteUnittypeResponse deleteUnittype(DeleteUnittypeRequest dur) throws RemoteException {
		try {
			
			xapsWS = XAPSWSFactory.getXAPSWS(dur.getLogin());
			xaps = xapsWS.getXAPS();
			if (dur.getUnittypeName() == null)
				throw XAPSWS.error(logger, "No unittype name is specified");
			Unittype unittype = xapsWS.getUnittypeFromXAPS(dur.getUnittypeName());
			//			System.out.println("D: Unitypes object: " + xaps.getUnittypes());
			//			System.out.println("D: Unittype object: " + unittype);
			int rowsDeleted = xaps.getUnittypes().deleteUnittype(unittype, xaps, true);
			if (rowsDeleted > 0)
				return new DeleteUnittypeResponse(true);
			else
				return new DeleteUnittypeResponse(false);
		} catch (Throwable t) {
			if (t instanceof RemoteException)
				throw (RemoteException) t;
			else {
				throw XAPSWS.error(logger, t);
			}
		}

	}
}
