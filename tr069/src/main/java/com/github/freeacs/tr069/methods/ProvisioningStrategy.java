package com.github.freeacs.tr069.methods;

import com.github.freeacs.cache.AcsCache;
import com.github.freeacs.dbi.DBI;
import com.github.freeacs.dbi.Syslog;
import com.github.freeacs.tr069.Properties;
import com.github.freeacs.tr069.base.Log;
import com.github.freeacs.tr069.http.HTTPRequestResponseData;
import com.github.freeacs.tr069.methods.decision.DecisionStrategy;
import com.github.freeacs.tr069.methods.request.RequestProcessStrategy;
import com.github.freeacs.tr069.methods.response.ResponseCreateStrategy;
import com.github.freeacs.tr069.xml.Parser;
import com.github.freeacs.tr069.xml.Response;
import com.github.freeacs.tr069.xml.XMLFormatterUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public abstract class ProvisioningStrategy {

    public abstract void process(HTTPRequestResponseData reqRes) throws Exception;

    public static ProvisioningStrategy getStrategy(Properties properties, DBI dbi, Syslog syslog, AcsCache acsCache) {
        return new NormalProvisioningStrategy(properties, dbi, acsCache, syslog);
    }

    private static class NormalProvisioningStrategy extends ProvisioningStrategy {

        private final Properties properties;
        private final DBI dbi;
        private final Syslog syslog;
        private final AcsCache acsCache;

        private NormalProvisioningStrategy(Properties properties, DBI dbi, AcsCache acsCache, Syslog syslog) {
            this.properties = properties;
            this.dbi = dbi;
            this.syslog = syslog;
            this.acsCache = acsCache;
        }

        @Override
        public void process(HTTPRequestResponseData reqRes) throws Exception {
            // 0. Pre-processing
            Parser xml = reqRes.getRequestData().getParser();
            reqRes.getRequestData().setMethod(xml.getCwmpMethod().name());
            reqRes.getSessionData().setCwmpVersionNumber(xml.getCwmpVersion());

            // 1. process the request
            logWillProcessRequest(reqRes);
            RequestProcessStrategy.getStrategy(xml.getCwmpMethod(), properties, dbi, acsCache, syslog).process(reqRes);
            if (Log.isConversationLogEnabled()) {
                logConversationRequest(reqRes);
            }

            // 2. decide what to do next
            DecisionStrategy.getStrategy(xml.getCwmpMethod(), properties, dbi, acsCache).makeDecision(reqRes);

            // 3. Create and set response
            ProvisioningMethod responseMethod = getResponseMethod(reqRes);
            Response response = ResponseCreateStrategy.getStrategy(responseMethod, properties).getResponse(reqRes);
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
            String xml = reqRes.getRequestData().getParser().getRawXMLForDebugging();
            if (xml != null && properties.isPrettyPrintQuirk(reqRes.getSessionData())) {
                xml = XMLFormatterUtils.prettyPrintXmlString(xml);
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
