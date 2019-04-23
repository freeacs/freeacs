package com.github.freeacs.tr069.methods.decision;

import com.github.freeacs.base.Log;
import com.github.freeacs.base.db.DBAccess;
import com.github.freeacs.http.HTTPRequestResponseData;
import com.github.freeacs.tr069.Properties;
import com.github.freeacs.tr069.methods.ProvisioningMethod;

@FunctionalInterface
public interface DecisionStrategy {

    void makeDecision(HTTPRequestResponseData reqRes) throws Exception;

    static DecisionStrategy getStrategy(ProvisioningMethod provisioningMethod,
                                        Properties properties,
                                        DBAccess dbAccess) {
        switch (provisioningMethod) {
            case Empty: return emStrategy(properties, dbAccess);
            case Inform: return informStrategy();
            case GetParameterNames: return getParameterNamesStrategy();
            case GetParameterValues: return getParameterValuesStrategy(properties, dbAccess);
            case SetParameterValues: return setParameterValuesStrategy(properties, dbAccess);
            case TransferComplete: return transferCompleteStrategy();
            case AutonomousTransferComplete: return autonomousTransferCompleteStrategy();
            case GetRPCMethods: return getRPCMethodsStrategy();
            default:
                Log.debug(DecisionStrategy.class,"The methodName " + provisioningMethod + " has no decision strategy. Using empty strategy.");
                return emStrategy(properties, dbAccess);
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

    static DecisionStrategy emStrategy(Properties properties, DBAccess dbAccess) {
        return new EmptyDecisionStrategy(properties, dbAccess);
    }

    static DecisionStrategy getParameterValuesStrategy(Properties properties, DBAccess dbAccess) {
        return new GetParameterValuesDecisionStrategy(properties, dbAccess);
    }

    static DecisionStrategy setParameterValuesStrategy(Properties properties, DBAccess dbAccess) {
        return new SetParameterValuesDecisionStrategy(properties, dbAccess);
    }
}
