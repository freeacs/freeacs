package com.github.freeacs.ws;


import com.github.freeacs.ws.impl.ACSWS_Impl;
import com.github.freeacs.ws.xml.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import java.rmi.RemoteException;

import static com.github.freeacs.ws.WebServiceConfig.NAMESPACE_URI;

@Endpoint
public class ACSEndpoiint {

    @Autowired
    private ACSWS_Impl acsws;

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "GetUnittypesRequest")
    @ResponsePayload
    public GetUnittypesResponse getUnittypes(@RequestPayload GetUnittypesRequest request) throws RemoteException {
        return acsws.getUnittypes(request);
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "GetProfilesRequest")
    @ResponsePayload
    public GetProfilesResponse getProfiles(@RequestPayload GetProfilesRequest request) throws RemoteException {
        return acsws.getProfiles(request);
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "GetUnitsRequest")
    @ResponsePayload
    public GetUnitsResponse getUnits(@RequestPayload GetUnitsRequest request) throws RemoteException {
        return acsws.getUnits(request);
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "GetUnitIdsRequest")
    @ResponsePayload
    public GetUnitIdsResponse getUnitIds(@RequestPayload GetUnitIdsRequest request) throws RemoteException {
        return acsws.getUnitIds(request);
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "AddOrChangeUnittypeRequest")
    @ResponsePayload
    public AddOrChangeUnittypeResponse addOrChangeUnittype(@RequestPayload AddOrChangeUnittypeRequest request) throws RemoteException {
        return acsws.addOrChangeUnittype(request);
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "AddOrChangeProfileRequest")
    @ResponsePayload
    public AddOrChangeProfileResponse addOrChangeProfile(@RequestPayload AddOrChangeProfileRequest request) throws RemoteException {
        return acsws.addOrChangeProfile(request);
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "AddOrChangeUnitRequest")
    @ResponsePayload
    public AddOrChangeUnitResponse addOrChangeUnit(@RequestPayload AddOrChangeUnitRequest request) throws RemoteException {
        return acsws.addOrChangeUnit(request);
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "DeleteUnitRequest")
    @ResponsePayload
    public DeleteUnitResponse deleteUnit(@RequestPayload DeleteUnitRequest request) throws RemoteException {
        return acsws.deleteUnit(request);
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "DeleteProfileRequest")
    @ResponsePayload
    public DeleteProfileResponse deleteProfile(@RequestPayload DeleteProfileRequest request) throws RemoteException {
        return acsws.deleteProfile(request);
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "DeleteUnittypeRequest")
    @ResponsePayload
    public DeleteUnittypeResponse deleteUnit(@RequestPayload DeleteUnittypeRequest request) throws RemoteException {
        return acsws.deleteUnittype(request);
    }
}