package com.github.freeacs.controllers;

import com.github.freeacs.dbi.DBI;
import com.github.freeacs.dbi.Unit;
import com.github.freeacs.security.AcsUnit;
import com.github.freeacs.tr069.Properties;
import com.github.freeacs.tr069.SessionData;
import com.github.freeacs.tr069.SessionLogging;
import com.github.freeacs.tr069.background.ActiveDeviceDetectionTask;
import com.github.freeacs.tr069.background.MessageListenerTask;
import com.github.freeacs.tr069.background.ScheduledKickTask;
import com.github.freeacs.tr069.base.BaseCache;
import com.github.freeacs.tr069.http.HTTPRequestData;
import com.github.freeacs.tr069.http.HTTPRequestResponseData;
import com.github.freeacs.tr069.http.HTTPResponseData;
import com.github.freeacs.tr069.methods.ProvisioningMethod;
import com.github.freeacs.tr069.methods.ProvisioningStrategy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is the "main-class" of TR069 Provisioning. It receives the HTTP-request from the CPE and
 * returns an HTTP-response. The content of the request/reponse can be both TR-069 request/response.
 */
@Slf4j
@RestController
public class Tr069Controller {

    @Value("${context-path}")
    private String contextPath;

    private final DBI dbi;
    private final Properties properties;

    public Tr069Controller(DBI dbi, Properties properties) {
        this.properties = properties;
        this.dbi = dbi;
    }

    /**
     * This is the entry point for TR-069 Clients - everything starts here!!!
     *
     * <p>A TR-069 session consists of many rounds of HTTP request/responses, however each
     * request/response non-the-less follows a standard pattern:
     *
     * <p>1. Check special HTTP headers for a "early return" (CONTINUE) 2. Check authentication -
     * challenge client if necessary. If not authenticated - return 3. Check concurrent sessions from
     * same unit - if detected: return 4. Extract XML from request - store in sessionData object 5.
     * Process HTTP Request (xml-parsing, find methodname, test-verification) 6. Decide upon next step
     * - may contain logic that processes the request and decide response 7. Produce HTTP Response
     * (xml-creation) 8. Some details about the xml-response like content-type/Empty response 9.
     * Return response to TR-069 client
     *
     * <p>At the end we have error handling, to make sure that no matter what, we do return an EMTPY
     * response to the client - to signal end of conversation/TR-069-session.
     *
     * <p>In the finally loop we check if a TR-069 Session is in-fact completed (one way or the other)
     * and if so, logging is performed. Also, if unit-parameters are queued up for writing, those will
     * be written now (instead of writing some here and some there along the entire TR-069 session).
     *
     * <p>In special cases the server will kick the device to "come back" and continue testing a new
     * test case.
     */
    @PostMapping(value = {"${context-path}", "${context-path}/prov"})
    public ResponseEntity<String> doPost(@RequestBody(required = false) String xmlPayload,
                                         Authentication authentication,
                                         HttpServletRequest req,
                                         HttpServletResponse res) {
        HTTPRequestResponseData reqRes = null;
        try {
            reqRes = new HTTPRequestResponseData(req);
            reqRes.getRequestData().setContextPath(contextPath);
            reqRes.getRequestData().setXml(xmlPayload);
            if (authentication != null && reqRes.getSessionData().getUnit() == null) {
                String username = ((AcsUnit) authentication.getPrincipal()).getUsername();
                SessionData sessionData = reqRes.getSessionData();
                sessionData.setUnitId(username);
                sessionData.setUnit(dbi.getACSUnit().getUnitById(username));
            }

            ProvisioningStrategy.getStrategy(properties, dbi).process(reqRes);

            return ResponseEntity
                    .status("Empty".equals(reqRes.getResponseData().getMethod())
                            ? HttpStatus.NO_CONTENT
                            : HttpStatus.OK)
                    .contentType(StringUtils.isNotEmpty(reqRes.getResponseData().getXml())
                            ? MediaType.TEXT_XML
                            : MediaType.TEXT_HTML)
                    .headers(StringUtils.isNotEmpty(reqRes.getResponseData().getXml())
                            ? getSOAPActionHeader()
                            : null)
                    .body(reqRes.getResponseData().getXml());
        } catch (Throwable t) {
            log.error("An error occurred during processing the request", t);
            if (reqRes != null) {
                reqRes.setThrowable(t);
            }
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("");
        } finally {
            if (reqRes != null && endOfSession(reqRes)) {
                log.debug("End of session is reached, " +
                        "will write queued unit parameters " +
                        "if unit (" + reqRes.getSessionData().getUnit() + ") is not null");
                if (reqRes.getSessionData().getUnit() != null) {
                    writeQueuedUnitParameters(reqRes);
                }
                SessionLogging.log(reqRes);
                BaseCache.removeSessionData(reqRes.getSessionData().getUnitId());
                BaseCache.removeSessionData(reqRes.getSessionData().getId());
                res.setHeader("Connection", "close");
                new SecurityContextLogoutHandler().logout(req, null, null);
            }
        }
    }

    private HttpHeaders getSOAPActionHeader() {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("SOAPAction", "");
        return responseHeaders;
    }

    // every 5 minute
    @Scheduled(cron = "0 0/5 * * * *")
    private void scheduleActiveDeviceDetectionTask() {
        final ActiveDeviceDetectionTask activeDeviceDetectionTask =
                new ActiveDeviceDetectionTask("ActiveDeviceDetection TR069", dbi);
        activeDeviceDetectionTask.setThisLaunchTms(System.currentTimeMillis());
        activeDeviceDetectionTask.run();
    }

    // every 1 second
    @Scheduled(cron = "* * * ? * *")
    private void scheduleKickTask() {
        final ScheduledKickTask scheduledKickTask =
                new ScheduledKickTask("ScheduledKick", dbi);
        scheduledKickTask.setThisLaunchTms(System.currentTimeMillis());
        scheduledKickTask.run();
    }

    // every 5 sec
    @Scheduled(cron = "0/5 * * ? * *")
    private void scheduleMessageListenerTask() {
        final MessageListenerTask messageListenerTask =
                new MessageListenerTask("MessageListener", dbi);
        messageListenerTask.setThisLaunchTms(System.currentTimeMillis());
        messageListenerTask.run();
    }

    private void writeQueuedUnitParameters(HTTPRequestResponseData reqRes) {
        try {
            Unit unit = reqRes.getSessionData().getUnit();
            if (unit != null) {
                dbi.getACSUnit().addOrChangeQueuedUnitParameters(unit);
            }
        } catch (Throwable t) {
            log.error("An error occured when writing queued unit parameters to Fusion. May affect provisioning", t);
        }
    }

    private boolean endOfSession(HTTPRequestResponseData reqRes) {
        try {
            if (reqRes.getThrowable() != null) {
                return true;
            }
            SessionData sessionData = reqRes.getSessionData();
            HTTPRequestData reqData = reqRes.getRequestData();
            HTTPResponseData resData = reqRes.getResponseData();
            if (reqData.getMethod() != null
                    && resData != null
                    && ProvisioningMethod.Empty.name().equals(resData.getMethod())) {
                boolean terminationQuirk = properties.isTerminationQuirk(sessionData);
                return !terminationQuirk || ProvisioningMethod.Empty.name().equals(reqData.getMethod());
            }
            return false;
        } catch (Throwable t) {
            log.warn("An error occured when determining endOfSession. Does not affect provisioning", t);
            return false;
        }
    }

}
