package com.owera.xapsws.impl;

import com.github.freeacs.dbi.*;
import com.owera.xaps.dbi.*;
import com.github.freeacs.dbi.Unittype.ProvisioningProtocol;
import com.owera.xapsws.AddOrChangeUnittypeRequest;
import com.owera.xapsws.AddOrChangeUnittypeResponse;
import com.owera.xapsws.Parameter;
import com.owera.xapsws.ParameterList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AddOrChangeUnittype {

	private static final Logger logger = LoggerFactory.getLogger(AddOrChangeUnittype.class);

	private XAPS xaps;
	private XAPSWS xapsWS;

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
					throw XAPSWS.error(logger, "Flag for parameter " + param.getName() + " had value " + param.getFlags() + ", but must be either D or AC");
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
		xapsWS.getXAPS().getUnittypes().addOrChangeUnittype(unittypeXAPS, xaps);
		for (UnittypeParameter utp : dUtpList) {
			//			System.out.println("D: Unittype: " + unittypeXAPS + ", UTP:" + utp.getName() + ", " + utp.getFlag().getFlag());
			unittypeXAPS.getUnittypeParameters().deleteUnittypeParameter(utp, xaps);
		}
		for (UnittypeParameter utp : acUtpList) {
			//			System.out.println("AOC: Unittype: " + unittypeXAPS + ", UTP:" + utp.getName() + ", " + utp.getFlag().getFlag());
			unittypeXAPS.getUnittypeParameters().addOrChangeUnittypeParameter(utp, xaps);
		}
	}

	public AddOrChangeUnittypeResponse addOrChangeUnittype(AddOrChangeUnittypeRequest gur) throws RemoteException {
		try {
			
			xapsWS = XAPSWSFactory.getXAPSWS(gur.getLogin());
			xaps = xapsWS.getXAPS();
			User user = xapsWS.getId().getUser();
			if (gur.getUnittype() == null || gur.getUnittype().getName() == null)
				throw XAPSWS.error(logger, "No unittype name specified");
			boolean isAdmin = user.getPermissions().getPermissions().length == 0;
			Unittypes unittypes = xaps.getUnittypes();
			Unittype unittypeXAPS = null;
			if (unittypes.getByName(gur.getUnittype().getName()) == null) { // make new unittype
				if (isAdmin) {// allow if login is admin
					com.owera.xapsws.Unittype uWS = gur.getUnittype();
					unittypeXAPS = new Unittype(uWS.getName(), uWS.getVendor(), uWS.getDescription(), ProvisioningProtocol.toEnum(uWS.getProtocol()));
					addOrChangeUnittypeImpl(unittypeXAPS, gur);
				} else {
					throw XAPSWS.error(logger, "The unittype " + gur.getUnittype().getName() + " does not exist, your login does not have the permissions to create it.");
				}
			} else { // change an existing one
				unittypeXAPS = xapsWS.getUnittypeFromXAPS(gur.getUnittype().getName());
				addOrChangeUnittypeImpl(unittypeXAPS, gur);
			}
			return new AddOrChangeUnittypeResponse(ConvertXAPS2WS.convert(unittypeXAPS));
		} catch (Throwable t) {
			if (t instanceof RemoteException)
				throw (RemoteException) t;
			else {
				throw XAPSWS.error(logger, t);
			}
		}

	}
}
