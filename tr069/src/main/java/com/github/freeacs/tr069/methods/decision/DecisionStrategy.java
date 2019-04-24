package com.github.freeacs.tr069.methods.decision;

import com.github.freeacs.dbi.DBI;
import com.github.freeacs.tr069.base.Log;
import com.github.freeacs.http.HTTPRequestResponseData;
import com.github.freeacs.tr069.Properties;
import com.github.freeacs.tr069.methods.ProvisioningMethod;

@FunctionalInterface
public interface DecisionStrategy {

    void makeDecision(HTTPRequestResponseData reqRes) throws Exception;

    static DecisionStrategy getStrategy(ProvisioningMethod provisioningMethod,
                                        Properties properties,
                                        DBI dbi) {
        switch (provisioningMethod) {
            case Empty: return emStrategy(properties, dbi);
            case Inform: return informStrategy();
            case GetParameterNames: return getParameterNamesStrategy();
            case GetParameterValues: return getParameterValuesStrategy(properties, dbi);
            case SetParameterValues: return setParameterValuesStrategy(properties, dbi);
            case TransferComplete: return transferCompleteStrategy();
            case AutonomousTransferComplete: return autonomousTransferCompleteStrategy();
            case GetRPCMethods: return getRPCMethodsStrategy();
            default:
                Log.debug(DecisionStrategy.class,"The methodName " + provisioningMethod + " has no decision strategy. Using empty strategy.");
                return emStrategy(properties, dbi);
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

    static DecisionStrategy emStrategy(Properties properties, DBI dbi) {
        return new EmptyDecisionStrategy(properties, dbi);
    }

    static DecisionStrategy getParameterValuesStrategy(Properties properties, DBI dbi) {
        return new GetParameterValuesDecisionStrategy(properties, dbi);
    }

    static DecisionStrategy setParameterValuesStrategy(Properties properties, DBI dbi) {
        return new SetParameterValuesDecisionStrategy(properties, dbi);
    }
}
