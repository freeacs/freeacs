package com.owera.xapsws.impl;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.owera.common.log.Logger;
import com.owera.xaps.dbi.Parameter;
import com.owera.xaps.dbi.Parameter.Operator;
import com.owera.xaps.dbi.Parameter.ParameterDataType;
import com.owera.xaps.dbi.Profile;
import com.owera.xaps.dbi.Unit;
import com.owera.xaps.dbi.Unittype;
import com.owera.xaps.dbi.UnittypeParameter;
import com.owera.xaps.dbi.XAPS;
import com.owera.xaps.dbi.XAPSUnit;

import com.owera.xapsws.*;

public class GetUnits {
	private static Logger logger = new Logger();

	private XAPS xaps;
	private XAPSWS xapsWS;

	public GetUnitsResponse getUnits(GetUnitsRequest gur) throws RemoteException {
		try {
			
			xapsWS = XAPSWSFactory.getXAPSWS(gur.getLogin());
			xaps = xapsWS.getXAPS();
			XAPSUnit xapsUnit = xapsWS.getXAPSUnit(xaps);

			com.owera.xapsws.Unit unitWS = gur.getUnit();

			/* Validate input - only allow permitted unittypes/profiles for this login */
			Unittype unittypeXAPS = null;
			List<Profile> profilesXAPS = new ArrayList<Profile>();
			if (unitWS.getUnittype() != null && unitWS.getUnittype().getName() != null) {
				unittypeXAPS = xapsWS.getUnittypeFromXAPS(unitWS.getUnittype().getName());
				if (unitWS.getProfile() != null && unitWS.getProfile().getName() != null) {
					profilesXAPS.add(xapsWS.getProfileFromXAPS(unittypeXAPS.getName(), unitWS.getProfile().getName()));
				} else
					profilesXAPS = Arrays.asList(unittypeXAPS.getProfiles().getProfiles());
			}
			boolean useCase3 = unitWS.getParameters() != null && unitWS.getParameters().getParameterArray().getItem().length > 0;
			if (useCase3) {
				if (profilesXAPS.size() == 0) {
					throw XAPSWS.error(logger, "Unittype and profiles are not specified, not possible to execute parameter-search");
				}
			}

			/* Input is validated - now execute searches */
			Map<String, Unit> unitMap = new TreeMap<String, Unit>();
			if (unitWS.getUnitId() != null) { // Use-case 1
				Unit unitXAPS = xapsUnit.getUnitById(unitWS.getUnitId());
				if (unitXAPS != null)
					unitMap.put(unitWS.getUnitId(), unitXAPS);
			} else if (useCase3) {// Use-case 3, expect parameters and unittype
				List<Parameter> upList = validateParameters(unitWS, profilesXAPS);
				Map<String, Unit> tmpMap = xapsUnit.getUnits(unittypeXAPS, profilesXAPS, upList, 51);
				for (Unit unitXAPS : tmpMap.values())
					unitMap.put(unitXAPS.getId(), xapsUnit.getUnitById(unitXAPS.getId()));
			} else { // Use-case 2
				Map<String, Unit> tmpMap = xapsUnit.getUnits(unitWS.getSerialNumber(), profilesXAPS, 51);
				for (Unit unitXAPS : tmpMap.values())
					unitMap.put(unitXAPS.getId(), xapsUnit.getUnitById(unitXAPS.getId()));
			}
			
			/* Search is executed - now build response */
			boolean moreUnits = unitMap.size() > 50;
			UnitList ul = new UnitList();
			com.owera.xapsws.Unit[] unitArray = new com.owera.xapsws.Unit[unitMap.size()];
			ul.setUnitArray(new ArrayOfUnit(unitArray));
			int ucount = 0;
			for (Unit unit : unitMap.values()) {
				Unittype utXAPS = unit.getUnittype();
				com.owera.xapsws.Unittype utWS = new com.owera.xapsws.Unittype(utXAPS.getName(), null, utXAPS.getVendor(), utXAPS.getDescription(), utXAPS.getProtocol().toString(), null);
				Profile pXAPS = unit.getProfile();
				com.owera.xapsws.Profile pWS = new com.owera.xapsws.Profile(pXAPS.getName(), null);
				UnittypeParameter snUtp = getSerialNumberUtp(utXAPS);
				String serialNumber = null;
				Map<String, String> unitParams = unit.getParameters();
				if (snUtp != null)
					serialNumber = unitParams.get(snUtp.getName());
				com.owera.xapsws.Parameter[] parameterArray = new com.owera.xapsws.Parameter[unitParams.size()];
				int pcount = 0;
				for (Entry<String, String> entry : unitParams.entrySet()) {
					String flags = "U";
					if (unit.getUnitParameters().get(entry.getKey()) == null)
						flags = "P";
					com.owera.xapsws.Parameter paramWS = new com.owera.xapsws.Parameter(entry.getKey(), entry.getValue(), flags);
					parameterArray[pcount++] = paramWS;
				}
				ParameterList parameterList = new ParameterList(new ArrayOfParameter(parameterArray));
				com.owera.xapsws.Unit uWS = new com.owera.xapsws.Unit(unit.getId(), serialNumber, pWS, utWS, parameterList);
				unitArray[ucount++] = uWS;
			}
			return new GetUnitsResponse(ul, moreUnits);
		} catch (Throwable t) {
			if (t instanceof RemoteException)
				throw (RemoteException) t;
			else {
				throw XAPSWS.error(logger, t);
			}
		}
	}

