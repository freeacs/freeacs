package com.owera.xapsws.impl;

import com.github.freeacs.dbi.Profile;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.dbi.XAPS;
import com.owera.xapsws.DeleteProfileRequest;
import com.owera.xapsws.DeleteProfileResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;

public class DeleteProfile {

	private static final Logger logger = LoggerFactory.getLogger(DeleteProfile.class);

	private XAPS xaps;
	private XAPSWS xapsWS;

	public DeleteProfileResponse deleteProfile(DeleteProfileRequest dur) throws RemoteException {
		try {
			
			xapsWS = XAPSWSFactory.getXAPSWS(dur.getLogin());
			xaps = xapsWS.getXAPS();
			if (dur.getUnittype() == null)
				throw XAPSWS.error(logger, "No unittype is specified");
			Unittype unittype = xapsWS.getUnittypeFromXAPS(dur.getUnittype().getName());
			if (dur.getProfile() == null)
				throw XAPSWS.error(logger, "No profile name is specified");
			Profile profile = xapsWS.getProfileFromXAPS(unittype.getName(), dur.getProfile().getName());
			int rowsDeleted = unittype.getProfiles().deleteProfile(profile, xaps, true);
			if (rowsDeleted > 0)
				return new DeleteProfileResponse(true);
			else
				return new DeleteProfileResponse(false);
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

}
