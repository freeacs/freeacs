package com.github.freeacs.ws;


import com.github.freeacs.ws.impl.ACSWS_Impl;
import com.github.freeacs.ws.xml.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import javax.validation.Valid;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import java.rmi.RemoteException;


@Endpoint
public class ACSEndpoiint {
    private static final String NAMESPACE_URI = "http://xml.ws.freeacs.github.com/";

    @Autowired
    private ACSWS_Impl acsws;

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "GetUnittypesRequest")
    @ResponsePayload
    public GetUnittypesResponse getCountry(@RequestPayload GetUnittypesRequest request) throws RemoteException {
        return acsws.getUnittypes(request);
    }
}