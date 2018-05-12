package com.github.freeacs.tr069.methods;

import com.github.freeacs.tr069.HTTPReqResData;
import com.github.freeacs.tr069.SessionData;
import com.github.freeacs.tr069.exception.TR069Exception;
import com.github.freeacs.tr069.xml.Parser;

public class GPAres {

	public static void process(HTTPReqResData reqRes) throws TR069Exception {
		reqRes.getRequest().setMethod(TR069Method.GET_PARAMETER_VALUES);
		Parser parser = new Parser(reqRes.getRequest().getXml());
		SessionData sessionData = reqRes.getSessionData();
		sessionData.setAttributesFromCPE(parser.getParameterList().getParameterAttributeList());
	}

}
