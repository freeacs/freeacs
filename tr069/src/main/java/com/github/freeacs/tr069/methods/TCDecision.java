package com.github.freeacs.tr069.methods;

import com.github.freeacs.base.Log;
import com.github.freeacs.http.HTTPRequestResponseData;
import com.github.freeacs.tr069.xml.Fault;

public class TCDecision {
  public static void process(HTTPRequestResponseData reqRes) {
    try {
      Fault fault = reqRes.getRequestData().getFault();
      if (fault != null && !"0".equals(fault.getFaultCode())) {
        String errormsg = "TC request reports a faultcode (" + fault.getFaultCode();
        errormsg += ") with faultstring (" + fault.getFaultString() + ")";
        Log.error(TCDecision.class, errormsg);
      }
    } finally {
      if (reqRes.getSessionData().isAutonomousTransferComplete()) {
        reqRes.getResponseData().setMethod(TR069Method.AUTONOMOUS_TRANSFER_COMPLETE);
      } else {
        reqRes.getResponseData().setMethod(TR069Method.TRANSFER_COMPLETE);
      }
    }
  }
}
