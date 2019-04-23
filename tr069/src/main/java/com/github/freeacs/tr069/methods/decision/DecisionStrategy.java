package com.github.freeacs.tr069.methods.decision;

import com.github.freeacs.base.Log;
import com.github.freeacs.http.HTTPRequestResponseData;
import com.github.freeacs.tr069.Properties;
import com.github.freeacs.tr069.methods.ProvisioningMethod;

@FunctionalInterface
public interface DecisionStrategy {

    void makeDecision(HTTPRequestResponseData reqRes) throws Exception;

    static DecisionStrategy getStrategy(ProvisioningMethod provisioningMethod, Properties properties) {
        switch (provisioningMethod) {
            case Empty:
                return emStrategy(properties);
            case Inform:
                return informStrategy();
            case GetParameterNames:
                return getParameterNamesStrategy();
            case GetParameterValues:
                return getParameterValuesStrategy(properties);
            case SetParameterValues:
                return setParameterValuesStrategy(properties);
            case TransferComplete:
                return transferCompleteStrategy();
            case AutonomousTransferComplete:
                return autonomousTransferCompleteStrategy();
            case GetRPCMethods:
                return getRPCMethodsStrategy();
            default:
                Log.debug(DecisionStrategy.class,"The methodName " + provisioningMethod + " has no decision strategy");
                return emStrategy(properties);
        }
    }

    static DecisionStrategy getRPCMethodsStrategy() {
        return new GetRPCMethodsDecisionStrategy();
    }

    static DecisionStrategy autonomousTransferCompleteStrategy() {
        return new AutonomousTransferCompleteDecisionStrategy();
    }

    static DecisionStrategy transferCompleteStrategy() {
        return new TransferCompleteDecisionStrategy();
    }

    static DecisionStrategy informStrategy() {
        return new InformDecisionStrategy();
    }

    static DecisionStrategy getParameterNamesStrategy() {
        return new GetParameterNamesDecisionStrategy();
    }

    static DecisionStrategy emStrategy(Properties properties) {
        return new EmptyDecisionStrategy(properties);
    }

    static DecisionStrategy getParameterValuesStrategy(Properties properties) {
        return new GetParameterValuesDecisionStrategy(properties);
    }

    static DecisionStrategy setParameterValuesStrategy(Properties properties) {
        return new SetParameterValuesDecisionStrategy(properties);
    }
}
