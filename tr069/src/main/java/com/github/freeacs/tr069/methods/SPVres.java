package com.github.freeacs.tr069.methods;

import com.github.freeacs.base.Log;
import com.github.freeacs.dbi.SyslogConstants;
import com.github.freeacs.dbi.util.SyslogClient;
import com.github.freeacs.tr069.HTTPReqResData;
import com.github.freeacs.tr069.Provisioning;
import com.github.freeacs.tr069.SessionData;
import com.github.freeacs.tr069.exception.TR069Exception;
import com.github.freeacs.tr069.xml.ParameterList;
import com.github.freeacs.tr069.xml.ParameterValueStruct;
import com.github.freeacs.tr069.xml.Parser;

public class SPVres {

	public static void process(HTTPReqResData reqRes) throws TR069Exception {
		reqRes.getRequest().setMethod(TR069Method.SET_PARAMETER_VALUES);
		Parser parser = new Parser(reqRes.getRequest().getXml());
		if (parser.getHeader().getNoMoreRequests() != null && parser.getHeader().getNoMoreRequests().getNoMoreRequestFlag())
			reqRes.getSessionData().setNoMoreRequests(true);
		SessionData sessionData = reqRes.getSessionData();
		ParameterList paramList = sessionData.getToCPE();
		for (ParameterValueStruct pvs : paramList.getParameterValueList()) {
			Log.notice(HTTPResponseCreator.class, "\t" + pvs.getName() + " : " + pvs.getValue());
			String user = sessionData.getDbAccessSession().getAcs().getSyslog().getIdentity().getUser().getUsername();
			SyslogClient.notice(sessionData.getUnitId(), "ProvMsg: Written to CPE: " + pvs.getName() + " = " + pvs.getValue(), SyslogConstants.FACILITY_TR069, Provisioning.VERSION, user);
		}

	}
}
