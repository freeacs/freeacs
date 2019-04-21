package com.github.freeacs.strategies.decision;

import com.github.freeacs.base.Log;
import com.github.freeacs.base.UnitJob;
import com.github.freeacs.dbi.UnitJobStatus;
import com.github.freeacs.dbi.util.ProvisioningMode;
import com.github.freeacs.http.HTTPRequestResponseData;
import com.github.freeacs.tr069.Properties;
import com.github.freeacs.tr069.SessionData;
import com.github.freeacs.tr069.methods.Method;
import com.github.freeacs.tr069.methods.SPVDecision;

public class SetParameterValuesDecisionStrategy implements DecisionStrategy {
    private final Properties properties;

    public SetParameterValuesDecisionStrategy(Properties properties) {
        this.properties = properties;
    }

    @SuppressWarnings("Duplicates")
    @Override
    public void makeDecision(HTTPRequestResponseData reqRes) throws Exception {
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
        reqRes.getResponseData().setMethod(Method.Empty.name());
    }
}
