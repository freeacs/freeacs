package com.github.freeacs.controllers;

import com.github.freeacs.tr069.base.BaseCache;
import com.github.freeacs.tr069.base.Log;
import com.github.freeacs.dbaccess.DBAccess;
import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.ACSUnit;
import com.github.freeacs.dbi.Unit;
import com.github.freeacs.tr069.Properties;
import com.github.freeacs.tr069.SessionData;
import com.github.freeacs.tr069.SessionLogging;
import com.github.freeacs.tr069.methods.ProvisioningMethod;
import com.github.freeacs.tr069.methods.ProvisioningStrategy;
import com.github.freeacs.tr069.background.ActiveDeviceDetectionTask;
import com.github.freeacs.tr069.background.MessageListenerTask;
import com.github.freeacs.tr069.background.ScheduledKickTask;
import com.github.freeacs.tr069.exception.TR069Exception;
import com.github.freeacs.tr069.exception.TR069ExceptionShortMessage;
import com.github.freeacs.http.HTTPRequestData;
import com.github.freeacs.http.HTTPRequestResponseData;
import com.github.freeacs.http.HTTPResponseData;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is the "main-class" of TR069 Provisioning. It receives the HTTP-request from the CPE and
 * returns an HTTP-response. The content of the request/reponse can be both TR-069 request/response.
 *
 * @author morten
 */
@RestController
public class Tr069Controller {

    private final DBAccess dbAccess;
    private final Properties properties;

    public Tr069Controller(DBAccess dbAccess,
                           Properties properties) {
        this.properties = properties;
        this.dbAccess = dbAccess;
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
    @PostMapping(value = {"/${context-path}", "/${context-path}/prov"})
    public void doPost(@RequestBody(required = false) String xmlPayload, HttpServletRequest req, HttpServletResponse res) throws IOException {
        HTTPRequestResponseData reqRes = null;
        try {
            reqRes = new HTTPRequestResponseData(req, res);
            reqRes.getRequestData().setContextPath(properties.getContextPath());
            reqRes.getRequestData().setXml(xmlPayload);

            ProvisioningStrategy.getStrategy(properties, dbAccess).process(reqRes);

            if (reqRes.getResponseData().getXml() != null && !reqRes.getResponseData().getXml().isEmpty()) {
                res.setHeader("SOAPAction", "");
                res.setContentType("text/xml");
            }

            if ("Empty".equals(reqRes.getResponseData().getMethod())) {
                res.setStatus(HttpServletResponse.SC_NO_CONTENT);
            }

            res.getWriter().print(reqRes.getResponseData().getXml());
        } catch (Throwable t) {
            // Make sure we return an EMPTY response to the TR-069 client
            if (t instanceof TR069Exception) {
                TR069Exception tex = (TR069Exception) t;
                Throwable stacktraceThrowable = t;
                if (tex.getCause() != null) {
                    stacktraceThrowable = tex.getCause();
                }
                if (tex.getShortMsg() == TR069ExceptionShortMessage.MISC
                        || tex.getShortMsg() == TR069ExceptionShortMessage.DATABASE) {
                    Log.error(Tr069Controller.class, "An error ocurred: " + t.getMessage(), stacktraceThrowable);
                }
                if (tex.getShortMsg() == TR069ExceptionShortMessage.IOABORTED) {
                    Log.warn(Tr069Controller.class, t.getMessage());
                } else {
                    Log.error(Tr069Controller.class, t.getMessage());
                } // No stacktrace printed to log
            }
            if (reqRes != null) {
                reqRes.setThrowable(t);
            }
            res.setStatus(HttpServletResponse.SC_NO_CONTENT);
            res.getWriter().print("");
        } finally {
            // Run at end of every TR-069 session
            if (reqRes != null && endOfSession(reqRes)) {
                Log.debug(
                        Tr069Controller.class,
                        "End of session is reached, will write queued unit parameters if unit ("
                                + reqRes.getSessionData().getUnit()
                                + ") is not null");
                // Logging of the entire session, both to tr069-event.log and syslog
                if (reqRes.getSessionData().getUnit() != null) {
                    writeQueuedUnitParameters(reqRes);
                }
                SessionLogging.log(reqRes);
                BaseCache.removeSessionData(reqRes.getSessionData().getUnitId());
                BaseCache.removeSessionData(reqRes.getSessionData().getId());
                res.setHeader("Connection", "close");
            }
        }
    }

    // every 5 minute
    @Scheduled(cron = "0 0/5 * * * *")
    private void scheduleActiveDeviceDetectionTask() {
        final ActiveDeviceDetectionTask activeDeviceDetectionTask =
                new ActiveDeviceDetectionTask("ActiveDeviceDetection TR069", dbAccess.getDbi());
        activeDeviceDetectionTask.setThisLaunchTms(System.currentTimeMillis());
        activeDeviceDetectionTask.run();
    }

    // every 1 second
    @Scheduled(cron = "* * * ? * *")
    private void scheduleKickTask() {
        final ScheduledKickTask scheduledKickTask =
                new ScheduledKickTask("ScheduledKick", dbAccess.getDbi());
        scheduledKickTask.setThisLaunchTms(System.currentTimeMillis());
        scheduledKickTask.run();
    }

    // every 5 sec
    @Scheduled(cron = "0/5 * * ? * *")
    private void scheduleMessageListenerTask() {
        final MessageListenerTask messageListenerTask =
                new MessageListenerTask("MessageListener", dbAccess.getDbi());
        messageListenerTask.setThisLaunchTms(System.currentTimeMillis());
        messageListenerTask.run();
    }

    private void writeQueuedUnitParameters(HTTPRequestResponseData reqRes) {
        try {
            Unit unit = reqRes.getSessionData().getUnit();
            if (unit != null) {
                ACS acs = dbAccess.getDbi().getAcs();
                ACSUnit acsUnit = new ACSUnit(acs.getDataSource(), acs, acs.getSyslog());
                acsUnit.addOrChangeQueuedUnitParameters(unit);
            }
        } catch (Throwable t) {
            Log.error(
                    Tr069Controller.class,
                    "An error occured when writing queued unit parameters to Fusion. May affect provisioning",
                    t);
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
            Log.warn(
                    Tr069Controller.class,
                    "An error occured when determining endOfSession. Does not affect provisioning",
                    t);
            return false;
        }
    }

}
