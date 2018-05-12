package com.github.freeacs.tr069.methods;

import com.github.freeacs.tr069.HTTPReqResData;

public class GPADecision {
	public static void process(HTTPReqResData reqRes) {
		// Dummy implementation - this decision is not used yet
		reqRes.getResponse().setMethod(TR069Method.EMPTY);
	}

}
