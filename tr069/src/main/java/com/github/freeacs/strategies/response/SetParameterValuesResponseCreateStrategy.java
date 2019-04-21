package com.github.freeacs.strategies.response;

import com.github.freeacs.base.Log;
import com.github.freeacs.http.HTTPRequestResponseData;
import com.github.freeacs.tr069.ParameterKey;
import com.github.freeacs.tr069.Properties;
import com.github.freeacs.tr069.methods.SPVreq;
import com.github.freeacs.tr069.xml.*;

public class SetParameterValuesResponseCreateStrategy implements ResponseCreateStrategy {
    private final Properties properties;

    public SetParameterValuesResponseCreateStrategy(Properties properties) {
        this.properties = properties;
    }

    @SuppressWarnings("Duplicates")
    @Override
    public Response getResponse(HTTPRequestResponseData reqRes) throws Exception {
        if (reqRes.getTR069TransactionID() == null) {
            reqRes.setTR069TransactionID(new TR069TransactionID("FREEACS-" + System.currentTimeMillis()));
        }
        Header header = new Header(reqRes.getTR069TransactionID(), null, null);
        Body body;
        ParameterList paramList = reqRes.getSessionData().getToCPE();
        ParameterKey pk = new ParameterKey();
        if (!properties.isParameterkeyQuirk(reqRes.getSessionData())) {
            pk.setServerKey(reqRes);
        }
        body = new SPVreq(paramList.getParameterValueList(), pk.getServerKey());
        Log.notice(
                SetParameterValuesResponseCreateStrategy.class,
                "Sent to CPE: " + paramList.getParameterValueList().size() + " parameters.");
        reqRes
                .getSessionData()
                .getProvisioningMessage()
                .setParamsWritten(paramList.getParameterValueList().size());
        return new Response(header, body, reqRes.getSessionData().getCwmpVersionNumber());
    }
}
