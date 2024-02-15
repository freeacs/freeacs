package com.github.freeacs.tr069.methods.decision;

import com.github.freeacs.cache.AcsCache;
import com.github.freeacs.dbi.DBI;
import com.github.freeacs.tr069.Properties;
import com.github.freeacs.tr069.http.HTTPRequestResponseData;
import com.github.freeacs.tr069.methods.ProvisioningMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FunctionalInterface
public interface DecisionStrategy {
    Logger log = LoggerFactory.getLogger(DecisionStrategy.class);

    void makeDecision(HTTPRequestResponseData reqRes) throws Exception;

    static DecisionStrategy getStrategy(ProvisioningMethod provisioningMethod,
                                        Properties properties,
                                        DBI dbi,
                                        AcsCache acsCache) {
        return switch (provisioningMethod) {
            case Empty -> emStrategy(properties, dbi, acsCache);
            case Inform -> informStrategy();
            case GetParameterNames -> getParameterNamesStrategy();
            case GetParameterValues -> getParameterValuesStrategy(properties, dbi, acsCache);
            case SetParameterValues -> setParameterValuesStrategy(properties, dbi, acsCache);
            case TransferComplete -> transferCompleteStrategy();
            case AutonomousTransferComplete -> autonomousTransferCompleteStrategy();
            case GetRPCMethods -> getRPCMethodsStrategy();
            default -> {
                log.debug("The methodName " + provisioningMethod + " has no decision strategy. Using empty strategy.");
                yield emStrategy(properties, dbi, acsCache);
            }
        };
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

    static DecisionStrategy emStrategy(Properties properties, DBI dbi, AcsCache acsCache) {
        return new EmptyDecisionStrategy(properties, dbi, acsCache);
    }

    static DecisionStrategy getParameterValuesStrategy(Properties properties, DBI dbi, AcsCache acsCache) {
        return new GetParameterValuesDecisionStrategy(properties, dbi, acsCache);
    }

    static DecisionStrategy setParameterValuesStrategy(Properties properties, DBI dbi, AcsCache acsCache) {
        return new SetParameterValuesDecisionStrategy(properties, dbi, acsCache);
    }
}
