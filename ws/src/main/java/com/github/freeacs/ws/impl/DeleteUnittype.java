package com.github.freeacs.ws.impl;

import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.dbi.ACS;
import com.github.freeacs.ws.DeleteUnittypeRequest;
import com.github.freeacs.ws.DeleteUnittypeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.rmi.RemoteException;

public class DeleteUnittype {

	private static final Logger logger = LoggerFactory.getLogger(DeleteUnittype.class);

	private ACS ACS;
	private ACSWS xapsWS;

	public DeleteUnittypeResponse deleteUnittype(DeleteUnittypeRequest dur, DataSource xapsDs, DataSource syslogDs) throws RemoteException {
		try {
			
			xapsWS = ACSWSFactory.getXAPSWS(dur.getLogin(), xapsDs, syslogDs);
			ACS = xapsWS.getXAPS();
			if (dur.getUnittypeName() == null)
				throw ACSWS.error(logger, "No unittype name is specified");
			Unittype unittype = xapsWS.getUnittypeFromXAPS(dur.getUnittypeName());
			//			System.out.println("D: Unitypes object: " + xaps.getUnittypes());
			//			System.out.println("D: Unittype object: " + unittype);
			int rowsDeleted = ACS.getUnittypes().deleteUnittype(unittype, ACS, true);
			if (rowsDeleted > 0)
				return new DeleteUnittypeResponse(true);
			else
				return new DeleteUnittypeResponse(false);
		} catch (Throwable t) {
			if (t instanceof RemoteException)
				throw (RemoteException) t;
			else {
				throw ACSWS.error(logger, t);
			}
		}

	}
}
