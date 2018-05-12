package com.github.freeacs.tr069.methods;

import com.github.freeacs.base.Log;
import com.github.freeacs.tr069.HTTPReqResData;
import com.github.freeacs.tr069.exception.TR069Exception;
import com.github.freeacs.tr069.xml.Header;
import com.github.freeacs.tr069.xml.Parser;

public class TCreq {

	public static void process(HTTPReqResData reqRes) throws TR069Exception {
		reqRes.getRequest().setMethod(TR069Method.TRANSFER_COMPLETE);
		Parser parser = new Parser(reqRes.getRequest().getXml());
		Header header = parser.getHeader();
    	reqRes.setTR069TransactionID(header.getId());
		if (parser.getFault() != null && !parser.getFault().getFaultCode().equals("0")) {
			reqRes.getRequest().setFault(parser.getFault());
			Log.debug(TCreq.class, "TCReq reported a fault");
		} else {
			Log.debug(TCreq.class, "TCReq is ok (download is assumed ok)");
		}
	}
}
