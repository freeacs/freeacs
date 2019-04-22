package com.github.freeacs.tr069.methods.request;

import com.github.freeacs.http.HTTPRequestResponseData;
import com.github.freeacs.tr069.Properties;
import com.github.freeacs.tr069.methods.ProvisioningMethod;

@FunctionalInterface
public interface RequestProcessStrategy {

    void process(HTTPRequestResponseData reqRes) throws Exception;

    static RequestProcessStrategy getStrategy(ProvisioningMethod provisioningMethod, Properties properties) {
        switch (provisioningMethod) {
            case Empty:
                return doNotProcessStrategy();
            case Download:
                return downloadStrategy();
            case Fault:
                return faultStrategy();
            case FactoryReset:
                return factoryResetStrategy();
            case Inform:
                return informStrategy(properties);
            case GetParameterNames:
                return getParameterNamesStrategy(properties);
            case GetParameterValues:
                return getParameterValuesStrategy();
            case SetParameterValues:
                return setParameterValuesStrategy();
            case TransferComplete:
                return transferCompleteStrategy();
            case AutonomousTransferComplete:
                return autonomousTransferComplete();
            case Reboot:
                return rebootStrategy();
            default:
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

    static RequestProcessStrategy informStrategy(Properties properties) {
        return new InformRequestProcessStrategy(properties);
    }

    static RequestProcessStrategy getParameterNamesStrategy(Properties properties) {
        return new GetParameterNamesProcessStrategy(properties);
    }

    static RequestProcessStrategy getParameterValuesStrategy() {
        return new GetParameterValuesRequestProcessStrategy();
    }

    static RequestProcessStrategy setParameterValuesStrategy() {
        return new SetParameterValuesRequestProcessStrategy();
    }
}
