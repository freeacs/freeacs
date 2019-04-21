package com.github.freeacs.strategies.response;

import com.github.freeacs.base.Log;
import com.github.freeacs.dbi.UnittypeParameters;
import com.github.freeacs.dbi.util.ProvisioningMode;
import com.github.freeacs.http.HTTPRequestResponseData;
import com.github.freeacs.tr069.CPEParameters;
import com.github.freeacs.tr069.Properties;
import com.github.freeacs.tr069.SessionData;
import com.github.freeacs.tr069.methods.GPVreq;
import com.github.freeacs.tr069.methods.HTTPResponseCreator;
import com.github.freeacs.tr069.methods.Method;
import com.github.freeacs.tr069.xml.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GetParameterValuesResponseCreateStrategy implements ResponseCreateStrategy {

    private final Properties properties;

    public GetParameterValuesResponseCreateStrategy(Properties properties) {
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
            Log.debug(
                    HTTPResponseCreator.class,
                    "Asks for all params ("
                            + sessionData.getKeyRoot()
                            + "), since in "
                            + ProvisioningMode.READALL
                            + " mode");
            ParameterValueStruct pvs = new ParameterValueStruct(sessionData.getKeyRoot(), "");
            parameterValueList.add(pvs);
        } else { // mode == ProvisioningMode.PERIODIC
            // List<RequestResponseData> reqResList = sessionData.getReqResList();
            String previousMethod = sessionData.getPreviousResponseMethod();
            if (properties.isUnitDiscovery(sessionData)
                    || Method.GetParameterValues.name().equals(previousMethod)) {
                Log.debug(
                        HTTPResponseCreator.class,
                        "Asks for all params ("
                                + sessionData.getKeyRoot()
                                + "), either because unitdiscovery-quirk or prev. GPV failed");
                ParameterValueStruct pvs = new ParameterValueStruct(sessionData.getKeyRoot(), "");
                parameterValueList.add(pvs);
            } else {
                Map<String, ParameterValueStruct> paramValueMap = sessionData.getFromDB();
                addCPEParameters(sessionData, properties);
                for (Map.Entry<String, ParameterValueStruct> entry : paramValueMap.entrySet()) {
                    parameterValueList.add(entry.getValue());
                }
                Log.debug(
                        HTTPResponseCreator.class,
                        "Asks for " + parameterValueList.size() + " parameters in GPV-req");
                parameterValueList.sort(new ParameterValueStructComparator());
            }
        }
        sessionData.setRequestedCPE(parameterValueList);
        Body body = new GPVreq(parameterValueList);
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
            Log.debug(
                    HTTPResponseCreator.class,
                    "VendorConfigFile object will be requested (default behavior)");
        } else {
            Log.debug(
                    HTTPResponseCreator.class,
                    "VendorConfigFile object will not be requested. (quirk behavior: old Pingcom device or quirk enabled)");
        }

        int counter = 0;
        for (String key : cpeParams.getCpeParams().keySet()) {
            if ((key.endsWith(".") && useVendorConfigFile)
                    || (paramValueMap.get(key) == null && utps.getByName(key) != null)) {
                paramValueMap.put(key, new ParameterValueStruct(key, "ExtraCPEParam"));
                counter++;
            }
        }
        Log.debug(
                HTTPResponseCreator.class,
                counter
                        + " cpe-param (not found in database, but of special interest to ACS) added to the GPV-request");
    }
}
