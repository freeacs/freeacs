/**
 * XAPSWS_BindingStub.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.owera.xapsws;

public class XAPSWS_BindingStub extends org.apache.axis.client.Stub implements com.owera.xapsws.XAPSWS_PortType {
    private java.util.Vector cachedSerClasses = new java.util.Vector();
    private java.util.Vector cachedSerQNames = new java.util.Vector();
    private java.util.Vector cachedSerFactories = new java.util.Vector();
    private java.util.Vector cachedDeserFactories = new java.util.Vector();

    static org.apache.axis.description.OperationDesc [] _operations;

    static {
        _operations = new org.apache.axis.description.OperationDesc[10];
        _initOperationDesc1();
    }

    private static void _initOperationDesc1(){
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getUnittypes");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://xapsws.owera.com/", "GetUnittypesRequest"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://xapsws.owera.com/", ">GetUnittypesRequest"), com.owera.xapsws.GetUnittypesRequest.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://xapsws.owera.com/", ">GetUnittypesResponse"));
        oper.setReturnClass(com.owera.xapsws.GetUnittypesResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://xapsws.owera.com/", "GetUnittypesResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[0] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("addOrChangeUnittype");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://xapsws.owera.com/", "AddOrChangeUnittypeRequest"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://xapsws.owera.com/", ">AddOrChangeUnittypeRequest"), com.owera.xapsws.AddOrChangeUnittypeRequest.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://xapsws.owera.com/", ">AddOrChangeUnittypeResponse"));
        oper.setReturnClass(com.owera.xapsws.AddOrChangeUnittypeResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://xapsws.owera.com/", "AddOrChangeUnittypeResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[1] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("deleteUnittype");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://xapsws.owera.com/", "DeleteUnittypeRequest"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://xapsws.owera.com/", ">DeleteUnittypeRequest"), com.owera.xapsws.DeleteUnittypeRequest.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://xapsws.owera.com/", ">DeleteUnittypeResponse"));
        oper.setReturnClass(com.owera.xapsws.DeleteUnittypeResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://xapsws.owera.com/", "DeleteUnittypeResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[2] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getProfiles");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://xapsws.owera.com/", "GetProfilesRequest"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://xapsws.owera.com/", ">GetProfilesRequest"), com.owera.xapsws.GetProfilesRequest.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://xapsws.owera.com/", ">GetProfilesResponse"));
        oper.setReturnClass(com.owera.xapsws.GetProfilesResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://xapsws.owera.com/", "GetProfilesResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[3] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("addOrChangeProfile");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://xapsws.owera.com/", "AddOrChangeProfileRequest"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://xapsws.owera.com/", ">AddOrChangeProfileRequest"), com.owera.xapsws.AddOrChangeProfileRequest.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://xapsws.owera.com/", ">AddOrChangeProfileResponse"));
        oper.setReturnClass(com.owera.xapsws.AddOrChangeProfileResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://xapsws.owera.com/", "AddOrChangeProfileResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[4] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("deleteProfile");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://xapsws.owera.com/", "DeleteProfileRequest"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://xapsws.owera.com/", ">DeleteProfileRequest"), com.owera.xapsws.DeleteProfileRequest.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://xapsws.owera.com/", ">DeleteProfileResponse"));
        oper.setReturnClass(com.owera.xapsws.DeleteProfileResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://xapsws.owera.com/", "DeleteProfileResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[5] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getUnitIds");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://xapsws.owera.com/", "GetUnitIdsRequest"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://xapsws.owera.com/", ">GetUnitIdsRequest"), com.owera.xapsws.GetUnitIdsRequest.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://xapsws.owera.com/", ">GetUnitIdsResponse"));
        oper.setReturnClass(com.owera.xapsws.GetUnitIdsResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://xapsws.owera.com/", "GetUnitIdsResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[6] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getUnits");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://xapsws.owera.com/", "GetUnitsRequest"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://xapsws.owera.com/", ">GetUnitsRequest"), com.owera.xapsws.GetUnitsRequest.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://xapsws.owera.com/", ">GetUnitsResponse"));
        oper.setReturnClass(com.owera.xapsws.GetUnitsResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://xapsws.owera.com/", "GetUnitsResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[7] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("addOrChangeUnit");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://xapsws.owera.com/", "AddOrChangeUnitRequest"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://xapsws.owera.com/", ">AddOrChangeUnitRequest"), com.owera.xapsws.AddOrChangeUnitRequest.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://xapsws.owera.com/", ">AddOrChangeUnitResponse"));
        oper.setReturnClass(com.owera.xapsws.AddOrChangeUnitResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://xapsws.owera.com/", "AddOrChangeUnitResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[8] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("deleteUnit");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://xapsws.owera.com/", "DeleteUnitRequest"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://xapsws.owera.com/", ">DeleteUnitRequest"), com.owera.xapsws.DeleteUnitRequest.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://xapsws.owera.com/", ">DeleteUnitResponse"));
        oper.setReturnClass(com.owera.xapsws.DeleteUnitResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://xapsws.owera.com/", "DeleteUnitResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[9] = oper;

    }

    public XAPSWS_BindingStub() throws org.apache.axis.AxisFault {
         this(null);
    }

    public XAPSWS_BindingStub(java.net.URL endpointURL, javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
         this(service);
         super.cachedEndpoint = endpointURL;
    }

    public XAPSWS_BindingStub(javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
        if (service == null) {
            super.service = new org.apache.axis.client.Service();
        } else {
            super.service = service;
        }
        ((org.apache.axis.client.Service)super.service).setTypeMappingVersion("1.2");
            java.lang.Class cls;
            javax.xml.namespace.QName qName;
            javax.xml.namespace.QName qName2;
            java.lang.Class beansf = org.apache.axis.encoding.ser.BeanSerializerFactory.class;
            java.lang.Class beandf = org.apache.axis.encoding.ser.BeanDeserializerFactory.class;
            java.lang.Class enumsf = org.apache.axis.encoding.ser.EnumSerializerFactory.class;
            java.lang.Class enumdf = org.apache.axis.encoding.ser.EnumDeserializerFactory.class;
            java.lang.Class arraysf = org.apache.axis.encoding.ser.ArraySerializerFactory.class;
            java.lang.Class arraydf = org.apache.axis.encoding.ser.ArrayDeserializerFactory.class;
            java.lang.Class simplesf = org.apache.axis.encoding.ser.SimpleSerializerFactory.class;
            java.lang.Class simpledf = org.apache.axis.encoding.ser.SimpleDeserializerFactory.class;
            java.lang.Class simplelistsf = org.apache.axis.encoding.ser.SimpleListSerializerFactory.class;
            java.lang.Class simplelistdf = org.apache.axis.encoding.ser.SimpleListDeserializerFactory.class;
            qName = new javax.xml.namespace.QName("http://xapsws.owera.com/", ">AddOrChangeProfileRequest");
            cachedSerQNames.add(qName);
            cls = com.owera.xapsws.AddOrChangeProfileRequest.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://xapsws.owera.com/", ">AddOrChangeProfileResponse");
            cachedSerQNames.add(qName);
            cls = com.owera.xapsws.AddOrChangeProfileResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://xapsws.owera.com/", ">AddOrChangeUnitRequest");
            cachedSerQNames.add(qName);
            cls = com.owera.xapsws.AddOrChangeUnitRequest.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://xapsws.owera.com/", ">AddOrChangeUnitResponse");
            cachedSerQNames.add(qName);
            cls = com.owera.xapsws.AddOrChangeUnitResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://xapsws.owera.com/", ">AddOrChangeUnittypeRequest");
            cachedSerQNames.add(qName);
            cls = com.owera.xapsws.AddOrChangeUnittypeRequest.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://xapsws.owera.com/", ">AddOrChangeUnittypeResponse");
            cachedSerQNames.add(qName);
            cls = com.owera.xapsws.AddOrChangeUnittypeResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://xapsws.owera.com/", ">DeleteProfileRequest");
            cachedSerQNames.add(qName);
            cls = com.owera.xapsws.DeleteProfileRequest.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://xapsws.owera.com/", ">DeleteProfileResponse");
            cachedSerQNames.add(qName);
            cls = com.owera.xapsws.DeleteProfileResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://xapsws.owera.com/", ">DeleteUnitRequest");
            cachedSerQNames.add(qName);
            cls = com.owera.xapsws.DeleteUnitRequest.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://xapsws.owera.com/", ">DeleteUnitResponse");
            cachedSerQNames.add(qName);
            cls = com.owera.xapsws.DeleteUnitResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://xapsws.owera.com/", ">DeleteUnittypeRequest");
            cachedSerQNames.add(qName);
            cls = com.owera.xapsws.DeleteUnittypeRequest.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://xapsws.owera.com/", ">DeleteUnittypeResponse");
            cachedSerQNames.add(qName);
            cls = com.owera.xapsws.DeleteUnittypeResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://xapsws.owera.com/", ">GetProfilesRequest");
            cachedSerQNames.add(qName);
            cls = com.owera.xapsws.GetProfilesRequest.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://xapsws.owera.com/", ">GetProfilesResponse");
            cachedSerQNames.add(qName);
            cls = com.owera.xapsws.GetProfilesResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://xapsws.owera.com/", ">GetUnitIdsRequest");
            cachedSerQNames.add(qName);
            cls = com.owera.xapsws.GetUnitIdsRequest.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://xapsws.owera.com/", ">GetUnitIdsResponse");
            cachedSerQNames.add(qName);
            cls = com.owera.xapsws.GetUnitIdsResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://xapsws.owera.com/", ">GetUnitsRequest");
            cachedSerQNames.add(qName);
            cls = com.owera.xapsws.GetUnitsRequest.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://xapsws.owera.com/", ">GetUnitsResponse");
            cachedSerQNames.add(qName);
            cls = com.owera.xapsws.GetUnitsResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://xapsws.owera.com/", ">GetUnittypesRequest");
            cachedSerQNames.add(qName);
            cls = com.owera.xapsws.GetUnittypesRequest.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://xapsws.owera.com/", ">GetUnittypesResponse");
            cachedSerQNames.add(qName);
            cls = com.owera.xapsws.GetUnittypesResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://xapsws.owera.com/", "ArrayOfParameter");
            cachedSerQNames.add(qName);
            cls = com.owera.xapsws.Parameter[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://xapsws.owera.com/", "Parameter");
            qName2 = new javax.xml.namespace.QName("", "item");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://xapsws.owera.com/", "ArrayOfProfile");
            cachedSerQNames.add(qName);
            cls = com.owera.xapsws.Profile[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://xapsws.owera.com/", "Profile");
            qName2 = new javax.xml.namespace.QName("", "item");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://xapsws.owera.com/", "ArrayOfUnit");
            cachedSerQNames.add(qName);
            cls = com.owera.xapsws.Unit[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://xapsws.owera.com/", "Unit");
            qName2 = new javax.xml.namespace.QName("", "item");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://xapsws.owera.com/", "ArrayOfUnitId");
            cachedSerQNames.add(qName);
            cls = java.lang.String[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string");
            qName2 = new javax.xml.namespace.QName("", "unitId");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://xapsws.owera.com/", "ArrayOfUnittype");
            cachedSerQNames.add(qName);
            cls = com.owera.xapsws.Unittype[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://xapsws.owera.com/", "Unittype");
            qName2 = new javax.xml.namespace.QName("", "item");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://xapsws.owera.com/", "Login");
            cachedSerQNames.add(qName);
            cls = com.owera.xapsws.Login.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://xapsws.owera.com/", "Parameter");
            cachedSerQNames.add(qName);
            cls = com.owera.xapsws.Parameter.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://xapsws.owera.com/", "parameterList");
            cachedSerQNames.add(qName);
            cls = com.owera.xapsws.ParameterList.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://xapsws.owera.com/", "Profile");
            cachedSerQNames.add(qName);
            cls = com.owera.xapsws.Profile.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://xapsws.owera.com/", "profileList");
            cachedSerQNames.add(qName);
            cls = com.owera.xapsws.ProfileList.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://xapsws.owera.com/", "Unit");
            cachedSerQNames.add(qName);
            cls = com.owera.xapsws.Unit.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://xapsws.owera.com/", "unitIdList");
            cachedSerQNames.add(qName);
            cls = com.owera.xapsws.UnitIdList.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://xapsws.owera.com/", "unitList");
            cachedSerQNames.add(qName);
            cls = com.owera.xapsws.UnitList.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://xapsws.owera.com/", "Unittype");
            cachedSerQNames.add(qName);
            cls = com.owera.xapsws.Unittype.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://xapsws.owera.com/", "unittypeList");
            cachedSerQNames.add(qName);
            cls = com.owera.xapsws.UnittypeList.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

    }

    protected org.apache.axis.client.Call createCall() throws java.rmi.RemoteException {
        try {
            org.apache.axis.client.Call _call = super._createCall();
            if (super.maintainSessionSet) {
                _call.setMaintainSession(super.maintainSession);
            }
            if (super.cachedUsername != null) {
                _call.setUsername(super.cachedUsername);
            }
            if (super.cachedPassword != null) {
                _call.setPassword(super.cachedPassword);
            }
            if (super.cachedEndpoint != null) {
                _call.setTargetEndpointAddress(super.cachedEndpoint);
            }
            if (super.cachedTimeout != null) {
                _call.setTimeout(super.cachedTimeout);
            }
            if (super.cachedPortName != null) {
                _call.setPortName(super.cachedPortName);
            }
            java.util.Enumeration keys = super.cachedProperties.keys();
            while (keys.hasMoreElements()) {
                java.lang.String key = (java.lang.String) keys.nextElement();
                _call.setProperty(key, super.cachedProperties.get(key));
            }
            // All the type mapping information is registered
            // when the first call is made.
            // The type mapping information is actually registered in
            // the TypeMappingRegistry of the service, which
            // is the reason why registration is only needed for the first call.
            synchronized (this) {
                if (firstCall()) {
                    // must set encoding style before registering serializers
                    _call.setEncodingStyle(null);
                    for (int i = 0; i < cachedSerFactories.size(); ++i) {
                        java.lang.Class cls = (java.lang.Class) cachedSerClasses.get(i);
                        javax.xml.namespace.QName qName =
                                (javax.xml.namespace.QName) cachedSerQNames.get(i);
                        java.lang.Object x = cachedSerFactories.get(i);
                        if (x instanceof Class) {
                            java.lang.Class sf = (java.lang.Class)
                                 cachedSerFactories.get(i);
                            java.lang.Class df = (java.lang.Class)
                                 cachedDeserFactories.get(i);
                            _call.registerTypeMapping(cls, qName, sf, df, false);
                        }
                        else if (x instanceof javax.xml.rpc.encoding.SerializerFactory) {
                            org.apache.axis.encoding.SerializerFactory sf = (org.apache.axis.encoding.SerializerFactory)
                                 cachedSerFactories.get(i);
                            org.apache.axis.encoding.DeserializerFactory df = (org.apache.axis.encoding.DeserializerFactory)
                                 cachedDeserFactories.get(i);
                            _call.registerTypeMapping(cls, qName, sf, df, false);
                        }
                    }
                }
            }
            return _call;
        }
        catch (java.lang.Throwable _t) {
            throw new org.apache.axis.AxisFault("Failure trying to get the Call object", _t);
        }
    }

    public com.owera.xapsws.GetUnittypesResponse getUnittypes(com.owera.xapsws.GetUnittypesRequest wsm1) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[0]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://http://xapsws.owera.com/xapsws/soap");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "getUnittypes"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {wsm1});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.owera.xapsws.GetUnittypesResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.owera.xapsws.GetUnittypesResponse) org.apache.axis.utils.JavaUtils.convert(_resp, com.owera.xapsws.GetUnittypesResponse.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public com.owera.xapsws.AddOrChangeUnittypeResponse addOrChangeUnittype(com.owera.xapsws.AddOrChangeUnittypeRequest wsm1) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[1]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://http://xapsws.owera.com/xapsws/soap");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "addOrChangeUnittype"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {wsm1});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.owera.xapsws.AddOrChangeUnittypeResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.owera.xapsws.AddOrChangeUnittypeResponse) org.apache.axis.utils.JavaUtils.convert(_resp, com.owera.xapsws.AddOrChangeUnittypeResponse.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public com.owera.xapsws.DeleteUnittypeResponse deleteUnittype(com.owera.xapsws.DeleteUnittypeRequest wsm3) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[2]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://http://xapsws.owera.com/xapsws/soap");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "deleteUnittype"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {wsm3});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.owera.xapsws.DeleteUnittypeResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.owera.xapsws.DeleteUnittypeResponse) org.apache.axis.utils.JavaUtils.convert(_resp, com.owera.xapsws.DeleteUnittypeResponse.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public com.owera.xapsws.GetProfilesResponse getProfiles(com.owera.xapsws.GetProfilesRequest wsm1) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[3]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://http://xapsws.owera.com/xapsws/soap");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "getProfiles"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {wsm1});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.owera.xapsws.GetProfilesResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.owera.xapsws.GetProfilesResponse) org.apache.axis.utils.JavaUtils.convert(_resp, com.owera.xapsws.GetProfilesResponse.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public com.owera.xapsws.AddOrChangeProfileResponse addOrChangeProfile(com.owera.xapsws.AddOrChangeProfileRequest wsm1) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[4]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://http://xapsws.owera.com/xapsws/soap");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "addOrChangeProfile"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {wsm1});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.owera.xapsws.AddOrChangeProfileResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.owera.xapsws.AddOrChangeProfileResponse) org.apache.axis.utils.JavaUtils.convert(_resp, com.owera.xapsws.AddOrChangeProfileResponse.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public com.owera.xapsws.DeleteProfileResponse deleteProfile(com.owera.xapsws.DeleteProfileRequest wsm3) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[5]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://http://xapsws.owera.com/xapsws/soap");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "deleteProfile"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {wsm3});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.owera.xapsws.DeleteProfileResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.owera.xapsws.DeleteProfileResponse) org.apache.axis.utils.JavaUtils.convert(_resp, com.owera.xapsws.DeleteProfileResponse.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public com.owera.xapsws.GetUnitIdsResponse getUnitIds(com.owera.xapsws.GetUnitIdsRequest wsm1) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[6]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://http://xapsws.owera.com/xapsws/soap");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "getUnitIds"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {wsm1});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.owera.xapsws.GetUnitIdsResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.owera.xapsws.GetUnitIdsResponse) org.apache.axis.utils.JavaUtils.convert(_resp, com.owera.xapsws.GetUnitIdsResponse.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public com.owera.xapsws.GetUnitsResponse getUnits(com.owera.xapsws.GetUnitsRequest wsm1) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[7]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://http://xapsws.owera.com/xapsws/soap");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "getUnits"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {wsm1});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.owera.xapsws.GetUnitsResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.owera.xapsws.GetUnitsResponse) org.apache.axis.utils.JavaUtils.convert(_resp, com.owera.xapsws.GetUnitsResponse.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public com.owera.xapsws.AddOrChangeUnitResponse addOrChangeUnit(com.owera.xapsws.AddOrChangeUnitRequest wsm1) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[8]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://http://xapsws.owera.com/xapsws/soap");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "addOrChangeUnit"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {wsm1});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.owera.xapsws.AddOrChangeUnitResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.owera.xapsws.AddOrChangeUnitResponse) org.apache.axis.utils.JavaUtils.convert(_resp, com.owera.xapsws.AddOrChangeUnitResponse.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public com.owera.xapsws.DeleteUnitResponse deleteUnit(com.owera.xapsws.DeleteUnitRequest wsm3) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[9]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://http://xapsws.owera.com/xapsws/soap");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "deleteUnit"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {wsm3});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.owera.xapsws.DeleteUnitResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.owera.xapsws.DeleteUnitResponse) org.apache.axis.utils.JavaUtils.convert(_resp, com.owera.xapsws.DeleteUnitResponse.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

}
