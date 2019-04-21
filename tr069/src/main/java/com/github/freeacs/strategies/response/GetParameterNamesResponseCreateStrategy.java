package com.github.freeacs.strategies.response;

import com.github.freeacs.http.HTTPRequestResponseData;
import com.github.freeacs.tr069.Properties;
import com.github.freeacs.tr069.methods.GPNreq;
import com.github.freeacs.tr069.xml.Body;
import com.github.freeacs.tr069.xml.Header;
import com.github.freeacs.tr069.xml.Response;
import com.github.freeacs.tr069.xml.TR069TransactionID;

public class GetParameterNamesResponseCreateStrategy implements ResponseCreateStrategy {
    private final Properties properties;

    public GetParameterNamesResponseCreateStrategy(Properties properties) {
        this.properties = properties;
    }

    @SuppressWarnings("Duplicates")
    @Override
    public Response getResponse(HTTPRequestResponseData reqRes) {
        if (reqRes.getTR069TransactionID() == null) {
            reqRes.setTR069TransactionID(new TR069TransactionID("FREEACS-" + System.currentTimeMillis()));
        }
        Header header = new Header(reqRes.getTR069TransactionID(), null, null);
        String keyRoot = reqRes.getSessionData().getKeyRoot();
        Body body = new GPNreq(keyRoot, properties.isNextLevel0InGPN(reqRes.getSessionData()));
        return new Response(header, body, reqRes.getSessionData().getCwmpVersionNumber());
    }
}
