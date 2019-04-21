package com.github.freeacs.tr069.methods.response;

import com.github.freeacs.base.Log;
import com.github.freeacs.http.HTTPRequestResponseData;
import com.github.freeacs.tr069.Namespace;
import com.github.freeacs.tr069.ParameterKey;
import com.github.freeacs.tr069.Properties;
import com.github.freeacs.tr069.xml.*;

import java.util.List;

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
            private final String START = "\t\t<cwmp:SetParameterValues>\n";
            private final String END = "\t\t</cwmp:SetParameterValues>\n";
            private final String PARAMETER_LIST_START_1 = "\t\t\t<ParameterList " + Namespace.getSoapEncNS() + ":arrayType=\"cwmp:ParameterValueStruct[";
            private final String PARAMETER_LIST_START_2 = "]\">\n";
            private final String PARAMETER_KEY_START = "\t\t\t<ParameterKey>";
            private final String PARAMETER_KEY_END = "</ParameterKey>\n";
            private final String PARAMETER_VALUE_STRUCT_START = "\t\t\t\t<ParameterValueStruct>\n";
            private final String NAME_START = "\t\t\t\t\t<Name>";
            private final String NAME_END = "</Name>\n";
            private final String PARAMETER_VALUE_STRUCT_END = "\t\t\t\t</ParameterValueStruct>\n";
            private final String PARAMETER_LIST_END = "\t\t\t</ParameterList>\n";

            private List<ParameterValueStruct> parameterValueList = paramList.getParameterValueList();
            private String parameterKey = pk.getServerKey();

            @Override
            public String toXmlImpl() {
                StringBuilder sb = new StringBuilder(50);
                sb.append(START);
                sb.append(PARAMETER_LIST_START_1);
                sb.append(parameterValueList.size());
                sb.append(PARAMETER_LIST_START_2);

                for (ParameterValueStruct pvs : parameterValueList) {
                    sb.append(PARAMETER_VALUE_STRUCT_START);
                    sb.append(NAME_START);
                    sb.append(pvs.getName());
                    sb.append(NAME_END);
                    sb.append("\t\t\t\t\t<Value xsi:type=\"").append(pvs.getType()).append("\">");
                    if (pvs.getType() != null
                            && pvs.getType().contains("int")
                            && (pvs.getValue() == null || "".equals(pvs.getValue().trim()))) {
                        sb.append("0");
                    } else {
                        sb.append(pvs.getValue());
                    }
                    sb.append("</Value>\n");
                    sb.append(PARAMETER_VALUE_STRUCT_END);
                }
                sb.append(PARAMETER_LIST_END);
                if (parameterKey != null) {
                    sb.append(PARAMETER_KEY_START).append(parameterKey).append(PARAMETER_KEY_END);
                }
                sb.append(END);
                return sb.toString();
            }
        };
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
