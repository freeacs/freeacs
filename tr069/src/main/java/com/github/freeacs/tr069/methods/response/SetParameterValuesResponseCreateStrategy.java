package com.github.freeacs.tr069.methods.response;

import com.github.freeacs.tr069.http.HTTPRequestResponseData;
import com.github.freeacs.tr069.ParameterKey;
import com.github.freeacs.tr069.Properties;
import com.github.freeacs.tr069.xml.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
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
        body = new Body() {
            private List<ParameterValueStruct> parameterValueList = paramList.getParameterValueList();
            private String parameterKey = pk.getServerKey();

            @Override
            public String toXmlImpl() {
                StringBuilder sb = new StringBuilder(50);
                sb.append("\t\t<cwmp:SetParameterValues>\n");
                sb.append("\t\t\t<ParameterList ").append("soapenc").append(":arrayType=\"cwmp:ParameterValueStruct[");
                sb.append(parameterValueList.size());
                sb.append("]\">\n");

                for (ParameterValueStruct pvs : parameterValueList) {
                    sb.append("\t\t\t\t<ParameterValueStruct>\n");
                    sb.append("\t\t\t\t\t<Name>");
                    sb.append(pvs.getName());
                    sb.append("</Name>\n");
                    sb.append("\t\t\t\t\t<Value xsi:type=\"").append(pvs.getType()).append("\">");
                    if (pvs.getType() != null
                            && pvs.getType().contains("int")
                            && (pvs.getValue() == null || "".equals(pvs.getValue().trim()))) {
                        sb.append("0");
                    } else {
                        sb.append(pvs.getValue());
                    }
                    sb.append("</Value>\n");
                    sb.append("\t\t\t\t</ParameterValueStruct>\n");
                }
                sb.append("\t\t\t</ParameterList>\n");
                if (parameterKey != null) {
                    sb.append("\t\t\t<ParameterKey>").append(parameterKey).append("</ParameterKey>\n");
                }
                sb.append("\t\t</cwmp:SetParameterValues>\n");
                return sb.toString();
            }
        };
        log.debug("Sent to CPE: " + paramList.getParameterValueList().size() + " parameters.");
        reqRes
                .getSessionData()
                .getProvisioningMessage()
                .setParamsWritten(paramList.getParameterValueList().size());
        return new Response(header, body, reqRes.getSessionData().getCwmpVersionNumber());
    }
}
