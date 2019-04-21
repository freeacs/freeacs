package com.github.freeacs.strategies.response;

import com.github.freeacs.http.HTTPRequestResponseData;
import com.github.freeacs.tr069.Properties;
import com.github.freeacs.tr069.methods.Method;
import com.github.freeacs.tr069.xml.Response;

@FunctionalInterface
public interface ResponseCreateStrategy {

    Response getResponse(HTTPRequestResponseData reqRes) throws Exception;

    static ResponseCreateStrategy getStrategy(Method method, Properties properties) {
        switch(method) {
            case Empty:
                return emStrategy();
            case Inform:
                return informStrategy();
            case GetParameterNames:
                return getParameterNamesStrategy(properties);
            case GetParameterValues:
                return getParameterValuesStrategy(properties);
            case SetParameterValues:
                return setParameterValuesStrategy(properties);
            case TransferComplete:
                return transferCompleteStrategy();
            case AutonomousTransferComplete:
                return autonomousTransferCompleteStrategy();
            default:
                return emStrategy();
        }
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
