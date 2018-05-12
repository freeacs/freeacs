package com.github.freeacs.tr069.methods;

import com.github.freeacs.tr069.HTTPReqResData;
import com.github.freeacs.tr069.exception.TR069Exception;
import com.github.freeacs.tr069.xml.Parser;

public class FAres {

	public static void process(HTTPReqResData reqRes) throws TR069Exception {
		reqRes.getRequest().setMethod(TR069Method.FAULT);
		Parser parser = new Parser(reqRes.getRequest().getXml());
		reqRes.getRequest().setFault(parser.getFault());

	}
}