	//	private List<Profile> validatedProfiles(com.owera.xapsws.Unit unitWS) throws RemoteException {
	//		Unittype unittype = null;
	//		if (unitWS.getUnittype() != null) {
	//			unittype = xapsWS.getUnittypeFromXAPS(unitWS.getUnittype().getName());
	//			if (unitWS.getProfile() != null && unitWS.getProfile().getName() != null) {
	//				List<Profile> allowedProfiles = new ArrayList<Profile>();
	//				allowedProfiles.add(xapsWS.getProfileFromXAPS(unittype.getName(), unitWS.getProfile().getName()));
	//				return allowedProfiles;
	//			} else
	//				return Arrays.asList(unittype.getProfiles().getProfiles());
	//		}
	//		return null;
	//		//			return xaps.getAllowedProfiles(unittype);
	//	}

	private Unittype getUnittypeForParameters(List<Profile> allowedProfiles) throws RemoteException {
		Unittype unittype = allowedProfiles.get(0).getUnittype();
		for (Profile p : allowedProfiles) {
			if (!p.getUnittype().getName().equals(unittype.getName()))
				throw XAPSWS.error(logger, "Cannot specify parameters or SerialNumber without specifying Unittype"); // there are more than 1 unittype - indicating no unittype has been specified
		}
		return unittype;
	}

	private List<Parameter> validateParameters(com.owera.xapsws.Unit unitWS, List<Profile> allowedProfiles) throws RemoteException {
		if (allowedProfiles == null || allowedProfiles.size() == 0)
			throw XAPSWS.error(logger, "Unittype and profiles are not specified, not possible to make parameter-search");
		List<Parameter> parameters = new ArrayList<Parameter>();
		if (unitWS.getParameters() != null && unitWS.getParameters().getParameterArray() != null) {
			Unittype unittype = getUnittypeForParameters(allowedProfiles);
			for (com.owera.xapsws.Parameter pWS : unitWS.getParameters().getParameterArray().getItem()) {
				UnittypeParameter utp = unittype.getUnittypeParameters().getByName(pWS.getName());
				if (utp == null)
					throw XAPSWS.error(logger, "Unittype parameter " + pWS.getName() + " is not found in unittype " + unittype.getName());
				//				boolean equal = true;
				ParameterDataType pdt = ParameterDataType.TEXT;
				Operator op = Operator.EQ;
				if (pWS.getFlags() != null) {
					String[] opTypeArr = pWS.getFlags().split(",");
					try {
						op = Operator.getOperatorFromLiteral(opTypeArr[0]);
						if (opTypeArr.length == 2)
							pdt = ParameterDataType.getDataType(opTypeArr[1]);
					} catch (IllegalArgumentException iae) {
						throw XAPSWS.error(logger, "An error occurred in flag (" + pWS.getFlags() + "): " + iae.getMessage());
					}
				}
				Parameter pXAPS = new Parameter(utp, pWS.getValue(), op, pdt);
				parameters.add(pXAPS);
			}
		}
		//		if (unitWS.getSerialNumber() != null) {
		//			Unittype unittype = getUnittypeForParameters(allowedProfiles);
		//			UnittypeParameter serialNumberUtp = getSerialNumberUtp(unittype);
		//			if (serialNumberUtp == null)
		//				throw XAPSWS.error(logger, "SerialNumber unittype parameter does not exist!");
		//			parameters.add(new Parameter(serialNumberUtp, unitWS.getSerialNumber(), true));
		//		}
		return parameters;
	}

	private UnittypeParameter getSerialNumberUtp(Unittype unittype) {
		String snName = "InternetGatewayDevice.DeviceInfo.SerialNumber";
		UnittypeParameter serialNumberUtp = unittype.getUnittypeParameters().getByName(snName);
		if (serialNumberUtp == null) {
			snName = "Device.DeviceInfo.SerialNumber";
			serialNumberUtp = unittype.getUnittypeParameters().getByName(snName);
		}
		return serialNumberUtp;
	}
}
