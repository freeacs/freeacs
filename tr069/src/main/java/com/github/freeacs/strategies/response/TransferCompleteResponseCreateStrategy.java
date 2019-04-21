package com.github.freeacs.strategies.response;

import com.github.freeacs.http.HTTPRequestResponseData;
import com.github.freeacs.tr069.methods.TCres;
import com.github.freeacs.tr069.xml.Body;
import com.github.freeacs.tr069.xml.Header;
import com.github.freeacs.tr069.xml.Response;
import com.github.freeacs.tr069.xml.TR069TransactionID;

public class TransferCompleteResponseCreateStrategy implements ResponseCreateStrategy {
    @SuppressWarnings("Duplicates")
    @Override
    public Response getResponse(HTTPRequestResponseData reqRes) {
        TR069TransactionID tr069ID = reqRes.getTR069TransactionID();
        Header header = new Header(tr069ID, null, null);
        Body body = new TCres();
        return new Response(header, body, reqRes.getSessionData().getCwmpVersionNumber());
    }
}
