package com.github.freeacs.tr069.methods;

import com.github.freeacs.base.Log;
import com.github.freeacs.tr069.HTTPReqResData;
import com.github.freeacs.tr069.xml.Fault;

public class ATCDecision {
  public static void process(HTTPReqResData reqRes) {

    try {
      Fault fault = reqRes.getRequest().getFault();
      if (fault != null && !fault.getFaultCode().equals("0")) {
        String errormsg = "ATC request reports a faultcode (" + fault.getFaultCode();
        errormsg += ") with faultstring (" + fault.getFaultString() + ")";
        Log.error(ATCDecision.class, errormsg);
      }
    } finally {
      reqRes.getResponse().setMethod(TR069Method.AUTONOMOUS_TRANSFER_COMPLETE);
    }
  }
}
