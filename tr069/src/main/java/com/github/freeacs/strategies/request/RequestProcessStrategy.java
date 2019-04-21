package com.github.freeacs.strategies.request;

import com.github.freeacs.http.HTTPRequestResponseData;
import com.github.freeacs.tr069.Properties;
import com.github.freeacs.tr069.methods.Method;

@FunctionalInterface
public interface RequestProcessStrategy {

    void process(HTTPRequestResponseData reqRes) throws Exception;

    static RequestProcessStrategy getStrategy(Method method, Properties properties) {
        switch (method) {
            case Empty:
                return doNotProcessStrategy();
            case Inform:
                return informStrategy(properties);
            case GetParameterNames:
                return getParameterNamesStrategy(properties);
            case GetParameterValues:
                return getParameterValuesStrategy();
            case SetParameterValues:
                return setParameterValuesStrategy();
            default:
                return doNotProcessStrategy();
        }
    }

    static RequestProcessStrategy doNotProcessStrategy() {
        return new DoNotProcessRequestStrategy();
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
