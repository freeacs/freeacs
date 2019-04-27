package com.github.freeacs.tr069.methods.response;

import com.github.freeacs.tr069.http.HTTPRequestResponseData;
import com.github.freeacs.tr069.methods.ProvisioningMethod;
import com.github.freeacs.tr069.xml.Body;
import com.github.freeacs.tr069.xml.Header;
import com.github.freeacs.tr069.xml.Response;

public class GetRPCMethodsResponseCreateStrategy implements ResponseCreateStrategy {
    @Override
    public Response getResponse(HTTPRequestResponseData reqRes) {
        Header header = new Header(reqRes.getTR069TransactionID(), null, null);
        Body body = new Body() {
            @Override
            public String toXmlImpl() {
                return  "<cwmp:GetRPCMethodsResponse>\n" +
                        "<MethodList soapenc:arrayType=\"xsd:string[12]\">\n" +
                        createMethodList() +
                        "</MethodList>" +
                        "</cwmp:GetRPCMethodsResponse>";
            }
        };
        return new Response(header, body, reqRes.getSessionData().getCwmpVersionNumber());
    }

    private String createMethodList() {
        StringBuilder sb = new StringBuilder();
        sb.append("<string>").append(ProvisioningMethod.Inform.name()).append("</string>\n");
        sb.append("<string>").append(ProvisioningMethod.GetRPCMethods.name()).append("</string>\n");
        sb.append("<string>").append(ProvisioningMethod.TransferComplete.name()).append("</string>\n");
        sb.append("<string>").append(ProvisioningMethod.AutonomousTransferComplete.name()).append("</string>\n");
        return sb.toString();
    }
}
