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
    private static final Pattern VERSION_REGEX =
            Pattern.compile(".*cwmp\\s*=\\s*\"urn:dslforum-org:cwmp-([^\"]+)\".*", Pattern.DOTALL);

    private static final Pattern METHOD_NAME_PATTERN =
            Pattern.compile(":Body.*>\\s*<cwmp:(\\w+)(>|/>)", Pattern.DOTALL);

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
            // 0. Pre-processing
            reqRes.getRequestData().setMethod(extractMethod(reqRes));
            reqRes.getSessionData().setCwmpVersionNumber(extractCwmpVersion(reqRes));
            // 1. process the request
            RequestProcessStrategy.getStrategy(getMethod(reqRes), properties).process(reqRes);
            // 2. decide what to do next
            DecisionStrategy.getStrategy(getMethod(reqRes), properties).makeDecision(reqRes);
            // 3. Create and set response
            Response response = ResponseCreateStrategy.getStrategy(getMethod(reqRes), properties).getResponse(reqRes);
            reqRes.getResponseData().setXml(response.toXml());
        }

        private String extractMethod(HTTPRequestResponseData reqRes) {
            String requestMethodName = extractMethodName(reqRes.getRequestData().getXml());
            if (requestMethodName == null) {
                requestMethodName = Method.Empty.name();
            }
            return requestMethodName;
        }

        private String extractCwmpVersion(HTTPRequestResponseData reqRes) {
            String version = "1";
            if (reqRes.getSessionData().getCwmpVersionNumber() == null) {
                version = extractCwmpVersion(reqRes.getRequestData().getXml());
            }
            return version;
        }

        private Method getMethod(HTTPRequestResponseData reqRes) {
            try {
                return Method.valueOf(reqRes.getRequestData().getMethod());
            } catch (Exception e) {
                return Method.Empty;
            }
        }

        /**
         * Fastest way to extract the method name without actually parsing the XML - the method name is
         * crucial to the next steps in TR-069 processing
         *
         * <p>The TR-069 Method is found after the first "<cwmp:" after ":Body"
         *
         * @param reqStr (TR-069 XML)
         * @return TR-069 methodName
         */
        @SuppressWarnings("Duplicates")
        private String extractMethodName(String reqStr) {
            String methodStr = getMethodStr(reqStr);
            if (methodStr != null && methodStr.endsWith("Response")) {
                methodStr = methodStr.substring(0, methodStr.length() - 8);
            }
            return methodStr;
        }

        private static String getMethodStr(String reqStr) {
            Matcher matcher = METHOD_NAME_PATTERN.matcher(reqStr);
            if (matcher.find()) {
                return matcher.group(1);
            }
            return null;
        }

        private String extractCwmpVersion(String reqStr) {
            Matcher matcher = VERSION_REGEX.matcher(reqStr);
            if (matcher.find()) {
                return matcher.group(1);
            }
            return null;
        }
    }
}
