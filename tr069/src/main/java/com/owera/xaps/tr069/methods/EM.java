package com.owera.xaps.tr069.methods;

import com.owera.xaps.tr069.HTTPReqResData;

public class EM {

	public static void process(HTTPReqResData reqRes) {
		reqRes.getRequest().setMethod(TR069Method.EMPTY);
	}
}
