package com.github.freeacs.tr069.methods;

import com.github.freeacs.dbi.DBI;
import com.github.freeacs.security.AcsUnit;
import com.github.freeacs.tr069.base.Log;
import com.github.freeacs.tr069.http.HTTPRequestResponseData;
import com.github.freeacs.tr069.methods.decision.DecisionStrategy;
import com.github.freeacs.tr069.methods.request.RequestProcessStrategy;
import com.github.freeacs.tr069.methods.response.ResponseCreateStrategy;
import com.github.freeacs.tr069.Properties;
import com.github.freeacs.tr069.xml.PrettyPrinter;
import com.github.freeacs.tr069.xml.Response;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public abstract class ProvisioningStrategy {

    public abstract void process(HTTPRequestResponseData reqRes) throws Exception;

    public static ProvisioningStrategy getStrategy(Properties properties, DBI dbi, AcsUnit acsUnit) {
        return new NormalProvisioningStrategy(properties, dbi, acsUnit);
    }

    private static class NormalProvisioningStrategy extends ProvisioningStrategy {
        private static final Pattern VERSION_REGEX =
                Pattern.compile(".*cwmp\\s*=\\s*\"urn:dslforum-org:cwmp-([^\"]+)\".*", Pattern.DOTALL);
        private static final Pattern METHOD_NAME_PATTERN =
                Pattern.compile(":Body.*>\\s*<cwmp:(\\w+)(>|/>)", Pattern.DOTALL);

        private final Properties properties;
        private final DBI dbi;
        private final AcsUnit acsUnit;

        private NormalProvisioningStrategy(Properties properties,
                                           DBI dbi,
                                           AcsUnit acsUnit) {
            this.properties = properties;
            this.dbi = dbi;
            this.acsUnit = acsUnit;
        }

        @Override
        public void process(HTTPRequestResponseData reqRes) throws Exception {
            // 0. Pre-processing
            reqRes.getRequestData().setXml(PrettyPrinter.filterInvalidCharacters(reqRes.getRequestData().getXml()));
            reqRes.getRequestData().setMethod(extractMethod(reqRes));
            reqRes.getSessionData().setCwmpVersionNumber(extractCwmpVersion(reqRes));

            // 1. process the request
            logWillProcessRequest(reqRes);
            ProvisioningMethod requestProvisioningMethod = getRequestMethod(reqRes);
            RequestProcessStrategy.getStrategy(requestProvisioningMethod, properties, dbi, acsUnit).process(reqRes);
            if (Log.isConversationLogEnabled()) {
                logConversationRequest(reqRes);
            }

            // 2. decide what to do next
            DecisionStrategy.getStrategy(requestProvisioningMethod, properties, dbi).makeDecision(reqRes);

            // 3. Create and set response
            ProvisioningMethod responseProvisioningMethod = getResponseMethod(reqRes);
            Response response = ResponseCreateStrategy.getStrategy(responseProvisioningMethod, properties).getResponse(reqRes);
            String responseStr = response.toXml();
            if (Log.isConversationLogEnabled()) {
                logConversationResponse(reqRes, responseStr);
            }
            reqRes.getResponseData().setXml(responseStr);
        }

        /**
         * Log that we have got a request.
         */
        private void logWillProcessRequest(HTTPRequestResponseData reqRes) {
            String method = reqRes.getRequestData().getMethod();
            log.debug("Will process method " + method + " (incoming request/response from CPE)");
        }

        /**
         * Log the xml payload. Pretty print it if pretty print quirk is enabled.
         */
        private void logConversationRequest(HTTPRequestResponseData reqRes) {
            String unitId = Optional.ofNullable(reqRes.getSessionData().getUnitId()).orElse("Unknown");
            String xml = reqRes.getRequestData().getXml();
            if (properties.isPrettyPrintQuirk(reqRes.getSessionData())) {
                xml = PrettyPrinter.prettyPrintXmlString(reqRes.getRequestData().getXml());
            }
            Log.conversation(reqRes.getSessionData(), "============== FROM CPE ( " + unitId + " ) TO ACS ===============\n" + xml);
        }

        /**
         * Log the xml response.
         */
        private void logConversationResponse(HTTPRequestResponseData reqRes, String responseStr) {
            String unitId = Optional.ofNullable(reqRes.getSessionData().getUnitId()).orElse("Unknown");
            Log.conversation(reqRes.getSessionData(), "=============== FROM ACS TO ( " + unitId + " ) ============\n" + responseStr + "\n");
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
                requestMethodName = ProvisioningMethod.Empty.name();
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
        private ProvisioningMethod getRequestMethod(HTTPRequestResponseData reqRes) {
            try {
                return ProvisioningMethod.valueOf(reqRes.getRequestData().getMethod());
            } catch (Exception e) {
                return ProvisioningMethod.Empty;
            }
        }

        /**
         * The response method will be mutated by the DecisionStrategy,
         * so this method can and will return something else than the method getRequestMethod.
         */
        private ProvisioningMethod getResponseMethod(HTTPRequestResponseData reqRes) {
            try {
                return ProvisioningMethod.valueOf(reqRes.getResponseData().getMethod());
            } catch (Exception e) {
                return ProvisioningMethod.Empty;
            }
        }
    }
}
