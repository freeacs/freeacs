package com.github.freeacs.tr069.methods.decision;

import com.github.freeacs.tr069.http.HTTPRequestResponseData;
import com.github.freeacs.tr069.methods.ProvisioningMethod;
import com.github.freeacs.tr069.xml.Fault;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AutonomousTransferCompleteDecisionStrategy implements DecisionStrategy {
    @SuppressWarnings("Duplicates")
    @Override
    public void makeDecision(HTTPRequestResponseData reqRes) {
        try {
            Fault fault = reqRes.getRequestData().getFault();
            if (fault != null && !"0".equals(fault.getFaultCode())) {
                String errormsg = "ATC request reports a faultcode (" + fault.getFaultCode();
                errormsg += ") with faultstring (" + fault.getFaultString() + ")";
                log.error(errormsg);
            }
        } finally {
            reqRes.getResponseData().setMethod(ProvisioningMethod.AutonomousTransferComplete.name());
        }
    }
}
