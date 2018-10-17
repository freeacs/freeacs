package com.github.freeacs.tr069.methods;

import com.github.freeacs.base.Log;
import com.github.freeacs.tr069.HTTPReqResData;
import com.github.freeacs.tr069.xml.Fault;

public class TCDecision {
  public static void process(HTTPReqResData reqRes) {
    try {
      Fault fault = reqRes.getRequest().getFault();
      if (fault != null && !"0".equals(fault.getFaultCode())) {
        String errormsg = "TC request reports a faultcode (" + fault.getFaultCode();
        errormsg += ") with faultstring (" + fault.getFaultString() + ")";
        Log.error(TCDecision.class, errormsg);
      }
    } finally {
      reqRes.getResponse().setMethod(TR069Method.AUTONOMOUS_TRANSFER_COMPLETE);
    }
  }
}
