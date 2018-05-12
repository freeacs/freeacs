package com.owera.xapsws.impl;

import java.rmi.RemoteException;

import com.owera.common.log.Logger;
import com.owera.xaps.dbi.Unittype;

import com.owera.xapsws.ArrayOfUnittype;
import com.owera.xapsws.GetUnittypesRequest;
import com.owera.xapsws.GetUnittypesResponse;
import com.owera.xapsws.UnittypeList;

public class GetUnittypes {

	private static Logger logger = new Logger();

	public GetUnittypesResponse getUnittypes(GetUnittypesRequest gur) throws RemoteException {
		try {
			
			XAPSWS xapsWS = XAPSWSFactory.getXAPSWS(gur.getLogin());
			if (gur.getUnittypeName() != null) {
				Unittype unittypeXAPS = xapsWS.getUnittypeFromXAPS(gur.getUnittypeName());
				com.owera.xapsws.Unittype[] unittypeArray = new com.owera.xapsws.Unittype[1];
				unittypeArray[0] = ConvertXAPS2WS.convert(unittypeXAPS);
				UnittypeList unittypeList = new UnittypeList(new ArrayOfUnittype(unittypeArray));
				return new GetUnittypesResponse(unittypeList);
			} else { // return all Unittypes allowed
				Unittype[] unittypeXAPSArr = xapsWS.getXAPS().getUnittypes().getUnittypes();
				com.owera.xapsws.Unittype[] unittypeArray = new com.owera.xapsws.Unittype[unittypeXAPSArr.length];
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
