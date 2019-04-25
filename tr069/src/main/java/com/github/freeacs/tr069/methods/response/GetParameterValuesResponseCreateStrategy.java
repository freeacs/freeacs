package com.github.freeacs.tr069.methods.response;

import com.github.freeacs.dbi.UnittypeParameters;
import com.github.freeacs.dbi.util.ProvisioningMode;
import com.github.freeacs.tr069.http.HTTPRequestResponseData;
import com.github.freeacs.tr069.CPEParameters;
import com.github.freeacs.tr069.Properties;
import com.github.freeacs.tr069.SessionData;
import com.github.freeacs.tr069.methods.ProvisioningMethod;
import com.github.freeacs.tr069.xml.Body;
import com.github.freeacs.tr069.xml.Header;
import com.github.freeacs.tr069.xml.ParameterValueStruct;
import com.github.freeacs.tr069.xml.ParameterValueStructComparator;
import com.github.freeacs.tr069.xml.Response;
import com.github.freeacs.tr069.xml.TR069TransactionID;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class GetParameterValuesResponseCreateStrategy implements ResponseCreateStrategy {

    private final Properties properties;

    GetParameterValuesResponseCreateStrategy(Properties properties) {
        this.properties = properties;
    }

    @SuppressWarnings("Duplicates")
    @Override
    public Response getResponse(HTTPRequestResponseData reqRes) {
        if (reqRes.getTR069TransactionID() == null) {
            reqRes.setTR069TransactionID(new TR069TransactionID("FREEACS-" + System.currentTimeMillis()));
        }
        Header header = new Header(reqRes.getTR069TransactionID(), null, null);
        SessionData sessionData = reqRes.getSessionData();
        ProvisioningMode mode = sessionData.getUnit().getProvisioningMode();
        List<ParameterValueStruct> parameterValueList = new ArrayList<>();
        if (mode == ProvisioningMode.READALL) {
            log.debug("Asks for all params (" + sessionData.getKeyRoot() + "), since in " + ProvisioningMode.READALL + " mode");
            ParameterValueStruct pvs = new ParameterValueStruct(sessionData.getKeyRoot(), "");
            parameterValueList.add(pvs);
        } else { // mode == ProvisioningMode.PERIODIC
            // List<RequestResponseData> reqResList = sessionData.getReqResList();
            String previousMethod = sessionData.getPreviousResponseMethod();
            if (properties.isUnitDiscovery(sessionData)
                    || ProvisioningMethod.GetParameterValues.name().equals(previousMethod)) {
                log.debug("Asks for all params (" + sessionData.getKeyRoot() + "), either because unitdiscovery-quirk or prev. GPV failed");
                ParameterValueStruct pvs = new ParameterValueStruct(sessionData.getKeyRoot(), "");
                parameterValueList.add(pvs);
            } else {
                Map<String, ParameterValueStruct> paramValueMap = sessionData.getFromDB();
                addCPEParameters(sessionData, properties);
                for (Map.Entry<String, ParameterValueStruct> entry : paramValueMap.entrySet()) {
                    parameterValueList.add(entry.getValue());
                }
                log.debug("Asks for " + parameterValueList.size() + " parameters in GPV-req");
                parameterValueList.sort(new ParameterValueStructComparator());
            }
        }
        sessionData.setRequestedCPE(parameterValueList);
        Body body = new Body() {
            public String toXmlImpl() {
                StringBuilder sb = new StringBuilder(3);
                sb.append("\t\t<cwmp:GetParameterValues>\n");
                sb.append("\t\t\t<ParameterNames ")
                        .append("soapenc")
                        .append(":arrayType=\"xsd:string[")
                        .append(parameterValueList.size())
                        .append("]\">\n");

                for (ParameterValueStruct parameter : parameterValueList) {
                    sb.append("\t\t\t\t<string>").append(parameter.getName()).append("</string>\n");
                }
                sb.append("\t\t\t</ParameterNames>\n");
                sb.append("\t\t</cwmp:GetParameterValues>\n");
                return sb.toString();
            }
        };
        return new Response(header, body, reqRes.getSessionData().getCwmpVersionNumber());
    }

    @SuppressWarnings("Duplicates")
    private void addCPEParameters(SessionData sessionData, Properties properties) {
        Map<String, ParameterValueStruct> paramValueMap = sessionData.getFromDB();
        CPEParameters cpeParams = sessionData.getCpeParameters();
        UnittypeParameters utps = sessionData.getUnittype().getUnittypeParameters();

        // If device is not old Ping Communication device (NPA201E or RGW208EN) and
        // vendor config file is not explicitely turned off,
        // then we may ask for VendorConfigFile object.
        String unitId = sessionData.getUnitId();
        boolean useVendorConfigFile =
                !(unitId.contains("NPA201E") || unitId.contains("RGW208EN") || unitId.contains("NPA101E"))
                        && !properties.isIgnoreVendorConfigFile(sessionData);
        if (useVendorConfigFile) {
            log.debug("VendorConfigFile object will be requested (default behavior)");
        } else {
            log.debug("VendorConfigFile object will not be requested. (quirk behavior: old Pingcom device or quirk enabled)");
        }

        int counter = 0;
        for (String key : cpeParams.getCpeParams().keySet()) {
            if ((key.endsWith(".") && useVendorConfigFile)
                    || (paramValueMap.get(key) == null && utps.getByName(key) != null)) {
                paramValueMap.put(key, new ParameterValueStruct(key, "ExtraCPEParam"));
                counter++;
            }
        }
        log.debug(counter + " cpe-param (not found in database, but of special interest to ACS) added to the GPV-request");
    }
}
