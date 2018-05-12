package com.owera.xaps.tr069.methods;

import com.owera.common.db.NoAvailableConnectionException;
import com.owera.xaps.tr069.HTTPReqResData;

import java.sql.SQLException;

public class DoNotProcessReq {

	@SuppressWarnings("unused")
  public static void process(HTTPReqResData reqRes) throws SQLException, NoAvailableConnectionException {
		// Nothing needs to be processed, because there is nothing in the request to process (for example if the request is EMPTY
	}
}
