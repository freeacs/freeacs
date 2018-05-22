package com.github.freeacs.ws;


import com.github.freeacs.ws.xml.GetUnittypesRequest;
import com.github.freeacs.ws.xml.GetUnittypesResponse;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;


@Endpoint
public class ApplicationEndpoint {
    private static final String NAMESPACE_URI = "http://xml.ws.freeacs.github.com/";

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getUnittypesRequest")
    @ResponsePayload
    public GetUnittypesResponse getCountry(@RequestPayload GetUnittypesRequest request) {
        return new GetUnittypesResponse();
    }
}