package com.github.freeacs.tr069.methods;

import com.github.freeacs.base.Log;
import com.github.freeacs.base.UnitJob;
import com.github.freeacs.dbi.UnitJobStatus;
import com.github.freeacs.dbi.util.ProvisioningMode;
import com.github.freeacs.http.HTTPRequestResponseData;
import com.github.freeacs.tr069.Properties;
import com.github.freeacs.tr069.SessionData;

public class SPVDecision {
  public static void process(HTTPRequestResponseData reqRes, Properties properties) {
    SessionData sessionData = reqRes.getSessionData();
    if (sessionData.getUnit().getProvisioningMode() == ProvisioningMode.REGULAR
        && properties.isParameterkeyQuirk(sessionData)
        && sessionData.isProvisioningAllowed()) {
      Log.debug(
          SPVDecision.class,
          "UnitJob is COMPLETED without verification stage, since CPE does not support ParameterKey");
      UnitJob uj = new UnitJob(sessionData, sessionData.getJob(), false);
      uj.stop(UnitJobStatus.COMPLETED_OK, properties.isDiscoveryMode());
    }
    reqRes.getResponseData().setMethod(TR069Method.EMPTY);
  }
}
