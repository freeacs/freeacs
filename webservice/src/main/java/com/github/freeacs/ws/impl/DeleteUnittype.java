package com.github.freeacs.ws.impl;

import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.ws.xml.DeleteUnittypeRequest;
import com.github.freeacs.ws.xml.DeleteUnittypeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.rmi.RemoteException;

public class DeleteUnittype {

	private static final Logger logger = LoggerFactory.getLogger(DeleteUnittype.class);

	public DeleteUnittypeResponse deleteUnittype(DeleteUnittypeRequest dur, DataSource xapsDs, DataSource syslogDs) throws RemoteException {
		try {

			ACSFactory acsWS = ACSWSFactory.getXAPSWS(dur.getLogin(), xapsDs, syslogDs);
			ACS acs = acsWS.getAcs();
			if (dur.getUnittypeName() == null)
				throw ACSFactory.error(logger, "No unittype name is specified");
			Unittype unittype = acsWS.getUnittypeFromXAPS(dur.getUnittypeName());
			int rowsDeleted = acs.getUnittypes().deleteUnittype(unittype, acs, true);
			if (rowsDeleted > 0)
				return getDeleteUnittypeResponse(true);
			else
				return getDeleteUnittypeResponse(false);
		} catch (Throwable t) {
			if (t instanceof RemoteException)
				throw (RemoteException) t;
			else {
				throw ACSFactory.error(logger, t);
			}
		}

	}

	private DeleteUnittypeResponse getDeleteUnittypeResponse(boolean b) {
		DeleteUnittypeResponse response = new DeleteUnittypeResponse();
		response.setDeleted(b);
		return response;
	}
}
