package com.github.freeacs.strategies.request;

import com.github.freeacs.base.Log;
import com.github.freeacs.http.HTTPRequestResponseData;
import com.github.freeacs.tr069.methods.Method;
import com.github.freeacs.tr069.methods.TCreq;
import com.github.freeacs.tr069.xml.Header;
import com.github.freeacs.tr069.xml.Parser;

public class TransferCompleteRequestProcessStrategy implements RequestProcessStrategy {
    @SuppressWarnings("Duplicates")
    @Override
    public void process(HTTPRequestResponseData reqRes) throws Exception {
        reqRes.getRequestData().setMethod(Method.TransferComplete.name());
        Parser parser = new Parser(reqRes.getRequestData().getXml());
        Header header = parser.getHeader();
        reqRes.setTR069TransactionID(header.getId());
        if (parser.getFault() != null && !"0".equals(parser.getFault().getFaultCode())) {
            reqRes.getRequestData().setFault(parser.getFault());
            Log.debug(TCreq.class, "TCReq reported a fault");
        } else {
            Log.debug(TCreq.class, "TCReq is ok (download is assumed ok)");
        }
    }
}
