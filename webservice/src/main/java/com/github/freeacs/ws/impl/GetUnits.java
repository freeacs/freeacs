package com.github.freeacs.ws.impl;

import com.github.freeacs.dbi.*;
import com.github.freeacs.dbi.Parameter;
import com.github.freeacs.dbi.Parameter.Operator;
import com.github.freeacs.dbi.Parameter.ParameterDataType;
import com.github.freeacs.dbi.Profile;
import com.github.freeacs.dbi.Unit;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.ws.xml.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.rmi.RemoteException;
import java.util.*;
import java.util.Map.Entry;

public class GetUnits {
	private static final Logger logger = LoggerFactory.getLogger(GetUnits.class);

    private static final ObjectFactory factory = new ObjectFactory();

	public GetUnitsResponse getUnits(GetUnitsRequest gur, DataSource xapsDs, DataSource syslogDs) throws RemoteException {
		try {

            ACSFactory acsWS = ACSWSFactory.getXAPSWS(gur.getLogin(), xapsDs, syslogDs);
            ACS acs = acsWS.getAcs();
			ACSUnit acsUnit = acsWS.getXAPSUnit(acs);

			com.github.freeacs.ws.xml.Unit unitWS = gur.getUnit();

			/* Validate input - only allow permitted unittypes/profiles for this login */
			Unittype unittypeXAPS = null;
			List<Profile> profilesXAPS = new ArrayList<Profile>();
			if (unitWS.getUnittype() != null && unitWS.getUnittype().getName() != null) {
				unittypeXAPS = acsWS.getUnittypeFromXAPS(unitWS.getUnittype().getValue().getName());
				if (unitWS.getProfile() != null && unitWS.getProfile().getName() != null) {
					profilesXAPS.add(acsWS.getProfileFromXAPS(unittypeXAPS.getName(), unitWS.getProfile().getValue().getName()));
				} else
					profilesXAPS = Arrays.asList(unittypeXAPS.getProfiles().getProfiles());
			}
			boolean useCase3 = unitWS.getParameters() != null && unitWS.getParameters().getValue().getParameterArray().getItem().size() > 0;
			if (useCase3) {
				if (profilesXAPS.size() == 0) {
					throw ACSFactory.error(logger, "Unittype and profiles are not specified, not possible to execute parameter-search");
				}
			}

			/* Input is validated - now execute searches */
			Map<String, Unit> unitMap = new TreeMap<String, Unit>();
			if (unitWS.getUnitId() != null) { // Use-case 1
				Unit unitXAPS = acsUnit.getUnitById(unitWS.getUnitId().getValue());
				if (unitXAPS != null)
					unitMap.put(unitWS.getUnitId().getValue(), unitXAPS);
			} else if (useCase3) {// Use-case 3, expect parameters and unittype
				List<Parameter> upList = validateParameters(unitWS, profilesXAPS);
				Map<String, Unit> tmpMap = acsUnit.getUnits(unittypeXAPS, profilesXAPS, upList, 51);
				for (Unit unitXAPS : tmpMap.values())
					unitMap.put(unitXAPS.getId(), acsUnit.getUnitById(unitXAPS.getId()));
			} else { // Use-case 2
				Map<String, Unit> tmpMap = acsUnit.getUnits(unitWS.getSerialNumber().getValue(), profilesXAPS, 51);
				for (Unit unitXAPS : tmpMap.values())
					unitMap.put(unitXAPS.getId(), acsUnit.getUnitById(unitXAPS.getId()));
			}

			/* Search is executed - now build response */
			boolean moreUnits = unitMap.size() > 50;
			UnitList ul = new UnitList();
			com.github.freeacs.ws.xml.Unit[] unitArray = new com.github.freeacs.ws.xml.Unit[unitMap.size()];
            ArrayOfUnit array = new ArrayOfUnit();
            array.getItem().addAll(Arrays.asList(unitArray));
            ul.setUnitArray(array);
			int ucount = 0;
			for (Unit unit : unitMap.values()) {
				Unittype utXAPS = unit.getUnittype();
				com.github.freeacs.ws.xml.Unittype utWS = new com.github.freeacs.ws.xml.Unittype();
				utWS.setName(utXAPS.getName());
				utWS.setVendor(factory.createUnittypeVendor(utXAPS.getVendor()));
				utWS.setDescription(factory.createUnittypeDescription(utXAPS.getDescription()));
				utWS.setProtocol(factory.createUnittypeProtocol(utXAPS.getProtocol().toString()));
				Profile pXAPS = unit.getProfile();
				com.github.freeacs.ws.xml.Profile pWS = new com.github.freeacs.ws.xml.Profile();
				pWS.setName(pXAPS.getName());
				UnittypeParameter snUtp = getSerialNumberUtp(utXAPS);
				String serialNumber = null;
				Map<String, String> unitParams = unit.getParameters();
				if (snUtp != null)
					serialNumber = unitParams.get(snUtp.getName());
				com.github.freeacs.ws.xml.Parameter[] parameterArray = new com.github.freeacs.ws.xml.Parameter[unitParams.size()];
				int pcount = 0;
				for (Entry<String, String> entry : unitParams.entrySet()) {
					String flags = "U";
					if (unit.getUnitParameters().get(entry.getKey()) == null)
						flags = "P";
					com.github.freeacs.ws.xml.Parameter paramWS = new com.github.freeacs.ws.xml.Parameter();
					paramWS.setName(entry.getKey());
					paramWS.setValue(factory.createParameterValue(entry.getValue()));
					paramWS.setFlags(factory.createParameterFlags(flags));
					parameterArray[pcount++] = paramWS;
				}
                ArrayOfParameter arrayOfParameter = new ArrayOfParameter();
				arrayOfParameter.getItem().addAll(Arrays.asList(parameterArray));
                ParameterList parameterList = new ParameterList();
                parameterList.setParameterArray(arrayOfParameter);
				com.github.freeacs.ws.xml.Unit uWS = new com.github.freeacs.ws.xml.Unit();
				uWS.setUnitId(factory.createUnitUnitId(unit.getId()));
				uWS.setSerialNumber(factory.createUnitSerialNumber(serialNumber));
				uWS.setProfile(factory.createUnitProfile(pWS));
				uWS.setUnittype(factory.createUnitUnittype(utWS));
				uWS.setParameters(factory.createUnitParameters(parameterList));
				unitArray[ucount++] = uWS;
			}
			GetUnitsResponse response = new GetUnitsResponse();
            response.setUnits(factory.createGetUnitsResponseUnits(ul));
            response.setMoreUnits(moreUnits);
            return response;
		} catch (Throwable t) {
			if (t instanceof RemoteException)
				throw (RemoteException) t;
			else {
				throw ACSFactory.error(logger, t);
			}
		}
	}

