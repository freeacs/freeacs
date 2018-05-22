//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.github.freeacs.ws;

import javax.xml.rpc.ServiceException;
import javax.xml.rpc.Stub;
import java.rmi.RemoteException;

public class XAPSWSProxy implements ACSWS_PortType {
    private String _endpoint = null;
    private ACSWS_PortType xAPSWS_PortType = null;

    public XAPSWSProxy() {
        this._initXAPSWSProxy();
    }

    public XAPSWSProxy(String endpoint) {
        this._endpoint = endpoint;
        this._initXAPSWSProxy();
    }

    private void _initXAPSWSProxy() {
        try {
            this.xAPSWS_PortType = (new ACSWS_ServiceLocator()).getACSWS();
            if (this.xAPSWS_PortType != null) {
                if (this._endpoint != null) {
                    ((Stub)this.xAPSWS_PortType)._setProperty("javax.xml.rpc.service.endpoint.address", this._endpoint);
                } else {
                    this._endpoint = (String)((Stub)this.xAPSWS_PortType)._getProperty("javax.xml.rpc.service.endpoint.address");
                }
            }
        } catch (ServiceException var2) {
            ;
        }

    }

    public String getEndpoint() {
        return this._endpoint;
    }

    public void setEndpoint(String endpoint) {
        this._endpoint = endpoint;
        if (this.xAPSWS_PortType != null) {
            ((Stub)this.xAPSWS_PortType)._setProperty("javax.xml.rpc.service.endpoint.address", this._endpoint);
        }

    }

    public ACSWS_PortType getXAPSWS_PortType() {
        if (this.xAPSWS_PortType == null) {
            this._initXAPSWSProxy();
        }

        return this.xAPSWS_PortType;
    }

    @Override
    public GetUnittypesResponse getUnittypes(GetUnittypesRequest wsm1) throws RemoteException {
        return null;
    }

    @Override
    public AddOrChangeUnittypeResponse addOrChangeUnittype(AddOrChangeUnittypeRequest wsm1) throws RemoteException {
        return null;
    }

    @Override
    public DeleteUnittypeResponse deleteUnittype(DeleteUnittypeRequest wsm3) throws RemoteException {
        return null;
    }

    @Override
    public GetProfilesResponse getProfiles(GetProfilesRequest wsm1) throws RemoteException {
        return null;
    }

    @Override
    public AddOrChangeProfileResponse addOrChangeProfile(AddOrChangeProfileRequest wsm1) throws RemoteException {
        return null;
    }

    @Override
    public DeleteProfileResponse deleteProfile(DeleteProfileRequest wsm3) throws RemoteException {
        return null;
    }

    public GetUnitsResponse getUnits(GetUnitsRequest getUnitsRequest) throws RemoteException {
        if (this.xAPSWS_PortType == null) {
            this._initXAPSWSProxy();
        }

        return this.xAPSWS_PortType.getUnits(getUnitsRequest);
    }

    @Override
    public GetUnitIdsResponse getUnitIds(GetUnitIdsRequest wsm1) throws RemoteException {
        return null;
    }

    public AddOrChangeUnitResponse addOrChangeUnit(AddOrChangeUnitRequest addOrChangeUnitRequest) throws RemoteException {
        if (this.xAPSWS_PortType == null) {
            this._initXAPSWSProxy();
        }

        return this.xAPSWS_PortType.addOrChangeUnit(addOrChangeUnitRequest);
    }

    public DeleteUnitResponse deleteUnit(DeleteUnitRequest deleteUnitRequest) throws RemoteException {
        if (this.xAPSWS_PortType == null) {
            this._initXAPSWSProxy();
        }

        return this.xAPSWS_PortType.deleteUnit(deleteUnitRequest);
    }
}
