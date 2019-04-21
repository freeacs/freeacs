package com.github.freeacs.tr069.methods.response;

import com.github.freeacs.http.HTTPRequestResponseData;
import com.github.freeacs.tr069.xml.Body;
import com.github.freeacs.tr069.xml.Header;
import com.github.freeacs.tr069.xml.Response;
import com.github.freeacs.tr069.xml.TR069TransactionID;

public class RebootResponseCreateStrategy implements ResponseCreateStrategy {
    @SuppressWarnings("Duplicates")
    @Override
    public Response getResponse(HTTPRequestResponseData reqRes) {
        if (reqRes.getTR069TransactionID() == null) {
            reqRes.setTR069TransactionID(new TR069TransactionID("FREEACS-" + System.currentTimeMillis()));
        }
        TR069TransactionID tr069ID = reqRes.getTR069TransactionID();
        Header header = new Header(tr069ID, null, null);
        Body body = new Body() {
            @Override
            public String toXmlImpl() {
                StringBuilder sb = new StringBuilder(3);
                sb.append("\t<cwmp:Reboot xmlns:cwmp=\"urn:dslforum-org:cwmp-1-0\">\n");
                sb.append("\t\t<CommandKey>Reboot_FREEACS-")
                        .append(System.currentTimeMillis())
                        .append("</CommandKey>\n");
                sb.append("\t</cwmp:Reboot>\n");
                return sb.toString();
            }
        };
        return new Response(header, body, reqRes.getSessionData().getCwmpVersionNumber());
    }
}
