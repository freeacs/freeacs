/**
 * XAPSWS_PortType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.owera.xapsws;

public interface XAPSWS_PortType extends java.rmi.Remote {
    public com.owera.xapsws.GetUnittypesResponse getUnittypes(com.owera.xapsws.GetUnittypesRequest wsm1) throws java.rmi.RemoteException;
    public com.owera.xapsws.AddOrChangeUnittypeResponse addOrChangeUnittype(com.owera.xapsws.AddOrChangeUnittypeRequest wsm1) throws java.rmi.RemoteException;
    public com.owera.xapsws.DeleteUnittypeResponse deleteUnittype(com.owera.xapsws.DeleteUnittypeRequest wsm3) throws java.rmi.RemoteException;
    public com.owera.xapsws.GetProfilesResponse getProfiles(com.owera.xapsws.GetProfilesRequest wsm1) throws java.rmi.RemoteException;
    public com.owera.xapsws.AddOrChangeProfileResponse addOrChangeProfile(com.owera.xapsws.AddOrChangeProfileRequest wsm1) throws java.rmi.RemoteException;
    public com.owera.xapsws.DeleteProfileResponse deleteProfile(com.owera.xapsws.DeleteProfileRequest wsm3) throws java.rmi.RemoteException;
    public com.owera.xapsws.GetUnitsResponse getUnits(com.owera.xapsws.GetUnitsRequest wsm1) throws java.rmi.RemoteException;
    public com.owera.xapsws.GetUnitIdsResponse getUnitIds(com.owera.xapsws.GetUnitIdsRequest wsm1) throws java.rmi.RemoteException;
    public com.owera.xapsws.AddOrChangeUnitResponse addOrChangeUnit(com.owera.xapsws.AddOrChangeUnitRequest wsm1) throws java.rmi.RemoteException;
    public com.owera.xapsws.DeleteUnitResponse deleteUnit(com.owera.xapsws.DeleteUnitRequest wsm3) throws java.rmi.RemoteException;
}
