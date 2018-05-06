/**
 * XAPSWS_BindingSkeleton.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.owera.xapsws;

public class XAPSWS_BindingSkeleton implements com.owera.xapsws.XAPSWS_PortType, org.apache.axis.wsdl.Skeleton {
    private com.owera.xapsws.XAPSWS_PortType impl;
    private static java.util.Map _myOperations = new java.util.Hashtable();
    private static java.util.Collection _myOperationsList = new java.util.ArrayList();

    /**
    * Returns List of OperationDesc objects with this name
    */
    public static java.util.List getOperationDescByName(java.lang.String methodName) {
        return (java.util.List)_myOperations.get(methodName);
    }

    /**
    * Returns Collection of OperationDescs
    */
    public static java.util.Collection getOperationDescs() {
        return _myOperationsList;
    }

    static {
        org.apache.axis.description.OperationDesc _oper;
        org.apache.axis.description.FaultDesc _fault;
        org.apache.axis.description.ParameterDesc [] _params;
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://xapsws.owera.com/", "GetUnittypesRequest"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://xapsws.owera.com/", ">GetUnittypesRequest"), com.owera.xapsws.GetUnittypesRequest.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("getUnittypes", _params, new javax.xml.namespace.QName("http://xapsws.owera.com/", "GetUnittypesResponse"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://xapsws.owera.com/", ">GetUnittypesResponse"));
        _oper.setElementQName(new javax.xml.namespace.QName("", "getUnittypes"));
        _oper.setSoapAction("http://http://xapsws.owera.com/xapsws/soap");
        _myOperationsList.add(_oper);
        if (_myOperations.get("getUnittypes") == null) {
            _myOperations.put("getUnittypes", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("getUnittypes")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://xapsws.owera.com/", "AddOrChangeUnittypeRequest"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://xapsws.owera.com/", ">AddOrChangeUnittypeRequest"), com.owera.xapsws.AddOrChangeUnittypeRequest.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("addOrChangeUnittype", _params, new javax.xml.namespace.QName("http://xapsws.owera.com/", "AddOrChangeUnittypeResponse"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://xapsws.owera.com/", ">AddOrChangeUnittypeResponse"));
        _oper.setElementQName(new javax.xml.namespace.QName("", "addOrChangeUnittype"));
        _oper.setSoapAction("http://http://xapsws.owera.com/xapsws/soap");
        _myOperationsList.add(_oper);
        if (_myOperations.get("addOrChangeUnittype") == null) {
            _myOperations.put("addOrChangeUnittype", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("addOrChangeUnittype")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://xapsws.owera.com/", "DeleteUnittypeRequest"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://xapsws.owera.com/", ">DeleteUnittypeRequest"), com.owera.xapsws.DeleteUnittypeRequest.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("deleteUnittype", _params, new javax.xml.namespace.QName("http://xapsws.owera.com/", "DeleteUnittypeResponse"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://xapsws.owera.com/", ">DeleteUnittypeResponse"));
        _oper.setElementQName(new javax.xml.namespace.QName("", "deleteUnittype"));
        _oper.setSoapAction("http://http://xapsws.owera.com/xapsws/soap");
        _myOperationsList.add(_oper);
        if (_myOperations.get("deleteUnittype") == null) {
            _myOperations.put("deleteUnittype", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("deleteUnittype")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://xapsws.owera.com/", "GetProfilesRequest"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://xapsws.owera.com/", ">GetProfilesRequest"), com.owera.xapsws.GetProfilesRequest.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("getProfiles", _params, new javax.xml.namespace.QName("http://xapsws.owera.com/", "GetProfilesResponse"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://xapsws.owera.com/", ">GetProfilesResponse"));
        _oper.setElementQName(new javax.xml.namespace.QName("", "getProfiles"));
        _oper.setSoapAction("http://http://xapsws.owera.com/xapsws/soap");
        _myOperationsList.add(_oper);
        if (_myOperations.get("getProfiles") == null) {
            _myOperations.put("getProfiles", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("getProfiles")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://xapsws.owera.com/", "AddOrChangeProfileRequest"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://xapsws.owera.com/", ">AddOrChangeProfileRequest"), com.owera.xapsws.AddOrChangeProfileRequest.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("addOrChangeProfile", _params, new javax.xml.namespace.QName("http://xapsws.owera.com/", "AddOrChangeProfileResponse"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://xapsws.owera.com/", ">AddOrChangeProfileResponse"));
        _oper.setElementQName(new javax.xml.namespace.QName("", "addOrChangeProfile"));
        _oper.setSoapAction("http://http://xapsws.owera.com/xapsws/soap");
        _myOperationsList.add(_oper);
        if (_myOperations.get("addOrChangeProfile") == null) {
            _myOperations.put("addOrChangeProfile", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("addOrChangeProfile")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://xapsws.owera.com/", "DeleteProfileRequest"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://xapsws.owera.com/", ">DeleteProfileRequest"), com.owera.xapsws.DeleteProfileRequest.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("deleteProfile", _params, new javax.xml.namespace.QName("http://xapsws.owera.com/", "DeleteProfileResponse"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://xapsws.owera.com/", ">DeleteProfileResponse"));
        _oper.setElementQName(new javax.xml.namespace.QName("", "deleteProfile"));
        _oper.setSoapAction("http://http://xapsws.owera.com/xapsws/soap");
        _myOperationsList.add(_oper);
        if (_myOperations.get("deleteProfile") == null) {
            _myOperations.put("deleteProfile", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("deleteProfile")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://xapsws.owera.com/", "GetUnitIdsRequest"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://xapsws.owera.com/", ">GetUnitIdsRequest"), com.owera.xapsws.GetUnitIdsRequest.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("getUnitIds", _params, new javax.xml.namespace.QName("http://xapsws.owera.com/", "GetUnitIdsResponse"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://xapsws.owera.com/", ">GetUnitIdsResponse"));
        _oper.setElementQName(new javax.xml.namespace.QName("", "getUnitIds"));
        _oper.setSoapAction("http://http://xapsws.owera.com/xapsws/soap");
        _myOperationsList.add(_oper);
        if (_myOperations.get("getUnitIds") == null) {
            _myOperations.put("getUnitIds", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("getUnitIds")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://xapsws.owera.com/", "GetUnitsRequest"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://xapsws.owera.com/", ">GetUnitsRequest"), com.owera.xapsws.GetUnitsRequest.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("getUnits", _params, new javax.xml.namespace.QName("http://xapsws.owera.com/", "GetUnitsResponse"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://xapsws.owera.com/", ">GetUnitsResponse"));
        _oper.setElementQName(new javax.xml.namespace.QName("", "getUnits"));
        _oper.setSoapAction("http://http://xapsws.owera.com/xapsws/soap");
        _myOperationsList.add(_oper);
        if (_myOperations.get("getUnits") == null) {
            _myOperations.put("getUnits", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("getUnits")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://xapsws.owera.com/", "AddOrChangeUnitRequest"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://xapsws.owera.com/", ">AddOrChangeUnitRequest"), com.owera.xapsws.AddOrChangeUnitRequest.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("addOrChangeUnit", _params, new javax.xml.namespace.QName("http://xapsws.owera.com/", "AddOrChangeUnitResponse"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://xapsws.owera.com/", ">AddOrChangeUnitResponse"));
        _oper.setElementQName(new javax.xml.namespace.QName("", "addOrChangeUnit"));
        _oper.setSoapAction("http://http://xapsws.owera.com/xapsws/soap");
        _myOperationsList.add(_oper);
        if (_myOperations.get("addOrChangeUnit") == null) {
            _myOperations.put("addOrChangeUnit", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("addOrChangeUnit")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://xapsws.owera.com/", "DeleteUnitRequest"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://xapsws.owera.com/", ">DeleteUnitRequest"), com.owera.xapsws.DeleteUnitRequest.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("deleteUnit", _params, new javax.xml.namespace.QName("http://xapsws.owera.com/", "DeleteUnitResponse"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://xapsws.owera.com/", ">DeleteUnitResponse"));
        _oper.setElementQName(new javax.xml.namespace.QName("", "deleteUnit"));
        _oper.setSoapAction("http://http://xapsws.owera.com/xapsws/soap");
        _myOperationsList.add(_oper);
        if (_myOperations.get("deleteUnit") == null) {
            _myOperations.put("deleteUnit", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("deleteUnit")).add(_oper);
    }

    public XAPSWS_BindingSkeleton() {
        this.impl = new com.owera.xapsws.XAPSWS_BindingImpl();
    }

    public XAPSWS_BindingSkeleton(com.owera.xapsws.XAPSWS_PortType impl) {
        this.impl = impl;
    }
    public com.owera.xapsws.GetUnittypesResponse getUnittypes(com.owera.xapsws.GetUnittypesRequest wsm1) throws java.rmi.RemoteException
    {
        com.owera.xapsws.GetUnittypesResponse ret = impl.getUnittypes(wsm1);
        return ret;
    }

    public com.owera.xapsws.AddOrChangeUnittypeResponse addOrChangeUnittype(com.owera.xapsws.AddOrChangeUnittypeRequest wsm1) throws java.rmi.RemoteException
    {
        com.owera.xapsws.AddOrChangeUnittypeResponse ret = impl.addOrChangeUnittype(wsm1);
        return ret;
    }

    public com.owera.xapsws.DeleteUnittypeResponse deleteUnittype(com.owera.xapsws.DeleteUnittypeRequest wsm3) throws java.rmi.RemoteException
    {
        com.owera.xapsws.DeleteUnittypeResponse ret = impl.deleteUnittype(wsm3);
        return ret;
    }

    public com.owera.xapsws.GetProfilesResponse getProfiles(com.owera.xapsws.GetProfilesRequest wsm1) throws java.rmi.RemoteException
    {
        com.owera.xapsws.GetProfilesResponse ret = impl.getProfiles(wsm1);
        return ret;
    }

    public com.owera.xapsws.AddOrChangeProfileResponse addOrChangeProfile(com.owera.xapsws.AddOrChangeProfileRequest wsm1) throws java.rmi.RemoteException
    {
        com.owera.xapsws.AddOrChangeProfileResponse ret = impl.addOrChangeProfile(wsm1);
        return ret;
    }

    public com.owera.xapsws.DeleteProfileResponse deleteProfile(com.owera.xapsws.DeleteProfileRequest wsm3) throws java.rmi.RemoteException
    {
        com.owera.xapsws.DeleteProfileResponse ret = impl.deleteProfile(wsm3);
        return ret;
    }

    public com.owera.xapsws.GetUnitIdsResponse getUnitIds(com.owera.xapsws.GetUnitIdsRequest wsm1) throws java.rmi.RemoteException
    {
        com.owera.xapsws.GetUnitIdsResponse ret = impl.getUnitIds(wsm1);
        return ret;
    }

    public com.owera.xapsws.GetUnitsResponse getUnits(com.owera.xapsws.GetUnitsRequest wsm1) throws java.rmi.RemoteException
    {
        com.owera.xapsws.GetUnitsResponse ret = impl.getUnits(wsm1);
        return ret;
    }

    public com.owera.xapsws.AddOrChangeUnitResponse addOrChangeUnit(com.owera.xapsws.AddOrChangeUnitRequest wsm1) throws java.rmi.RemoteException
    {
        com.owera.xapsws.AddOrChangeUnitResponse ret = impl.addOrChangeUnit(wsm1);
        return ret;
    }

    public com.owera.xapsws.DeleteUnitResponse deleteUnit(com.owera.xapsws.DeleteUnitRequest wsm3) throws java.rmi.RemoteException
    {
        com.owera.xapsws.DeleteUnitResponse ret = impl.deleteUnit(wsm3);
        return ret;
    }

}
