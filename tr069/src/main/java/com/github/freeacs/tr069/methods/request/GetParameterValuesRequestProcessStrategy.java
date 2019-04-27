package com.github.freeacs.tr069.methods.request;

import com.github.freeacs.tr069.http.HTTPRequestResponseData;
import com.github.freeacs.tr069.CPEParameters;
import com.github.freeacs.tr069.SessionData;
import com.github.freeacs.tr069.methods.ProvisioningMethod;
import com.github.freeacs.tr069.xml.ParameterValueStruct;
import com.github.freeacs.tr069.xml.Parser;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GetParameterValuesRequestProcessStrategy implements RequestProcessStrategy {

    @SuppressWarnings("Duplicates")
    @Override
    public void process(HTTPRequestResponseData reqRes) throws Exception {
        reqRes.getRequestData().setMethod(ProvisioningMethod.GetParameterNames.name());
        log.debug("Will process XML: " + reqRes.getRequestData().getXml().length() + " char");
        Parser parser = new Parser(reqRes.getRequestData().getXml());
        SessionData sessionData = reqRes.getSessionData();
        if (parser.getHeader().getNoMoreRequests() != null
                && parser.getHeader().getNoMoreRequests().getNoMoreRequestFlag()) {
            sessionData.setNoMoreRequests(true);
        }
        sessionData.setValuesFromCPE(parser.getParameterList().getParameterValueList());
        sessionData.getProvisioningMessage().setParamsRead(sessionData.getValuesFromCPE().size());
        log.debug("Response holds " + sessionData.getValuesFromCPE().size() + " parameters");
        if (sessionData.getValuesFromCPE().size() < sessionData.getRequestedCPE().size()) {
            String msg = "Number of parameters returned from CPE is less than asked for (";
            msg += sessionData.getRequestedCPE().size() + ")";
            log.warn(msg);
        }
        populateCPEParameters(sessionData);
    }

    @SuppressWarnings("Duplicates")
    private void populateCPEParameters(SessionData sessionData) {
        CPEParameters cpeParams = sessionData.getCpeParameters();
        int counter = 0;
        for (ParameterValueStruct pvs : sessionData.getValuesFromCPE()) {
            if (pvs.getName().contains(cpeParams.CONFIG_FILES)) {
                counter++;
                cpeParams.getCpeParams().put(pvs.getName(), pvs);
                //			} else if (pvs.getName().contains(cpeParams.CONFIG_VERSION)) {
                //				counter++;
                //				cpeParams.putPvs(pvs.getName(), pvs);
            } else if (pvs.getName().equals(cpeParams.CONNECTION_URL)) {
                counter++;
                cpeParams.getCpeParams().put(cpeParams.CONNECTION_URL, pvs);
            } else if (pvs.getName().equals(cpeParams.CONNECTION_USERNAME)) {
                counter++;
                cpeParams.getCpeParams().put(cpeParams.CONNECTION_USERNAME, pvs);
            } else if (pvs.getName().equals(cpeParams.CONNECTION_PASSWORD)) {
                counter++;
                cpeParams.getCpeParams().put(cpeParams.CONNECTION_PASSWORD, pvs);
            } else if (pvs.getName().equals(cpeParams.PERIODIC_INFORM_INTERVAL)) {
                counter++;
                cpeParams.getCpeParams().put(cpeParams.PERIODIC_INFORM_INTERVAL, pvs);
            } else if (pvs.getName().equals(cpeParams.SOFTWARE_VERSION)) {
                counter++;
                cpeParams.getCpeParams().put(cpeParams.SOFTWARE_VERSION, pvs);
            }
        }
        log.debug("Found " + counter + " cpe-params (of special interest to ACS) in response");
    }
}
