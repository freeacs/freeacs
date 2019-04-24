package com.github.freeacs.tr069.methods.decision;

import com.github.freeacs.tr069.base.Log;
import com.github.freeacs.tr069.base.UnitJob;
import com.github.freeacs.dbaccess.DBAccess;
import com.github.freeacs.dbi.UnitJobStatus;
import com.github.freeacs.dbi.util.ProvisioningMode;
import com.github.freeacs.http.HTTPRequestResponseData;
import com.github.freeacs.tr069.Properties;
import com.github.freeacs.tr069.SessionData;
import com.github.freeacs.tr069.methods.ProvisioningMethod;

public class SetParameterValuesDecisionStrategy implements DecisionStrategy {
    private final Properties properties;
    private final DBAccess dbAccess;

    public SetParameterValuesDecisionStrategy(Properties properties, DBAccess dbAccess) {
        this.properties = properties;
        this.dbAccess = dbAccess;
    }

    @SuppressWarnings("Duplicates")
    @Override
    public void makeDecision(HTTPRequestResponseData reqRes) {
        SessionData sessionData = reqRes.getSessionData();
        if (sessionData.getUnit().getProvisioningMode() == ProvisioningMode.REGULAR
                && properties.isParameterkeyQuirk(sessionData)
                && sessionData.isProvisioningAllowed()) {
            Log.debug(
                    SetParameterValuesDecisionStrategy.class,
                    "UnitJob is COMPLETED without verification stage, since CPE does not support ParameterKey");
            UnitJob uj = new UnitJob(sessionData, dbAccess.getDbi().getAcs(), sessionData.getJob(), false);
            uj.stop(UnitJobStatus.COMPLETED_OK, properties.isDiscoveryMode());
        }
        reqRes.getResponseData().setMethod(ProvisioningMethod.Empty.name());
    }
}
