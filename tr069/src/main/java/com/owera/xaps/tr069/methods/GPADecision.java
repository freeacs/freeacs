package com.owera.xaps.tr069.methods;

import com.owera.xaps.tr069.HTTPReqResData;

public class GPADecision {
	public static void process(HTTPReqResData reqRes) {
		// Dummy implementation - this decision is not used yet
		reqRes.getResponse().setMethod(TR069Method.EMPTY);
	}

}
