package com.github.freeacs.tr069.methods.request;

import com.github.freeacs.cache.AcsCache;
import com.github.freeacs.dbi.DBI;
import com.github.freeacs.dbi.Syslog;
import com.github.freeacs.tr069.Properties;
import com.github.freeacs.tr069.http.HTTPRequestResponseData;
import com.github.freeacs.tr069.methods.ProvisioningMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FunctionalInterface
public interface RequestProcessStrategy {
    Logger log = LoggerFactory.getLogger(RequestProcessStrategy.class);

    void process(HTTPRequestResponseData reqRes) throws Exception;

    static RequestProcessStrategy getStrategy(ProvisioningMethod provisioningMethod, Properties properties, DBI dbi, AcsCache acsCache, Syslog syslog) {
        switch (provisioningMethod) {
            case Empty: return doNotProcessStrategy();
            case Download: return downloadStrategy();
            case Fault: return faultStrategy();
            case FactoryReset: return factoryResetStrategy();
            case Inform: return informStrategy(properties, dbi, acsCache);
            case GetParameterNames: return getParameterNamesStrategy(properties, dbi, acsCache);
            case GetParameterValues: return getParameterValuesStrategy();
            case SetParameterValues: return setParameterValuesStrategy(syslog);
            case TransferComplete: return transferCompleteStrategy();
            case AutonomousTransferComplete: return autonomousTransferComplete();
            case Reboot: return rebootStrategy();
            default:
                log.debug("The methodName " + provisioningMethod + " has no request processing strategy");
                return doNotProcessStrategy();
        }
    }

    static RequestProcessStrategy factoryResetStrategy() {
        return new FactoryResetRequestProcessStrategy();
    }

    static RequestProcessStrategy rebootStrategy() {
        return new RebootRequestProcessStrategy();
    }

    static RequestProcessStrategy downloadStrategy() {
        return new DownloadRequestProcessStrategy();
    }

    static RequestProcessStrategy faultStrategy() {
        return new FaultRequestProcessStrategy();
    }

    static RequestProcessStrategy autonomousTransferComplete() {
        return new AutonomousTransferCompleteRequestProcessStrategy();
    }

    static RequestProcessStrategy transferCompleteStrategy() {
        return new TransferCompleteRequestProcessStrategy();
    }

    static RequestProcessStrategy doNotProcessStrategy() {
        return reqRes -> {};
    }

    static RequestProcessStrategy informStrategy(Properties properties, DBI dbi, AcsCache acsCache) {
        return new InformRequestProcessStrategy(properties, dbi, acsCache);
    }

    static RequestProcessStrategy getParameterNamesStrategy(Properties properties, DBI dbi, AcsCache acsCache) {
        return new GetParameterNamesProcessStrategy(properties, dbi, acsCache);
    }

    static RequestProcessStrategy getParameterValuesStrategy() {
        return new GetParameterValuesRequestProcessStrategy();
    }

    static RequestProcessStrategy setParameterValuesStrategy(Syslog syslog) {
        return new SetParameterValuesRequestProcessStrategy(syslog);
    }
}
