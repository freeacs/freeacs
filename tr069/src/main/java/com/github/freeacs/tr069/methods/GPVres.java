package com.github.freeacs.tr069.methods;

import com.github.freeacs.base.Log;
import com.github.freeacs.tr069.CPEParameters;
import com.github.freeacs.tr069.HTTPReqResData;
import com.github.freeacs.tr069.SessionData;
import com.github.freeacs.tr069.exception.TR069Exception;
import com.github.freeacs.tr069.xml.ParameterValueStruct;
import com.github.freeacs.tr069.xml.Parser;

public class GPVres {

	private static void populateCPEParameters(SessionData sessionData) {
		CPEParameters cpeParams = sessionData.getCpeParameters();
		int counter = 0;
		for (ParameterValueStruct pvs : sessionData.getFromCPE()) {
			if (pvs.getName().contains(cpeParams.CONFIG_FILES)) {
				counter++;
				cpeParams.putPvs(pvs.getName(), pvs);		
				//			} else if (pvs.getName().contains(cpeParams.CONFIG_VERSION)) {
				//				counter++;
				//				cpeParams.putPvs(pvs.getName(), pvs);
			} else if (pvs.getName().equals(cpeParams.CONNECTION_URL)) {
				counter++;
				cpeParams.putPvs(cpeParams.CONNECTION_URL, pvs);
			} else if (pvs.getName().equals(cpeParams.CONNECTION_USERNAME)) {
				counter++;
				cpeParams.putPvs(cpeParams.CONNECTION_USERNAME, pvs);
			} else if (pvs.getName().equals(cpeParams.CONNECTION_PASSWORD)) {
				counter++;
				cpeParams.putPvs(cpeParams.CONNECTION_PASSWORD, pvs);
			} else if (pvs.getName().equals(cpeParams.PERIODIC_INFORM_INTERVAL)) {
				counter++;
				cpeParams.putPvs(cpeParams.PERIODIC_INFORM_INTERVAL, pvs);
			} else if (pvs.getName().equals(cpeParams.SOFTWARE_VERSION)) {
				counter++;
				cpeParams.putPvs(cpeParams.SOFTWARE_VERSION, pvs);
			}
		}
		Log.debug(GPVres.class, "Found " + counter + " cpe-params (of special interest to ACS) in response");
	}

	public static void process(HTTPReqResData reqRes) throws TR069Exception {
		reqRes.getRequest().setMethod(TR069Method.GET_PARAMETER_VALUES);
		Log.debug(GPVres.class, "Will process XML: " + reqRes.getRequest().getXml().length() + " char");
		Parser parser = new Parser(reqRes.getRequest().getXml());
		SessionData sessionData = reqRes.getSessionData();
		if (parser.getHeader().getNoMoreRequests() != null && parser.getHeader().getNoMoreRequests().getNoMoreRequestFlag())
			sessionData.setNoMoreRequests(true);
		sessionData.setFromCPE(parser.getParameterList().getParameterValueList());
		sessionData.getProvisioningMessage().setParamsRead(sessionData.getFromCPE().size());
		Log.debug(GPVres.class, "Response holds " + sessionData.getFromCPE().size() + " parameters");
		if (sessionData.getFromCPE().size() < sessionData.getRequestedCPE().size()) {
			String msg = "Number of parameters returned from CPE is less than asked for (";
			msg += sessionData.getRequestedCPE().size() + ")";
			Log.warn(GPVres.class, msg);
		}
		populateCPEParameters(sessionData);
	}

}
