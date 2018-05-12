package com.github.freeacs.tr069.methods;

import com.github.freeacs.base.Log;
import com.github.freeacs.tr069.HTTPReqResData;
import com.github.freeacs.tr069.xml.Fault;

public class TCDecision {
	public static void process(HTTPReqResData reqRes) {

		try {
			Fault fault = reqRes.getRequest().getFault();
			if (fault != null && !fault.getFaultCode().equals("0")) {
				String errormsg = "TC request reports a faultcode (" + fault.getFaultCode();
				errormsg += ") with faultstring (" + fault.getFaultString() + ")";
				Log.error(TCDecision.class, errormsg);
//				UnitJob.stop(reqRes.getSessionData(), UnitJobStatus.CONFIRMED_FAILED);
			} else {
//				UnitJob.stop(reqRes.getSessionData(), UnitJobStatus.COMPLETED_OK);
			}
		} finally {
//			DownloadLogic.removeOldest();
			reqRes.getResponse().setMethod(TR069Method.TRANSFER_COMPLETE);
		}
	}

}
