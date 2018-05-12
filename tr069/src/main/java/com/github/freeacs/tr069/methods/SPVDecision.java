package com.github.freeacs.tr069.methods;

import com.github.freeacs.common.db.NoAvailableConnectionException;
import com.github.freeacs.tr069.HTTPReqResData;
import com.github.freeacs.tr069.Properties;
import com.github.freeacs.tr069.SessionData;
import com.owera.xaps.base.Log;
import com.owera.xaps.base.UnitJob;
import com.github.freeacs.dbi.UnitJobStatus;
import com.github.freeacs.dbi.util.ProvisioningMode;
import com.owera.xaps.tr069.HTTPReqResData;
import com.owera.xaps.tr069.Properties;
import com.owera.xaps.tr069.SessionData;

import java.sql.SQLException;

public class SPVDecision {
	public static void process(HTTPReqResData reqRes) throws SQLException, NoAvailableConnectionException {
		SessionData sessionData = reqRes.getSessionData();
		if (sessionData.getUnit().getProvisioningMode() == ProvisioningMode.REGULAR) {
			if (Properties.isParameterkeyQuirk(sessionData) && sessionData.isProvisioningAllowed()) {
				Log.debug(SPVDecision.class, "UnitJob is COMPLETED without verification stage, since CPE does not support ParameterKey");
				UnitJob uj = new UnitJob(sessionData, sessionData.getJob(), false);
				uj.stop(UnitJobStatus.COMPLETED_OK);
			}
		} 
		reqRes.getResponse().setMethod(TR069Method.EMPTY);
	}

}
