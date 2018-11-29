package com.github.freeacs.tr069.methods;

import com.github.freeacs.base.Log;
import com.github.freeacs.tr069.HTTPRequestResponseData;
import com.github.freeacs.tr069.xml.Fault;

public class ATCDecision {
  public static void process(HTTPRequestResponseData reqRes) {
    try {
      Fault fault = reqRes.getRequestData().getFault();
      if (fault != null && !"0".equals(fault.getFaultCode())) {
        String errormsg = "ATC request reports a faultcode (" + fault.getFaultCode();
        errormsg += ") with faultstring (" + fault.getFaultString() + ")";
        Log.error(ATCDecision.class, errormsg);
      }
    } finally {
      reqRes.getResponseData().setMethod(TR069Method.AUTONOMOUS_TRANSFER_COMPLETE);
    }
  }
}
