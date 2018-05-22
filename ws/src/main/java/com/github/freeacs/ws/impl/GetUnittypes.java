package com.github.freeacs.ws.impl;

import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.ws.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.rmi.RemoteException;

public class GetUnittypes {

	private static final Logger logger = LoggerFactory.getLogger(GetUnittypes.class);

	public GetUnittypesResponse getUnittypes(GetUnittypesRequest gur, DataSource xapsDs, DataSource syslogDs) throws RemoteException {
		try {
			
			ACSWS xapsWS = ACSWSFactory.getXAPSWS(gur.getLogin(), xapsDs, syslogDs);
			if (gur.getUnittypeName() != null) {
				Unittype unittypeXAPS = xapsWS.getUnittypeFromXAPS(gur.getUnittypeName());
				com.github.freeacs.ws.Unittype[] unittypeArray = new com.github.freeacs.ws.Unittype[1];
				unittypeArray[0] = ConvertACS2WS.convert(unittypeXAPS);
				UnittypeList unittypeList = new UnittypeList(new ArrayOfUnittype(unittypeArray));
				return new GetUnittypesResponse(unittypeList);
			} else { // return all Unittypes allowed
				Unittype[] unittypeXAPSArr = xapsWS.getAcs().getUnittypes().getUnittypes();
				com.github.freeacs.ws.Unittype[] unittypeArray = new com.github.freeacs.ws.Unittype[unittypeXAPSArr.length];
				int i = 0;
				for (Unittype unittypeXAPS : unittypeXAPSArr)
					unittypeArray[i++] = ConvertACS2WS.convert(unittypeXAPS);
				UnittypeList unittypeList = new UnittypeList(new ArrayOfUnittype(unittypeArray));
				return new GetUnittypesResponse(unittypeList);
			}
		} catch (Throwable t) {
			if (t instanceof RemoteException)
				throw (RemoteException) t;
			else {
				throw ACSWS.error(logger, t);
			}
		}

	}
}
