package com.github.freeacs.ws.impl;

import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.Permission;
import com.github.freeacs.dbi.Profile;
import com.github.freeacs.dbi.ProfileParameter;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.dbi.UnittypeParameter;
import com.github.freeacs.dbi.User;
import com.github.freeacs.ws.xml.AddOrChangeProfileRequest;
import com.github.freeacs.ws.xml.AddOrChangeProfileResponse;
import com.github.freeacs.ws.xml.Parameter;
import com.github.freeacs.ws.xml.ParameterList;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AddOrChangeProfile {

	private static org.slf4j.Logger logger = LoggerFactory.getLogger(AddOrChangeProfile.class);

	private ACS acs;

	private void addOrChangeProfileImpl(Profile profileXAPS, AddOrChangeProfileRequest gur) throws SQLException, RemoteException {
		ParameterList parameterList = gur.getProfile().getParameters().getValue();
		List<ProfileParameter> acPpList = new ArrayList<ProfileParameter>();
		List<ProfileParameter> dPpList = new ArrayList<ProfileParameter>();
		Unittype unittypeXAPS = profileXAPS.getUnittype();
		if (parameterList != null && parameterList.getParameterArray() != null) {
			for (Parameter param : parameterList.getParameterArray().getItem()) {
				UnittypeParameter utp = unittypeXAPS.getUnittypeParameters().getByName(param.getName());
				if (utp == null)
					throw ACSFactory.error(logger, "The unittype parameter " + param.getName() + " does not exist, hence cannot add profile parameter");
				if (param.getFlags() != null && !param.getFlags().getValue().equals("D") && !param.getFlags().getValue().equals("AC"))
					throw ACSFactory.error(logger, "Flag for parameter " + param.getName() + " had value " + param.getFlags() + ", but must be either D or AC");
				ProfileParameter pp = profileXAPS.getProfileParameters().getByName(param.getName());
				if (param.getFlags() == null || param.getFlags().getValue().equals("AC")) {
					if (pp == null)
						acPpList.add(new ProfileParameter(profileXAPS, utp, param.getValue().getValue()));
					else {
						pp.setValue(param.getValue().getValue());
						acPpList.add(pp);
					}
				} else if (param.getFlags().getValue().equals("D") && pp != null) {
					dPpList.add(pp);
				}
			}
		}
		unittypeXAPS.getProfiles().addOrChangeProfile(profileXAPS, acs);
		for (ProfileParameter pp : dPpList)
			profileXAPS.getProfileParameters().deleteProfileParameter(pp, acs);
		for (ProfileParameter pp : acPpList)
			profileXAPS.getProfileParameters().addOrChangeProfileParameter(pp, acs);
	}

	public AddOrChangeProfileResponse addOrChangeProfile(AddOrChangeProfileRequest gur, DataSource xapsDs, DataSource syslogDs) throws RemoteException {
		try {

			ACSFactory acsWS = ACSWSFactory.getXAPSWS(gur.getLogin(), xapsDs, syslogDs);
			acs = acsWS.getAcs();
			if (gur.getUnittype() == null || gur.getProfile() == null)
				throw ACSFactory.error(logger, "No unittype or profile specified");
			Unittype unittype = acs.getUnittype(gur.getUnittype().getName());
			User user = acsWS.getId().getUser();
			boolean isAllowedToMakeProfile = user.getPermissions().getPermissions().length == 0;
			if (!isAllowedToMakeProfile) {
				Permission perm = user.getPermissions().getByUnittypeProfile(unittype.getId(), null);
				if (perm != null)
					isAllowedToMakeProfile = true;
			}

			Profile profileXAPS = null;
			if (unittype.getProfiles().getByName(gur.getProfile().getName()) == null) { // make new profile
				if (isAllowedToMakeProfile) {
					com.github.freeacs.ws.xml.Profile pWS = gur.getProfile();
					profileXAPS = new Profile(pWS.getName(), unittype);
					addOrChangeProfileImpl(profileXAPS, gur);
				} else {
					throw ACSFactory.error(logger, "The profile " + gur.getProfile().getName() + " does not exist, your login does not have the permissions to create it.");
				}
			} else { // change an existing one
				profileXAPS = acsWS.getProfileFromXAPS(unittype.getName(), gur.getProfile().getName());
				addOrChangeProfileImpl(profileXAPS, gur);
			}
			AddOrChangeProfileResponse response = new AddOrChangeProfileResponse();
			response.setProfile(ConvertACS2WS.convert(profileXAPS));
			return response;
		} catch (Throwable t) {
			if (t instanceof RemoteException)
				throw (RemoteException) t;
			else {
				throw ACSFactory.error(logger, t);
			}
		}

	}
}
