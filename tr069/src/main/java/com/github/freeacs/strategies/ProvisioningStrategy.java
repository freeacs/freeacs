package com.github.freeacs.strategies;

import com.github.freeacs.http.HTTPRequestResponseData;
import com.github.freeacs.strategies.decision.DecisionStrategy;
import com.github.freeacs.strategies.request.RequestProcessStrategy;
import com.github.freeacs.strategies.response.ResponseCreateStrategy;
import com.github.freeacs.tr069.Properties;
import com.github.freeacs.tr069.methods.Method;
import com.github.freeacs.tr069.xml.Response;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class ProvisioningStrategy {

    public abstract void process(HTTPRequestResponseData reqRes) throws Exception;

    public static ProvisioningStrategy getStrategy(Properties properties) {
        return new NormalProvisioningStrategy(properties);
    }

    private static class NormalProvisioningStrategy extends ProvisioningStrategy {
        private static final Pattern VERSION_REGEX =
                Pattern.compile(".*cwmp\\s*=\\s*\"urn:dslforum-org:cwmp-([^\"]+)\".*", Pattern.DOTALL);
        private static final Pattern METHOD_NAME_PATTERN =
                Pattern.compile(":Body.*>\\s*<cwmp:(\\w+)(>|/>)", Pattern.DOTALL);

        private final Properties properties;

        private NormalProvisioningStrategy(Properties properties) {
            this.properties = properties;
        }

        @Override
        public void process(HTTPRequestResponseData reqRes) throws Exception {
            // 0. Pre-processing
            reqRes.getRequestData().setMethod(extractMethod(reqRes));
            reqRes.getSessionData().setCwmpVersionNumber(extractCwmpVersion(reqRes));
            // 1. process the request
            Method requestMethod = getRequestMethod(reqRes);
            RequestProcessStrategy.getStrategy(requestMethod, properties).process(reqRes);
            // 2. decide what to do next
            DecisionStrategy.getStrategy(requestMethod, properties).makeDecision(reqRes);
            // 3. Create and set response
            Method responseMethod = getResponseMethod(reqRes);
            Response response = ResponseCreateStrategy.getStrategy(responseMethod, properties).getResponse(reqRes);
            reqRes.getResponseData().setXml(response.toXml());
        }

        /**
         * Extract request method name.
         */
        private String extractMethod(HTTPRequestResponseData reqRes) {
            String requestMethodName = null;
            Matcher matcher = METHOD_NAME_PATTERN.matcher(reqRes.getRequestData().getXml());
            if (matcher.find()) {
                requestMethodName = matcher.group(1);
            }
            if (requestMethodName != null && requestMethodName.endsWith("Response")) {
                requestMethodName = requestMethodName.substring(0, requestMethodName.length() - 8);
            }
            if (requestMethodName == null) {
                requestMethodName = Method.Empty.name();
            }
            return requestMethodName;
        }

        /**
         * Extract cwmp version or default to "1-2"
         */
        private String extractCwmpVersion(HTTPRequestResponseData reqRes) {
            String version = "1-2";
            if (reqRes.getSessionData().getCwmpVersionNumber() == null) {
                Matcher matcher = VERSION_REGEX.matcher(reqRes.getRequestData().getXml());
                if (matcher.find()) {
                    version = matcher.group(1);
                }
            }
            return version;
        }

        /**
         * The request method will never change, so this method is just here to wrap the operation
         * of converting null method to Empty
         */
        private Method getRequestMethod(HTTPRequestResponseData reqRes) {
            try {
                return Method.valueOf(reqRes.getRequestData().getMethod());
            } catch (Exception e) {
                return Method.Empty;
            }
        }

        /**
         * The response method will be mutated by the DecisionStrategy,
         * so this method can and will return something else than the method getRequestMethod.
         */
        private Method getResponseMethod(HTTPRequestResponseData reqRes) {
            try {
                return Method.valueOf(reqRes.getResponseData().getMethod());
            } catch (Exception e) {
                return Method.Empty;
            }
        }
    }
}
