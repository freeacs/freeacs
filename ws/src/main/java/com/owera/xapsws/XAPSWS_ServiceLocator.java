/**
 * XAPSWS_ServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.owera.xapsws;

public class XAPSWS_ServiceLocator extends org.apache.axis.client.Service implements com.owera.xapsws.XAPSWS_Service {

    public XAPSWS_ServiceLocator() {
    }


    public XAPSWS_ServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public XAPSWS_ServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for xAPSWS
    private java.lang.String xAPSWS_address = "http://localhost:8082/xapsws/services/xAPSWS";

    public java.lang.String getxAPSWSAddress() {
        return xAPSWS_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String xAPSWSWSDDServiceName = "xAPSWS";

    public java.lang.String getxAPSWSWSDDServiceName() {
        return xAPSWSWSDDServiceName;
    }

    public void setxAPSWSWSDDServiceName(java.lang.String name) {
        xAPSWSWSDDServiceName = name;
    }

    public com.owera.xapsws.XAPSWS_PortType getxAPSWS() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(xAPSWS_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getxAPSWS(endpoint);
    }

    public com.owera.xapsws.XAPSWS_PortType getxAPSWS(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            com.owera.xapsws.XAPSWS_BindingStub _stub = new com.owera.xapsws.XAPSWS_BindingStub(portAddress, this);
            _stub.setPortName(getxAPSWSWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setxAPSWSEndpointAddress(java.lang.String address) {
        xAPSWS_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (com.owera.xapsws.XAPSWS_PortType.class.isAssignableFrom(serviceEndpointInterface)) {
                com.owera.xapsws.XAPSWS_BindingStub _stub = new com.owera.xapsws.XAPSWS_BindingStub(new java.net.URL(xAPSWS_address), this);
                _stub.setPortName(getxAPSWSWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("xAPSWS".equals(inputPortName)) {
            return getxAPSWS();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://xapsws.owera.com/", "xAPSWS");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://xapsws.owera.com/", "xAPSWS"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("xAPSWS".equals(portName)) {
            setxAPSWSEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
