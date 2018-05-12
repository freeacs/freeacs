package com.github.freeacs.ws.impl;

import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.dbi.XAPS;
import com.github.freeacs.ws.DeleteUnittypeRequest;
import com.github.freeacs.ws.DeleteUnittypeResponse;
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
