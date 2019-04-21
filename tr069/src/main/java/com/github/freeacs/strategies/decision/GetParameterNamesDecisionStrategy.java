package com.github.freeacs.strategies.decision;

import com.github.freeacs.http.HTTPRequestResponseData;
import com.github.freeacs.tr069.methods.Method;

public class GetParameterNamesDecisionStrategy implements DecisionStrategy {
    @Override
    public void makeDecision(HTTPRequestResponseData reqRes) {
        reqRes.getResponseData().setMethod(Method.GetParameterValues.name());
    }
}
