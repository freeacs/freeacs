package com.github.freeacs.tr069.methods.request;

import com.github.freeacs.tr069.http.HTTPRequestResponseData;
import com.github.freeacs.tr069.methods.ProvisioningMethod;
import com.github.freeacs.tr069.xml.Parser;

public class FaultRequestProcessStrategy implements RequestProcessStrategy {
    @Override
    public void process(HTTPRequestResponseData reqRes) throws Exception {
        reqRes.getRequestData().setMethod(ProvisioningMethod.Fault.name());
        Parser parser = new Parser(reqRes.getRequestData().getXml());
        reqRes.getRequestData().setFault(parser.getFault());
    }
}
