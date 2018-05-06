/**
 * XAPSWS_BindingImpl.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.owera.xapsws;

import com.owera.xapsws.impl.AddOrChangeProfile;
import com.owera.xapsws.impl.AddOrChangeUnit;
import com.owera.xapsws.impl.AddOrChangeUnittype;
import com.owera.xapsws.impl.DeleteProfile;
import com.owera.xapsws.impl.DeleteUnit;
import com.owera.xapsws.impl.DeleteUnittype;
import com.owera.xapsws.impl.GetProfiles;
import com.owera.xapsws.impl.GetUnitIds;
import com.owera.xapsws.impl.GetUnits;
import com.owera.xapsws.impl.GetUnittypes;

public class XAPSWS_BindingImpl implements com.owera.xapsws.XAPSWS_PortType {
	public com.owera.xapsws.GetUnittypesResponse getUnittypes(com.owera.xapsws.GetUnittypesRequest gur) throws java.rmi.RemoteException {
		GetUnittypes getUnittypes = new GetUnittypes();
		return getUnittypes.getUnittypes(gur);
	}

	public com.owera.xapsws.AddOrChangeUnittypeResponse addOrChangeUnittype(com.owera.xapsws.AddOrChangeUnittypeRequest wsm1) throws java.rmi.RemoteException {
		AddOrChangeUnittype xapsws = new AddOrChangeUnittype();
		return xapsws.addOrChangeUnittype(wsm1);
	}

	public com.owera.xapsws.DeleteUnittypeResponse deleteUnittype(com.owera.xapsws.DeleteUnittypeRequest wsm3) throws java.rmi.RemoteException {
		DeleteUnittype xapsws = new DeleteUnittype();
		return xapsws.deleteUnittype(wsm3);
	}

	public com.owera.xapsws.GetProfilesResponse getProfiles(com.owera.xapsws.GetProfilesRequest gur) throws java.rmi.RemoteException {
		GetProfiles getProfiles = new GetProfiles();
		return getProfiles.getProfiles(gur);
	}

	public com.owera.xapsws.AddOrChangeProfileResponse addOrChangeProfile(com.owera.xapsws.AddOrChangeProfileRequest wsm1) throws java.rmi.RemoteException {
		AddOrChangeProfile xapsws = new AddOrChangeProfile();
		return xapsws.addOrChangeProfile(wsm1);
	}

	public com.owera.xapsws.DeleteProfileResponse deleteProfile(com.owera.xapsws.DeleteProfileRequest wsm3) throws java.rmi.RemoteException {
		DeleteProfile xapsws = new DeleteProfile();
		return xapsws.deleteProfile(wsm3);
	}

	public com.owera.xapsws.GetUnitsResponse getUnits(com.owera.xapsws.GetUnitsRequest wsm1) throws java.rmi.RemoteException {
		GetUnits getUnits = new GetUnits();
		return getUnits.getUnits(wsm1);
	}

	public com.owera.xapsws.AddOrChangeUnitResponse addOrChangeUnit(com.owera.xapsws.AddOrChangeUnitRequest wsm1) throws java.rmi.RemoteException {
		AddOrChangeUnit xapsws = new AddOrChangeUnit();
		return xapsws.addOrChangeUnit(wsm1);
	}

	public com.owera.xapsws.DeleteUnitResponse deleteUnit(com.owera.xapsws.DeleteUnitRequest wsm3) throws java.rmi.RemoteException {
		DeleteUnit xapsws = new DeleteUnit();
		return xapsws.deleteUnit(wsm3);
	}

	public com.owera.xapsws.GetUnitIdsResponse getUnitIds(com.owera.xapsws.GetUnitIdsRequest wsm1) throws java.rmi.RemoteException {
		GetUnitIds getUnitIds = new GetUnitIds();
		return getUnitIds.getUnits(wsm1);
	}

}
