package com.github.freeacs.tr069.methods.decision;

import com.github.freeacs.dbi.DBI;
import com.github.freeacs.tr069.base.UnitJob;
import com.github.freeacs.dbi.UnitJobStatus;
import com.github.freeacs.dbi.util.ProvisioningMode;
import com.github.freeacs.tr069.http.HTTPRequestResponseData;
import com.github.freeacs.tr069.Properties;
import com.github.freeacs.tr069.SessionData;
import com.github.freeacs.tr069.methods.ProvisioningMethod;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SetParameterValuesDecisionStrategy implements DecisionStrategy {
    private final Properties properties;
    private final DBI dbi;

    SetParameterValuesDecisionStrategy(Properties properties, DBI dbi) {
        this.properties = properties;
        this.dbi = dbi;
    }

    @SuppressWarnings("Duplicates")
    @Override
    public void makeDecision(HTTPRequestResponseData reqRes) {
        SessionData sessionData = reqRes.getSessionData();
        if (sessionData.getUnit().getProvisioningMode() == ProvisioningMode.REGULAR
                && properties.isParameterkeyQuirk(sessionData)
                && sessionData.isProvisioningAllowed()) {
            log.debug("UnitJob is COMPLETED without verification stage, since CPE does not support ParameterKey");
            UnitJob uj = new UnitJob(sessionData, dbi, sessionData.getJob(), false);
            uj.stop(UnitJobStatus.COMPLETED_OK, properties.isDiscoveryMode());
        }
        reqRes.getResponseData().setMethod(ProvisioningMethod.Empty.name());
    }
}