	private Unittype getUnittypeForParameters(List<Profile> allowedProfiles) throws RemoteException {
		Unittype unittype = allowedProfiles.get(0).getUnittype();
		for (Profile p : allowedProfiles) {
			if (!p.getUnittype().getName().equals(unittype.getName()))
				throw ACSFactory.error(logger, "Cannot specify parameters or SerialNumber without specifying Unittype"); // there are more than 1 unittype - indicating no unittype has been specified
		}
		return unittype;
	}

	private List<Parameter> validateParameters(com.github.freeacs.ws.xml.Unit unitWS, List<Profile> allowedProfiles) throws RemoteException {
		if (allowedProfiles == null || allowedProfiles.size() == 0)
			throw ACSFactory.error(logger, "Unittype and profiles are not specified, not possible to make parameter-search");
		List<Parameter> parameters = new ArrayList<Parameter>();
		if (unitWS.getParameters() != null && unitWS.getParameters().getValue().getParameterArray() != null) {
			Unittype unittype = getUnittypeForParameters(allowedProfiles);
			for (com.github.freeacs.ws.xml.Parameter pWS : unitWS.getParameters().getValue().getParameterArray().getItem()) {
				UnittypeParameter utp = unittype.getUnittypeParameters().getByName(pWS.getName());
				if (utp == null)
					throw ACSFactory.error(logger, "Unittype parameter " + pWS.getName() + " is not found in unittype " + unittype.getName());
				//				boolean equal = true;
				ParameterDataType pdt = ParameterDataType.TEXT;
				Operator op = Operator.EQ;
				if (pWS.getFlags() != null) {
					String[] opTypeArr = pWS.getFlags().getValue().split(",");
					try {
						op = Operator.getOperatorFromLiteral(opTypeArr[0]);
						if (opTypeArr.length == 2)
							pdt = ParameterDataType.getDataType(opTypeArr[1]);
					} catch (IllegalArgumentException iae) {
						throw ACSFactory.error(logger, "An error occurred in flag (" + pWS.getFlags() + "): " + iae.getMessage());
					}
				}
				Parameter pXAPS = new Parameter(utp, pWS.getValue().getValue(), op, pdt);
				parameters.add(pXAPS);
			}
		}
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
