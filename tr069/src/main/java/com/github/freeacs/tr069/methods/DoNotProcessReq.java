package com.github.freeacs.tr069.methods;

import com.github.freeacs.tr069.HTTPReqResData;

import java.sql.SQLException;

public class DoNotProcessReq {

    public static void process(HTTPReqResData reqRes) throws SQLException {
		// Nothing needs to be processed, because there is nothing in the request to process (for example if the request is EMPTY
	}
}
