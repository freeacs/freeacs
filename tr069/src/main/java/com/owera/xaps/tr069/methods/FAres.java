package com.owera.xaps.tr069.methods;

import com.owera.xaps.tr069.HTTPReqResData;
import com.owera.xaps.tr069.exception.TR069Exception;
import com.owera.xaps.tr069.xml.Parser;

public class FAres {

	public static void process(HTTPReqResData reqRes) throws TR069Exception {
		reqRes.getRequest().setMethod(TR069Method.FAULT);
		Parser parser = new Parser(reqRes.getRequest().getXml());
		reqRes.getRequest().setFault(parser.getFault());

	}
}
