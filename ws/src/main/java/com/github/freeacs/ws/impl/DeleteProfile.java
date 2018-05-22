package com.github.freeacs.ws.impl;

import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.Profile;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.ws.DeleteProfileRequest;
import com.github.freeacs.ws.DeleteProfileResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.rmi.RemoteException;

public class DeleteProfile {

	private static final Logger logger = LoggerFactory.getLogger(DeleteProfile.class);

	private ACS ACS;
	private ACSWS xapsWS;

	public DeleteProfileResponse deleteProfile(DeleteProfileRequest dur, DataSource xapsDs, DataSource syslogDs) throws RemoteException {
		try {
			
			xapsWS = ACSWSFactory.getXAPSWS(dur.getLogin(), xapsDs, syslogDs);
			ACS = xapsWS.getXAPS();
			if (dur.getUnittype() == null)
				throw ACSWS.error(logger, "No unittype is specified");
			Unittype unittype = xapsWS.getUnittypeFromXAPS(dur.getUnittype().getName());
			if (dur.getProfile() == null)
				throw ACSWS.error(logger, "No profile name is specified");
			Profile profile = xapsWS.getProfileFromXAPS(unittype.getName(), dur.getProfile().getName());
			int rowsDeleted = unittype.getProfiles().deleteProfile(profile, ACS, true);
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
