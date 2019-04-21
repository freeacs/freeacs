package com.github.freeacs.tr069.methods.decision;

import com.github.freeacs.base.Log;
import com.github.freeacs.http.HTTPRequestResponseData;
import com.github.freeacs.tr069.methods.ProvisioningMethod;
import com.github.freeacs.tr069.xml.Fault;

public class TransferCompleteDecisionStrategy implements DecisionStrategy {
    @SuppressWarnings("Duplicates")
    @Override
    public void makeDecision(HTTPRequestResponseData reqRes) {
        try {
            Fault fault = reqRes.getRequestData().getFault();
            if (fault != null && !"0".equals(fault.getFaultCode())) {
                String errormsg = "TC request reports a faultcode (" + fault.getFaultCode();
                errormsg += ") with faultstring (" + fault.getFaultString() + ")";
                Log.error(TransferCompleteDecisionStrategy.class, errormsg);
            }
        } finally {
            if (reqRes.getSessionData().isAutonomousTransferComplete()) {
                reqRes.getResponseData().setMethod(ProvisioningMethod.AutonomousTransferComplete.name());
            } else {
                reqRes.getResponseData().setMethod(ProvisioningMethod.TransferComplete.name());
            }
        }
    }
}
