package com.github.freeacs.ws.impl;

import com.github.freeacs.dbi.*;

import com.github.freeacs.dbi.Unittype.ProvisioningProtocol;
import com.github.freeacs.ws.AddOrChangeUnittypeRequest;
import com.github.freeacs.ws.AddOrChangeUnittypeResponse;
import com.github.freeacs.ws.Parameter;
import com.github.freeacs.ws.ParameterList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AddOrChangeUnittype {

	private static final Logger logger = LoggerFactory.getLogger(AddOrChangeUnittype.class);

	private ACS acs;
	private ACSWS acsWS;

	private void addOrChangeUnittypeImpl(Unittype unittypeXAPS, AddOrChangeUnittypeRequest gur) throws SQLException, RemoteException {
		unittypeXAPS.setDescription(gur.getUnittype().getDescription());
		//		unittypeXAPS.setMatcherId(gur.getUnittype().getMatcherId());
		unittypeXAPS.setProtocol(ProvisioningProtocol.toEnum(gur.getUnittype().getProtocol()));
		unittypeXAPS.setVendor(gur.getUnittype().getVendor());
		ParameterList parameterList = gur.getUnittype().getParameters();
		List<UnittypeParameter> acUtpList = new ArrayList<UnittypeParameter>();
		List<UnittypeParameter> dUtpList = new ArrayList<UnittypeParameter>();
		if (parameterList != null && parameterList.getParameterArray() != null) {
			for (Parameter param : parameterList.getParameterArray().getItem()) {
				UnittypeParameter utp = unittypeXAPS.getUnittypeParameters().getByName(param.getName());
				if (param.getFlags() != null && !param.getFlags().equals("D") && !param.getFlags().equals("AC"))
					throw ACSWS.error(logger, "Flag for parameter " + param.getName() + " had value " + param.getFlags() + ", but must be either D or AC");
				if (param.getFlags() == null || param.getFlags().equals("AC")) {
					if (utp == null)
						acUtpList.add(new UnittypeParameter(unittypeXAPS, param.getName(), new UnittypeParameterFlag(param.getValue())));
					else {
						utp.setFlag(new UnittypeParameterFlag(param.getValue()));
						acUtpList.add(utp);
					}
				} else if (param.getFlags().equals("D") && utp != null) {
					dUtpList.add(utp);
				}
			}
		}
		//		System.out.println("AOC: Unitypes object: " + xapsWS.getXAPS().getUnittypes());
		acsWS.getAcs().getUnittypes().addOrChangeUnittype(unittypeXAPS, acs);
		for (UnittypeParameter utp : dUtpList) {
			//			System.out.println("D: Unittype: " + unittypeXAPS + ", UTP:" + utp.getName() + ", " + utp.getFlag().getFlag());
			unittypeXAPS.getUnittypeParameters().deleteUnittypeParameter(utp, acs);
		}
		for (UnittypeParameter utp : acUtpList) {
			//			System.out.println("AOC: Unittype: " + unittypeXAPS + ", UTP:" + utp.getName() + ", " + utp.getFlag().getFlag());
			unittypeXAPS.getUnittypeParameters().addOrChangeUnittypeParameter(utp, acs);
		}
	}

	public AddOrChangeUnittypeResponse addOrChangeUnittype(AddOrChangeUnittypeRequest gur, DataSource xapsDs, DataSource syslogDs) throws RemoteException {
		try {
			
			acsWS = ACSWSFactory.getXAPSWS(gur.getLogin(),xapsDs,syslogDs);
			acs = acsWS.getAcs();
			User user = acsWS.getId().getUser();
			if (gur.getUnittype() == null || gur.getUnittype().getName() == null)
				throw ACSWS.error(logger, "No unittype name specified");
			boolean isAdmin = user.getPermissions().getPermissions().length == 0;
			Unittypes unittypes = acs.getUnittypes();
			Unittype unittypeXAPS = null;
			if (unittypes.getByName(gur.getUnittype().getName()) == null) { // make new unittype
				if (isAdmin) {// allow if login is admin
					com.github.freeacs.ws.Unittype uWS = gur.getUnittype();
					unittypeXAPS = new Unittype(uWS.getName(), uWS.getVendor(), uWS.getDescription(), ProvisioningProtocol.toEnum(uWS.getProtocol()));
					addOrChangeUnittypeImpl(unittypeXAPS, gur);
				} else {
					throw ACSWS.error(logger, "The unittype " + gur.getUnittype().getName() + " does not exist, your login does not have the permissions to create it.");
				}
			} else { // change an existing one
				unittypeXAPS = acsWS.getUnittypeFromXAPS(gur.getUnittype().getName());
				addOrChangeUnittypeImpl(unittypeXAPS, gur);
			}
			return new AddOrChangeUnittypeResponse(ConvertACS2WS.convert(unittypeXAPS));
		} catch (Throwable t) {
			if (t instanceof RemoteException)
				throw (RemoteException) t;
			else {
				throw ACSWS.error(logger, t);
			}
		}

	}
}
