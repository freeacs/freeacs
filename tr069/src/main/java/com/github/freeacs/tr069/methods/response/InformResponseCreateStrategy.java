package com.github.freeacs.tr069.methods.response;

import com.github.freeacs.http.HTTPRequestResponseData;
import com.github.freeacs.tr069.xml.Body;
import com.github.freeacs.tr069.xml.Header;
import com.github.freeacs.tr069.xml.Response;
import com.github.freeacs.tr069.xml.TR069TransactionID;

public class InformResponseCreateStrategy implements ResponseCreateStrategy {
    @SuppressWarnings("Duplicates")
    @Override
    public Response getResponse(HTTPRequestResponseData reqRes) {
        TR069TransactionID tr069ID = reqRes.getTR069TransactionID();
        Header header = new Header(tr069ID, null, null);
        Body body = new Body() {
            @Override
            public String toXmlImpl() {
                StringBuilder sb = new StringBuilder(3);
                sb.append("\t\t<cwmp:InformResponse>\n");
                sb.append("\t\t\t<MaxEnvelopes>1</MaxEnvelopes>\n");
                sb.append("\t\t</cwmp:InformResponse>\n");
                return sb.toString();
            }
        };
        return new Response(header, body, reqRes.getSessionData().getCwmpVersionNumber());
    }
}
