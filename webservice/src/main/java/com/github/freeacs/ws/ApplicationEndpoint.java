package com.github.freeacs.ws;


import com.github.freeacs.ws.xml.*;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import javax.validation.Valid;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;


@Endpoint
public class ApplicationEndpoint {
    private static final String NAMESPACE_URI = "http://xml.ws.freeacs.github.com/";

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "GetUnittypesRequest")
    @ResponsePayload
    public GetUnittypesResponse getCountry(@RequestPayload GetUnittypesRequest request) {
        ObjectFactory factory = new ObjectFactory();
        UnittypeList unittypeList = factory.createUnittypeList();
        ArrayOfUnittype arrayOfUnittype = factory.createArrayOfUnittype();
        Unittype unittype = factory.createUnittype();
        unittype.setDescription(factory.createUnittypeDescription("Test"));
        unittype.setProtocol(factory.createUnittypeProtocol("Protocol"));
        unittype.setName("tEST");
        arrayOfUnittype.getItem().add(unittype);
        unittypeList.setUnittypeArray(arrayOfUnittype);
        JAXBElement<UnittypeList> unittypeListElem = factory.createGetUnittypesResponseUnittypes(unittypeList);
        GetUnittypesResponse response = new GetUnittypesResponse();
        response.setUnittypes(unittypeListElem);
        return response;
    }
}