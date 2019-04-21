package com.github.freeacs.strategies;

import com.github.freeacs.http.HTTPRequestResponseData;
import com.github.freeacs.strategies.decision.DecisionStrategy;
import com.github.freeacs.strategies.request.RequestProcessStrategy;
import com.github.freeacs.strategies.response.ResponseCreateStrategy;
import com.github.freeacs.tr069.Properties;
import com.github.freeacs.tr069.methods.Method;
import com.github.freeacs.tr069.xml.Response;

public abstract class ProvisioningStrategy {

    public abstract void process(HTTPRequestResponseData reqRes) throws Exception;

    public static ProvisioningStrategy getStrategy(Properties properties) {
        return new NormalProvisioningStrategy(properties);
    }

    private static class NormalProvisioningStrategy extends ProvisioningStrategy {

        private final Properties properties;

        private NormalProvisioningStrategy(Properties properties) {
            this.properties = properties;
        }

        @Override
        public void process(HTTPRequestResponseData reqRes) throws Exception {
            // 1. process the request
            RequestProcessStrategy.getStrategy(getMethod(reqRes), properties).process(reqRes);
            // 2. decide what to do next
            DecisionStrategy.getStrategy(getMethod(reqRes), properties).makeDecision(reqRes);
            // 3. Create and set response
            Response response = ResponseCreateStrategy.getStrategy(getMethod(reqRes), properties).getResponse(reqRes);
            reqRes.getResponseData().setXml(response.toXml());
        }

        private Method getMethod(HTTPRequestResponseData reqRes) {
            return Method.valueOf(reqRes.getRequestData().getMethod());
        }
    }
}
