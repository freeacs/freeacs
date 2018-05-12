package com.github.freeacs.ws.impl;

import com.github.freeacs.common.db.NoAvailableConnectionException;
import com.github.freeacs.dbi.*;

import com.github.freeacs.ws.AddOrChangeUnitRequest;
import com.github.freeacs.ws.AddOrChangeUnitResponse;
import com.github.freeacs.ws.Parameter;
import com.github.freeacs.ws.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AddOrChangeUnit {
	private static final Logger logger = LoggerFactory.getLogger(AddOrChangeUnit.class);

	private XAPS xaps;
	private XAPSWS xapsWS;
	private XAPSUnit xapsUnit;

	public AddOrChangeUnitResponse addOrChangeUnit(AddOrChangeUnitRequest aocur) throws RemoteException {
		try {
			
			xapsWS = XAPSWSFactory.getXAPSWS(aocur.getLogin());
			xaps = xapsWS.getXAPS();
			xapsUnit = xapsWS.getXAPSUnit(xaps);

			/* 
			 * We need to support these use cases
			 * UC1 - Add unit:
			 *  unittype (must exist in xAPS)
			 *  profile (must exist in xAPS) 
			 *  unitId (should be <OUI>-<ProductClass>-<SerialNumber>
			 *  secret (must match protocol pattern)
			 * UC2 - Change unit: 
			 *  unittype (must exist in xAPS)
			 *  profile (must exist in xAPS) 
			 * 	unitId (must match protocol pattern)
			 * UC3 - Change unit:
			 *  unittype (must exist in xAPS)
			 *  profile (must exist in xAPS) 
			 * 	serialNumber (no requirements)
			 * 
			 * To find out which UC it is, use following algorithm:
			 * 1. If uniqueId is present -> UC2
			 * 2. If serialNumber is present, but no unit Id is present -> UC4
			 * 3. UC1 and UC3 are basically the same when it comes to logic performed
			 * 
			 * Additional rules: 
			 * 1. unittype and profile must be specified and must exist in xAPS
			 * 2. If a secret is specified it must adhere to the protocol pattern
			 * 3. If a unit id is specified it must adhere to the protocol pattern
			 */
			Unit unitWS = aocur.getUnit();
			if (unitWS.getUnittype() == null || unitWS.getProfile() == null)
				throw XAPSWS.error(logger, "Unittype and/or Profile object are missing");
			Profile profile = xapsWS.getProfileFromXAPS(unitWS.getUnittype().getName(), unitWS.getProfile().getName());
			String unitId = validateUnitId(unitWS, profile.getUnittype(), profile);
			List<String> unitIds = new ArrayList<String>();
			unitIds.add(unitId);
			List<UnitParameter> acParams = validateAddOrChangeUnitParameters(unitWS, profile.getUnittype(), profile);
			List<UnitParameter> dParams = validateDeleteUnitParameters(unitWS, profile.getUnittype(), profile);
			xapsUnit.addUnits(unitIds, profile);
			xapsUnit.addOrChangeUnitParameters(acParams, profile);
			xapsUnit.deleteUnitParameters(dParams);
			return new AddOrChangeUnitResponse(unitWS);
		} catch (Throwable t) {
			if (t instanceof RemoteException)
				throw (RemoteException) t;
			else {
				throw XAPSWS.error(logger, t);
			}
		}
	}

	private String validateUnitId(Unit unitWS, Unittype unittype, Profile profile) throws SQLException, NoAvailableConnectionException, RemoteException {
		if (unitWS.getUnitId() == null) {
			if (unitWS.getSerialNumber() != null) {
				com.github.freeacs.dbi.Unit unitXAPS = xapsWS.getUnitByMAC(xapsUnit, unittype, profile, unitWS.getSerialNumber());
				if (unitXAPS != null) {
					unitWS.setUnitId(unitXAPS.getId());
				}
			}
			if (unitWS.getUnitId() == null)
				XAPSWS.error(logger, "No unitId or serial number is supplied to the service");
		}
		return unitWS.getUnitId();
	}

	private List<UnitParameter> validateDeleteUnitParameters(Unit unitWS, Unittype unittype, Profile profile) throws RemoteException {
		List<UnitParameter> unitParams = new ArrayList<UnitParameter>();
		Parameter[] parameters = unitWS.getParameters().getParameterArray().getItem();
		for (Parameter p : parameters) {
			UnittypeParameter utp = unittype.getUnittypeParameters().getByName(p.getName());
			if (utp == null) {
				throw XAPSWS.error(logger, "Unittype parameter " + p.getName() + " is not found in unittype " + unittype.getName());
			} else {
				if (p.getFlags() != null && p.getFlags().equals("D")) {
					unitParams.add(new UnitParameter(utp, unitWS.getUnitId(), p.getValue(), profile));
				}
			}
		}
		return unitParams;
	}

	private List<UnitParameter> validateAddOrChangeUnitParameters(Unit unitWS, Unittype unittype, Profile profile) throws RemoteException {
		List<UnitParameter> unitParams = new ArrayList<UnitParameter>();
		Parameter[] parameters = unitWS.getParameters().getParameterArray().getItem();
		for (Parameter p : parameters) {
			UnittypeParameter utp = unittype.getUnittypeParameters().getByName(p.getName());
			if (utp == null) {
				throw XAPSWS.error(logger, "Unittype parameter " + p.getName() + " is not found in unittype " + unittype.getName());
			} else {
				if (p.getFlags() == null || p.getFlags().equals("AC")) {
					unitParams.add(new UnitParameter(utp, unitWS.getUnitId(), p.getValue(), profile));
				}
			}
		}
		return unitParams;
	}

	//	private String getOPPUnitId(Unit unitWS, Unittype unittype, Profile profile) throws RemoteException, SQLException, NoAvailableConnectionException {
	//		if (unitWS.getParameters() != null && unitWS.getParameters().getParameterArray() != null) {
	//			String namespace = null;
	//			String uniqueId = null;
	//			List<Parameter> parameterList = new ArrayList<Parameter>();
	//			for (Parameter p : unitWS.getParameters().getParameterArray()) {
	//				if (p.getName().equals(SystemParameters.OPP_NAMESPACE))
	//					namespace = p.getValue();
	//				else if (p.getName().equals(SystemParameters.OPP_UNIQUE_ID))
	//					uniqueId = p.getValue();
	//				else
	//					parameterList.add(p);
	//			}
	//			if (namespace != null && uniqueId == null)
	//				XAPSWS.error(logger, "Namespace is defined, but no uniqueId is supplied - cannot generate OPP Unit id");
	//			if (namespace == null && uniqueId != null)
	//				XAPSWS.error(logger, "UniqueId is defined, but no namespace is supplied - cannot generate OPP Unit id");
	//			if (namespace == null && uniqueId == null)
	//				return null;
	//
	//			String unitIdSeed = "opp://" + namespace + "?uniqueid=" + uniqueId;
	//			UUID uuid = UUID.nameUUIDFromBytes(unitIdSeed.getBytes());
	//			com.owera.xaps.dbi.Unit unitXAPS = xapsUnit.getUnitById(uuid.toString(), unittype, profile);
	//			if (unitXAPS == null) {
	//				String oppSecret = "";
	//				byte[] secret = new byte[36];
	//				xapsWS.random.nextBytes(secret);
	//				for (byte b : secret) {
	//					String hex = Integer.toHexString(b + 128);
	//					if (hex.length() == 1)
	//						hex = "0" + hex;
	//					oppSecret += hex;
	//				}
	//				oppSecret = oppSecret.toUpperCase();
	//				parameterList.add(new Parameter("System.X_OWERA-COM.OPP.Connector.SASecret.Unit", oppSecret, null));
	//				parameterList.add(new Parameter("System.X_OWERA-COM.Secret", oppSecret, null));
	//			}
	//			Parameter[] parameterArrayModified = new Parameter[parameterList.size()];
	//			parameterList.toArray(parameterArrayModified);
	//			unitWS.getParameters().setParameterArray(parameterArrayModified);
	//			return uuid.toString();
	//		} else {
	//			return null;
	//		}
	//
	//	}

}
