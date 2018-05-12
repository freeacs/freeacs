package com.github.freeacs.tr069.methods;

import com.github.freeacs.tr069.HTTPReqResData;

public class EM {

	public static void process(HTTPReqResData reqRes) {
		reqRes.getRequest().setMethod(TR069Method.EMPTY);
	}
}
