package com.github.freeacs.tr069.methods.request;

import com.github.freeacs.http.HTTPRequestResponseData;
import com.github.freeacs.tr069.methods.ProvisioningMethod;
import com.github.freeacs.tr069.xml.Parser;

public class FactoryResetRequestProcessStrategy implements RequestProcessStrategy {
    @SuppressWarnings("Duplicates")
    @Override
    public void process(HTTPRequestResponseData reqRes) throws Exception {
        reqRes.getRequestData().setMethod(ProvisioningMethod.FactoryReset.name());
        Parser parser = new Parser(reqRes.getRequestData().getXml());
        if (parser.getHeader().getNoMoreRequests() != null
                && parser.getHeader().getNoMoreRequests().getNoMoreRequestFlag()) {
            reqRes.getSessionData().setNoMoreRequests(true);
        }
    }
}
