package com.github.freeacs.tr069.methods.response;

import com.github.freeacs.tr069.http.HTTPRequestResponseData;
import com.github.freeacs.tr069.Properties;
import com.github.freeacs.tr069.methods.ProvisioningMethod;
import com.github.freeacs.tr069.xml.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FunctionalInterface
public interface ResponseCreateStrategy {
    Logger log = LoggerFactory.getLogger(ResponseCreateStrategy.class);

    Response getResponse(HTTPRequestResponseData reqRes) throws Exception;

    static ResponseCreateStrategy getStrategy(ProvisioningMethod provisioningMethod, Properties properties) {
        switch(provisioningMethod) {
            case Empty: return emStrategy();
            case Download: return downloadStrategy(properties);
            case FactoryReset: return factoryResetStrategy();
            case Inform: return informStrategy();
            case GetParameterNames: return getParameterNamesStrategy(properties);
            case GetParameterValues: return getParameterValuesStrategy(properties);
            case SetParameterValues: return setParameterValuesStrategy(properties);
            case TransferComplete: return transferCompleteStrategy();
            case AutonomousTransferComplete: return autonomousTransferCompleteStrategy();
            case Reboot: return rebootStrategy();
            case GetRPCMethods: return getRPCMethodsStrategy();
            default:
                log.error("The methodName " + provisioningMethod + " has no response strategy");
                return emStrategy();
        }
    }

    static ResponseCreateStrategy getRPCMethodsStrategy() {
        return new GetRPCMethodsResponseCreateStrategy();
    }

    static ResponseCreateStrategy factoryResetStrategy() {
        return new FactoryResetResponseCreateStrategy();
    }

    static ResponseCreateStrategy rebootStrategy() {
        return new RebootResponseCreateStrategy();
    }

    static ResponseCreateStrategy downloadStrategy(Properties properties) {
        return new DownloadResponseCreateStrategy(properties);
    }

    static ResponseCreateStrategy autonomousTransferCompleteStrategy() {
        return new AutonomousTransferCompleteResponseCreateStrategy();
    }

    static ResponseCreateStrategy transferCompleteStrategy() {
        return new TransferCompleteResponseCreateStrategy();
    }

    static ResponseCreateStrategy emStrategy() {
        return new EmptyResponseCreateStrategy();
    }

    static ResponseCreateStrategy informStrategy() {
        return new InformResponseCreateStrategy();
    }

    static ResponseCreateStrategy getParameterNamesStrategy(Properties properties) {
        return new GetParameterNamesResponseCreateStrategy(properties);
    }

    static ResponseCreateStrategy getParameterValuesStrategy(Properties properties) {
        return new GetParameterValuesResponseCreateStrategy(properties);
    }

    static ResponseCreateStrategy setParameterValuesStrategy(Properties properties) {
        return new SetParameterValuesResponseCreateStrategy(properties);
    }
}
