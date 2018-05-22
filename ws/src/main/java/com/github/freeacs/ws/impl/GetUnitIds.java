package com.github.freeacs.ws.impl;

import com.github.freeacs.dbi.*;

import com.github.freeacs.dbi.Parameter.Operator;
import com.github.freeacs.dbi.Parameter.ParameterDataType;
import com.github.freeacs.ws.ArrayOfUnitId;
import com.github.freeacs.ws.GetUnitIdsRequest;
import com.github.freeacs.ws.GetUnitIdsResponse;
import com.github.freeacs.ws.UnitIdList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.rmi.RemoteException;
import java.util.*;

public class GetUnitIds {
	private static final Logger logger = LoggerFactory.getLogger(GetUnitIds.class);

	private ACS acs;
	private ACSWS acsWS;

	public GetUnitIdsResponse getUnits(GetUnitIdsRequest gur, DataSource xapsDs, DataSource syslogDs) throws RemoteException {
		try {
			
			acsWS = ACSWSFactory.getXAPSWS(gur.getLogin(), xapsDs,syslogDs);
			acs = acsWS.getAcs();
			ACSUnit acsUnit = acsWS.getXAPSUnit(acs);

			com.github.freeacs.ws.Unit unitWS = gur.getUnit();

			/* Validate input - only allow permitted unittypes/profiles for this login */
			Unittype unittypeXAPS = null;
			List<Profile> profilesXAPS = new ArrayList<Profile>();
			if (unitWS.getUnittype() != null && unitWS.getUnittype().getName() != null) {
				unittypeXAPS = acsWS.getUnittypeFromXAPS(unitWS.getUnittype().getName());
				if (unitWS.getProfile() != null && unitWS.getProfile().getName() != null) {
					profilesXAPS.add(acsWS.getProfileFromXAPS(unittypeXAPS.getName(), unitWS.getProfile().getName()));
				} else
					profilesXAPS = Arrays.asList(unittypeXAPS.getProfiles().getProfiles());
			}
			boolean useCase3 = unitWS.getParameters() != null && unitWS.getParameters().getParameterArray().getItem().length > 0;
			if (useCase3) {
				if (profilesXAPS.size() == 0) {
					throw ACSWS.error(logger, "Unittype and profiles are not specified, not possible to execute parameter-search");
				}
			}

			/* Input is validated - now execute searches */
			Map<String, Unit> unitMap = new TreeMap<String, Unit>();
			if (unitWS.getUnitId() != null) { // Use-case 1
				Unit unitXAPS = acsUnit.getUnitById(unitWS.getUnitId());
				if (unitXAPS != null)
					unitMap.put(unitWS.getUnitId(), unitXAPS);
			} else if (useCase3) {// Use-case 3, expect parameters and unittype
				List<Parameter> upList = validateParameters(unitWS, profilesXAPS);
				Map<String, Unit> tmpMap = acsUnit.getUnits(unittypeXAPS, profilesXAPS, upList, 51);
				for (Unit unitXAPS : tmpMap.values())
					unitMap.put(unitXAPS.getId(), acsUnit.getUnitById(unitXAPS.getId()));
			} else { // Use-case 2
				Map<String, Unit> tmpMap = acsUnit.getUnits(unitWS.getSerialNumber(), profilesXAPS, 51);
				for (Unit unitXAPS : tmpMap.values())
					unitMap.put(unitXAPS.getId(), acsUnit.getUnitById(unitXAPS.getId()));
			}

			

			String[] unitIdArray = new String[unitMap.size()];
			unitMap.keySet().toArray(unitIdArray);
			return new GetUnitIdsResponse(new UnitIdList(new ArrayOfUnitId(unitIdArray)));
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

	//	private List<Profile> validateProfiles(com.owera.xapsws.Unit unitWS) throws RemoteException {
	//		Unittype unittype = null;
	//		if (unitWS.getUnittype() != null) {
	//			unittype = xapsWS.getUnittypeFromXAPS(unitWS.getUnittype().getName());
	//			if (unitWS.getProfile() != null && unitWS.getProfile().getName() != null) {
	//				List<Profile> allowedProfiles = new ArrayList<Profile>();
	//				allowedProfiles.add(xapsWS.getProfileFromXAPS(unittype.getName(), unitWS.getProfile().getName()));
	//				return allowedProfiles;
	//			} else
	//				return Arrays.asList(unittype.getProfiles().getProfiles());
	//		} else
	//			return null;
	//		//			return xaps.getAllowedProfiles(unittype);
	//	}

	private Unittype getUnittypeForParameters(List<Profile> allowedProfiles) throws RemoteException {
		Unittype unittype = allowedProfiles.get(0).getUnittype();
		for (Profile p : allowedProfiles) {
			if (!p.getUnittype().getName().equals(unittype.getName()))
				// there are more than 1 unittype - indicating no unittype has been specified
				throw ACSWS.error(logger, "Cannot specify parameters or SerialNumber without specifying Unittype");
		}
		return unittype;
	}

	private List<Parameter> validateParameters(com.github.freeacs.ws.Unit unitWS, List<Profile> allowedProfiles) throws RemoteException {
		if (allowedProfiles == null || allowedProfiles.size() == 0)
			throw ACSWS.error(logger, "Unittype and profiles are not specified, not possible to make parameter-search");
		List<Parameter> parameters = new ArrayList<Parameter>();
		if (unitWS.getParameters() != null && unitWS.getParameters().getParameterArray() != null) {
			Unittype unittype = getUnittypeForParameters(allowedProfiles);
			for (com.github.freeacs.ws.Parameter pWS : unitWS.getParameters().getParameterArray().getItem()) {
				UnittypeParameter utp = unittype.getUnittypeParameters().getByName(pWS.getName());
				if (utp == null)
					throw ACSWS.error(logger, "Unittype parameter " + pWS.getName() + " is not found in unittype " + unittype.getName());
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
						throw ACSWS.error(logger, "An error occurred in flag (" + pWS.getFlags() + "): " + iae.getMessage());
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

	//	private UnittypeParameter getSerialNumberUtp(Unittype unittype) {
	//		
	//		String snName = SystemParameters.MAC;
	//		UnittypeParameter serialNumberUtp = unittype.getUnittypeParameters().getByName(snName);
	//		if (serialNumberUtp == null) {
	//			snName = "InternetGatewayDevice.DeviceInfo.SerialNumber";
	//			serialNumberUtp = unittype.getUnittypeParameters().getByName(snName);
	//		}
	//		if (serialNumberUtp == null) {
	//			snName = "Device.DeviceInfo.SerialNumber";
	//			serialNumberUtp = unittype.getUnittypeParameters().getByName(snName);
	//		}
	//		return serialNumberUtp;
	//	}
}
