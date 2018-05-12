package com.github.freeacs.ws.impl;

import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.ws.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;

public class GetUnittypes {

	private static final Logger logger = LoggerFactory.getLogger(GetUnittypes.class);

	public GetUnittypesResponse getUnittypes(GetUnittypesRequest gur) throws RemoteException {
		try {
			
			XAPSWS xapsWS = XAPSWSFactory.getXAPSWS(gur.getLogin());
			if (gur.getUnittypeName() != null) {
				Unittype unittypeXAPS = xapsWS.getUnittypeFromXAPS(gur.getUnittypeName());
				com.github.freeacs.ws.Unittype[] unittypeArray = new com.github.freeacs.ws.Unittype[1];
				unittypeArray[0] = ConvertXAPS2WS.convert(unittypeXAPS);
				UnittypeList unittypeList = new UnittypeList(new ArrayOfUnittype(unittypeArray));
				return new GetUnittypesResponse(unittypeList);
			} else { // return all Unittypes allowed
				Unittype[] unittypeXAPSArr = xapsWS.getXAPS().getUnittypes().getUnittypes();
				com.github.freeacs.ws.Unittype[] unittypeArray = new com.github.freeacs.ws.Unittype[unittypeXAPSArr.length];
				int i = 0;
				for (Unittype unittypeXAPS : unittypeXAPSArr)
					unittypeArray[i++] = ConvertXAPS2WS.convert(unittypeXAPS);
				UnittypeList unittypeList = new UnittypeList(new ArrayOfUnittype(unittypeArray));
				return new GetUnittypesResponse(unittypeList);
			}
		} catch (Throwable t) {
			if (t instanceof RemoteException)
				throw (RemoteException) t;
			else {
				throw XAPSWS.error(logger, t);
			}
		}

	}
}
