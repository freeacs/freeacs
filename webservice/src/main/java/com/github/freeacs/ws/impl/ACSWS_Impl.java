package com.github.freeacs.ws.impl;

import com.github.freeacs.ws.xml.AddOrChangeProfileRequest;
import com.github.freeacs.ws.xml.AddOrChangeProfileResponse;
import com.github.freeacs.ws.xml.AddOrChangeUnitRequest;
import com.github.freeacs.ws.xml.AddOrChangeUnitResponse;
import com.github.freeacs.ws.xml.AddOrChangeUnittypeRequest;
import com.github.freeacs.ws.xml.AddOrChangeUnittypeResponse;
import com.github.freeacs.ws.xml.DeleteProfileRequest;
import com.github.freeacs.ws.xml.DeleteProfileResponse;
import com.github.freeacs.ws.xml.DeleteUnitRequest;
import com.github.freeacs.ws.xml.DeleteUnitResponse;
import com.github.freeacs.ws.xml.DeleteUnittypeRequest;
import com.github.freeacs.ws.xml.DeleteUnittypeResponse;
import com.github.freeacs.ws.xml.GetProfilesRequest;
import com.github.freeacs.ws.xml.GetProfilesResponse;
import com.github.freeacs.ws.xml.GetUnitIdsRequest;
import com.github.freeacs.ws.xml.GetUnitIdsResponse;
import com.github.freeacs.ws.xml.GetUnitsRequest;
import com.github.freeacs.ws.xml.GetUnitsResponse;
import com.github.freeacs.ws.xml.GetUnittypesRequest;
import com.github.freeacs.ws.xml.GetUnittypesResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * This class i simply a placeholde for code which is overwritten each time this class is re-
 * generated on the basis of a change in the wsdl-file. Then we copy the class content fromt
 * this class into com.owera.xapsws.XAPSWS_BindingImplCopy
 * @author Morten
 *
 */
@Component
public class ACSWS_Impl {

	private final DataSource xapsDs, syslogDs;

	@Autowired
	public ACSWS_Impl(@Qualifier("main") DataSource xapsDs, @Qualifier("syslog") DataSource syslogDs) {
		this.xapsDs = xapsDs;
		this.syslogDs = syslogDs;
	}

	public GetUnittypesResponse getUnittypes(GetUnittypesRequest gur) throws java.rmi.RemoteException {
		GetUnittypes getUnittypes = new GetUnittypes();
		return getUnittypes.getUnittypes(gur, xapsDs, syslogDs);
	}

	public AddOrChangeUnittypeResponse addOrChangeUnittype(AddOrChangeUnittypeRequest wsm1) throws java.rmi.RemoteException {
		AddOrChangeUnittype xapsws = new AddOrChangeUnittype();
		return xapsws.addOrChangeUnittype(wsm1, xapsDs, syslogDs);
	}

	public DeleteUnittypeResponse deleteUnittype(DeleteUnittypeRequest wsm3) throws java.rmi.RemoteException {
		DeleteUnittype xapsws = new DeleteUnittype();
		return xapsws.deleteUnittype(wsm3, xapsDs, syslogDs);
	}

	public GetProfilesResponse getProfiles(GetProfilesRequest gur) throws java.rmi.RemoteException {
		GetProfiles getProfiles = new GetProfiles();
		return getProfiles.getProfiles(gur, xapsDs, syslogDs);
	}

	public AddOrChangeProfileResponse addOrChangeProfile(AddOrChangeProfileRequest wsm1) throws java.rmi.RemoteException {
		AddOrChangeProfile xapsws = new AddOrChangeProfile();
		return xapsws.addOrChangeProfile(wsm1, xapsDs, syslogDs);
	}

	public DeleteProfileResponse deleteProfile(DeleteProfileRequest wsm3) throws java.rmi.RemoteException {
		DeleteProfile xapsws = new DeleteProfile();
		return xapsws.deleteProfile(wsm3, xapsDs, syslogDs);
	}

	public GetUnitsResponse getUnits(GetUnitsRequest wsm1) throws java.rmi.RemoteException {
		GetUnits getUnits = new GetUnits();
		return getUnits.getUnits(wsm1, xapsDs, syslogDs);
	}

	public AddOrChangeUnitResponse addOrChangeUnit(AddOrChangeUnitRequest wsm1) throws java.rmi.RemoteException {
		AddOrChangeUnit xapsws = new AddOrChangeUnit();
		return xapsws.addOrChangeUnit(wsm1, xapsDs, syslogDs);
	}

	public DeleteUnitResponse deleteUnit(DeleteUnitRequest wsm3) throws java.rmi.RemoteException {
		DeleteUnit xapsws = new DeleteUnit();
		return xapsws.deleteUnit(wsm3, xapsDs, syslogDs);
	}

	public GetUnitIdsResponse getUnitIds(GetUnitIdsRequest wsm1) throws java.rmi.RemoteException {
		GetUnitIds getUnitIds = new GetUnitIds();
		return getUnitIds.getUnits(wsm1, xapsDs, syslogDs);
	}
}
