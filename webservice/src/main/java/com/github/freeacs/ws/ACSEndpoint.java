package com.github.freeacs.ws;

import static com.github.freeacs.ws.WebServiceConfig.NAMESPACE_URI;

import com.github.freeacs.ws.impl.ACSWS_Impl;
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
import com.github.freeacs.ws.xml.KickUnitRequest;
import com.github.freeacs.ws.xml.KickUnitResponse;
import java.rmi.RemoteException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public class ACSEndpoint {
  private final ACSWS_Impl acsws;

  @Autowired
  public ACSEndpoint(ACSWS_Impl acsws) {
    this.acsws = acsws;
  }

  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "GetUnittypesRequest")
  @ResponsePayload
  public GetUnittypesResponse getUnittypes(@RequestPayload GetUnittypesRequest request)
      throws RemoteException {
    return acsws.getUnittypes(request);
  }

  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "GetProfilesRequest")
  @ResponsePayload
  public GetProfilesResponse getProfiles(@RequestPayload GetProfilesRequest request)
      throws RemoteException {
    return acsws.getProfiles(request);
  }

  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "GetUnitsRequest")
  @ResponsePayload
  public GetUnitsResponse getUnits(@RequestPayload GetUnitsRequest request) throws RemoteException {
    return acsws.getUnits(request);
  }

  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "GetUnitIdsRequest")
  @ResponsePayload
  public GetUnitIdsResponse getUnitIds(@RequestPayload GetUnitIdsRequest request)
      throws RemoteException {
    return acsws.getUnitIds(request);
  }

  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "AddOrChangeUnittypeRequest")
  @ResponsePayload
  public AddOrChangeUnittypeResponse addOrChangeUnittype(
      @RequestPayload AddOrChangeUnittypeRequest request) throws RemoteException {
    return acsws.addOrChangeUnittype(request);
  }

  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "AddOrChangeProfileRequest")
  @ResponsePayload
  public AddOrChangeProfileResponse addOrChangeProfile(
      @RequestPayload AddOrChangeProfileRequest request) throws RemoteException {
    return acsws.addOrChangeProfile(request);
  }

  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "AddOrChangeUnitRequest")
  @ResponsePayload
  public AddOrChangeUnitResponse addOrChangeUnit(@RequestPayload AddOrChangeUnitRequest request)
      throws RemoteException {
    return acsws.addOrChangeUnit(request);
  }

  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "DeleteUnitRequest")
  @ResponsePayload
  public DeleteUnitResponse deleteUnit(@RequestPayload DeleteUnitRequest request)
      throws RemoteException {
    return acsws.deleteUnit(request);
  }

  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "DeleteProfileRequest")
  @ResponsePayload
  public DeleteProfileResponse deleteProfile(@RequestPayload DeleteProfileRequest request)
      throws RemoteException {
    return acsws.deleteProfile(request);
  }

  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "DeleteUnittypeRequest")
  @ResponsePayload
  public DeleteUnittypeResponse deleteUnit(@RequestPayload DeleteUnittypeRequest request)
      throws RemoteException {
    return acsws.deleteUnittype(request);
  }

  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "KickUnitRequest")
  @ResponsePayload
  public KickUnitResponse deleteUnit(@RequestPayload KickUnitRequest request)
      throws RemoteException {
    return acsws.kickUnit(request);
  }
}
