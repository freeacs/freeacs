package com.owera.xapsws.impl;

import java.rmi.RemoteException;

import com.owera.common.log.Logger;
import com.owera.xaps.dbi.Profile;
import com.owera.xaps.dbi.Unittype;

import com.owera.xapsws.ArrayOfProfile;
import com.owera.xapsws.GetProfilesRequest;
import com.owera.xapsws.GetProfilesResponse;
import com.owera.xapsws.ProfileList;

public class GetProfiles {

	private static Logger logger = new Logger();

	private XAPSWS xapsWS;

	public GetProfilesResponse getProfiles(GetProfilesRequest gur) throws RemoteException {
		try {
			
			xapsWS = XAPSWSFactory.getXAPSWS(gur.getLogin());
			if (gur.getUnittype() == null || gur.getUnittype().getName() == null)
				throw XAPSWS.error(logger, "No unittype is specified");
			Unittype unittype = xapsWS.getUnittypeFromXAPS(gur.getUnittype().getName());
			com.owera.xapsws.Profile[] profileArray = null;
			if (gur.getProfile() == null || gur.getProfile().getName() == null) {
				Profile[] profileXAPSArr = unittype.getProfiles().getProfiles();
				//				List<Profile> allowedProfiles = xapsWS.getXAPS().getAllowedProfiles(unittype);
				//				profileArray = new com.owera.xapsws.Profile[allowedProfiles.size()];
				profileArray = new com.owera.xapsws.Profile[profileXAPSArr.length];
				int i = 0;
				for (Profile profileXAPS : profileXAPSArr)
					profileArray[i++] = ConvertXAPS2WS.convert(profileXAPS);
				//				for (int i = 0; i < allowedProfiles.size(); i++)
				//					profileArray[i] = ConvertXAPS2WS.convert(allowedProfiles.get(i));
			} else {
				profileArray = new com.owera.xapsws.Profile[1];
				Profile p = xapsWS.getProfileFromXAPS(unittype.getName(), gur.getProfile().getName());
				profileArray[0] = ConvertXAPS2WS.convert(p);
			}
			return new GetProfilesResponse(new ProfileList(new ArrayOfProfile(profileArray)));
		} catch (Throwable t) {
			if (t instanceof RemoteException)
				throw (RemoteException) t;
			else {
				throw XAPSWS.error(logger, t);
			}
		}

	}
}
