package com.github.freeacs.tr069.methods;

import com.github.freeacs.base.Log;
import com.github.freeacs.dbi.util.ProvisioningMessage;
import com.github.freeacs.dbi.util.ProvisioningMessage.ErrorResponsibility;
import com.github.freeacs.dbi.util.ProvisioningMessage.ProvStatus;
import com.github.freeacs.dbi.util.ProvisioningMode;
import com.github.freeacs.tr069.HTTPReqResData;
import com.github.freeacs.tr069.SessionData;
import com.github.freeacs.tr069.xml.Fault;

import java.util.List;

public class FADecision {
	public static void process(HTTPReqResData reqRes) {

		SessionData sessionData = reqRes.getSessionData();
		if (sessionData.getUnit().getProvisioningMode() == ProvisioningMode.REGULAR) {
			List<HTTPReqResData> reqResList = sessionData.getReqResList();
			if (reqResList != null && reqResList.size() >= 3) {
				HTTPReqResData prevReqRes = reqResList.get(reqResList.size() - 2);
				HTTPReqResData prev2ReqRes = reqResList.get(reqResList.size() - 3);
				String prevMethod = prevReqRes.getResponse().getMethod();
				if (prevMethod.equals(TR069Method.GET_PARAMETER_VALUES)) {
					String prev2Method = prev2ReqRes.getResponse().getMethod();
					if (!prev2Method.equals(TR069Method.GET_PARAMETER_VALUES)) {
						Log.warn(FADecision.class, "GPVres contained error, try once more and ask for all parameters");
						reqRes.getResponse().setMethod(TR069Method.GET_PARAMETER_VALUES);
						return;
					}
				}
			}
		}

		ProvisioningMessage pm = sessionData.getProvisioningMessage();
		Fault fault = reqRes.getRequest().getFault();
		pm.setErrorCode(new Integer(fault.getFaultCode()));
		pm.setErrorMessage(fault.getFaultString());
		pm.setErrorResponsibility(ErrorResponsibility.CLIENT);
		pm.setProvStatus(ProvStatus.ERROR);
		reqRes.getResponse().setMethod(TR069Method.EMPTY);
	}
}
