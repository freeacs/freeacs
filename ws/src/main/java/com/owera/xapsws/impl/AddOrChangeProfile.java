package com.owera.xapsws.impl;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.owera.common.db.NoAvailableConnectionException;
import com.owera.common.log.Logger;
import com.owera.xaps.dbi.Permission;
import com.owera.xaps.dbi.Profile;
import com.owera.xaps.dbi.ProfileParameter;
import com.owera.xaps.dbi.Unittype;
import com.owera.xaps.dbi.UnittypeParameter;
import com.owera.xaps.dbi.User;
import com.owera.xaps.dbi.XAPS;

import com.owera.xapsws.AddOrChangeProfileRequest;
import com.owera.xapsws.AddOrChangeProfileResponse;
import com.owera.xapsws.Parameter;
import com.owera.xapsws.ParameterList;

public class AddOrChangeProfile {

	private static Logger logger = new Logger();

	private XAPS xaps;
	private XAPSWS xapsWS;

	private void addOrChangeProfileImpl(Profile profileXAPS, AddOrChangeProfileRequest gur) throws NoAvailableConnectionException, SQLException, RemoteException {
		ParameterList parameterList = gur.getProfile().getParameters();
		List<ProfileParameter> acPpList = new ArrayList<ProfileParameter>();
		List<ProfileParameter> dPpList = new ArrayList<ProfileParameter>();
		Unittype unittypeXAPS = profileXAPS.getUnittype();
		if (parameterList != null && parameterList.getParameterArray() != null) {
			for (Parameter param : parameterList.getParameterArray().getItem()) {
				UnittypeParameter utp = unittypeXAPS.getUnittypeParameters().getByName(param.getName());
				if (utp == null)
					throw XAPSWS.error(logger, "The unittype parameter " + param.getName() + " does not exist, hence cannot add profile parameter");
				if (param.getFlags() != null && !param.getFlags().equals("D") && !param.getFlags().equals("AC"))
					throw XAPSWS.error(logger, "Flag for parameter " + param.getName() + " had value " + param.getFlags() + ", but must be either D or AC");
				ProfileParameter pp = profileXAPS.getProfileParameters().getByName(param.getName());
				if (param.getFlags() == null || param.getFlags().equals("AC")) {
					if (pp == null)
						acPpList.add(new ProfileParameter(profileXAPS, utp, param.getValue()));
					else {
						pp.setValue(param.getValue());
						acPpList.add(pp);
					}
				} else if (param.getFlags().equals("D") && pp != null) {
					dPpList.add(pp);
				}
			}
		}
		unittypeXAPS.getProfiles().addOrChangeProfile(profileXAPS, xaps);
		for (ProfileParameter pp : dPpList)
			profileXAPS.getProfileParameters().deleteProfileParameter(pp, xaps);
		for (ProfileParameter pp : acPpList)
			profileXAPS.getProfileParameters().addOrChangeProfileParameter(pp, xaps);
	}

	public AddOrChangeProfileResponse addOrChangeProfile(AddOrChangeProfileRequest gur) throws RemoteException {
		try {
			
			xapsWS = XAPSWSFactory.getXAPSWS(gur.getLogin());
			xaps = xapsWS.getXAPS();
			if (gur.getUnittype() == null || gur.getProfile() == null)
				throw XAPSWS.error(logger, "No unittype or profile specified");
			Unittype unittype = xaps.getUnittype(gur.getUnittype().getName());
			User user = xapsWS.getId().getUser();
			boolean isAllowedToMakeProfile = user.getPermissions().getPermissions().length == 0;
			if (!isAllowedToMakeProfile) {
				Permission perm = user.getPermissions().getByUnittypeProfile(unittype.getId(), null);
				if (perm != null)
					isAllowedToMakeProfile = true;
			}

			Profile profileXAPS = null;
			if (unittype.getProfiles().getByName(gur.getProfile().getName()) == null) { // make new profile
				if (isAllowedToMakeProfile) {
					com.owera.xapsws.Profile pWS = gur.getProfile();
					profileXAPS = new Profile(pWS.getName(), unittype);
					addOrChangeProfileImpl(profileXAPS, gur);
				} else {
					throw XAPSWS.error(logger, "The profile " + gur.getProfile().getName() + " does not exist, your login does not have the permissions to create it.");
				}
			} else { // change an existing one
				profileXAPS = xapsWS.getProfileFromXAPS(unittype.getName(), gur.getProfile().getName());
				addOrChangeProfileImpl(profileXAPS, gur);
			}
			return new AddOrChangeProfileResponse(ConvertXAPS2WS.convert(profileXAPS));
		} catch (Throwable t) {
			if (t instanceof RemoteException)
				throw (RemoteException) t;
			else {
				throw XAPSWS.error(logger, t);
			}
		}

	}
}
