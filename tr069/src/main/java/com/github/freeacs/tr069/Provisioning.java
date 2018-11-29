package com.github.freeacs.tr069;

import com.github.freeacs.base.BaseCache;
import com.github.freeacs.base.Log;
import com.github.freeacs.base.db.DBAccess;
import com.github.freeacs.base.http.Authenticator;
import com.github.freeacs.base.http.ThreadCounter;
import com.github.freeacs.common.scheduler.ExecutorWrapper;
import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.ACSUnit;
import com.github.freeacs.dbi.DBI;
import com.github.freeacs.dbi.ScriptExecutions;
import com.github.freeacs.dbi.Unit;
import com.github.freeacs.tr069.background.ActiveDeviceDetectionTask;
import com.github.freeacs.tr069.background.MessageListenerTask;
import com.github.freeacs.tr069.background.ScheduledKickTask;
import com.github.freeacs.tr069.background.StabilityTask;
import com.github.freeacs.tr069.exception.TR069Exception;
import com.github.freeacs.tr069.exception.TR069ExceptionShortMessage;
import com.github.freeacs.http.AbstractHttpDataWrapper;
import com.github.freeacs.http.HTTPRequestData;
import com.github.freeacs.http.HTTPRequestResponseData;
import com.github.freeacs.http.HTTPResponseData;
import com.github.freeacs.tr069.methods.DecisionMaker;
import com.github.freeacs.tr069.methods.HTTPRequestProcessor;
import com.github.freeacs.tr069.methods.HTTPResponseCreator;
import com.github.freeacs.tr069.methods.TR069Method;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is the "main-class" of TR069 Provisioning. It receives the HTTP-request from the CPE and
 * returns an HTTP-response. The content of the request/reponse can be both TR-069 request/response.
 *
 * @author morten
 */
public class Provisioning extends AbstractHttpDataWrapper {
  private static ScriptExecutions executions;

  private final TR069Method tr069Method;

  private final ExecutorWrapper executorWrapper;

  public Provisioning(
      DBAccess dbAccess,
      TR069Method tr069Method,
      Properties properties,
      ExecutorWrapper executorWrapper) {
    super(dbAccess, properties);
    this.tr069Method = tr069Method;
    this.executorWrapper = executorWrapper;
  }

  public void init() {
    Log.notice(Provisioning.class, "Server starts...");
    try {
      DBI dbi = dbAccess.getDBI();
      scheduleStabilityTask();
      scheduleMessageListenerTask(dbi);
      scheduleKickTask(dbi);
      scheduleActiveDeviceDetectionTask(dbi);
    } catch (Throwable t) {
      Log.fatal(Provisioning.class, "Couldn't start BackgroundProcesses correctly ", t);
    }
    try {
      executions = new ScriptExecutions(dbAccess.getMainDataSource());
    } catch (Throwable t) {
      Log.fatal(
          Provisioning.class,
          "Couldn't initialize ScriptExecutions - not possible to run SHELL-jobs",
          t);
    }
  }

  private void scheduleActiveDeviceDetectionTask(final DBI dbi) {
    // every 5 minute
    final ActiveDeviceDetectionTask activeDeviceDetectionTask =
        new ActiveDeviceDetectionTask("ActiveDeviceDetection TR069", dbi);
    executorWrapper.scheduleCron(
        "0 0/5 * * * ?",
        (tms) ->
            () -> {
              activeDeviceDetectionTask.setThisLaunchTms(tms);
              activeDeviceDetectionTask.run();
            });
  }

  private void scheduleKickTask(final DBI dbi) {
    // every 1 second
    final ScheduledKickTask scheduledKickTask = new ScheduledKickTask("ScheduledKick", dbi);
    executorWrapper.scheduleCron(
        "* * * ? * * *",
        (tms) ->
            () -> {
              scheduledKickTask.setThisLaunchTms(tms);
              scheduledKickTask.run();
            });
  }

  private void scheduleMessageListenerTask(final DBI dbi) {
    // every 5 sec
    final MessageListenerTask messageListenerTask = new MessageListenerTask("MessageListener", dbi);
    executorWrapper.scheduleCron(
        "0/5 * * ? * * *",
        (tms) ->
            () -> {
              messageListenerTask.setThisLaunchTms(tms);
              messageListenerTask.run();
            });
  }

  private void scheduleStabilityTask() {
    // every 10 sec
    final StabilityTask stabilityTask = new StabilityTask("StabilityLogger");
    executorWrapper.scheduleCron(
        "0/10 * * ? * * *",
        (tms) ->
            () -> {
              stabilityTask.setThisLaunchTms(tms);
              stabilityTask.run();
            });
  }

  private static void extractRequest(HTTPRequestResponseData reqRes) throws TR069Exception {
    try {
      InputStreamReader isr = new InputStreamReader(reqRes.getRawRequest().getInputStream());
      BufferedReader br = new BufferedReader(isr);
      StringBuilder requestSB = new StringBuilder(1000);
      do {
        String line = br.readLine();
        if (line == null) {
          break;
        }
        requestSB.append(line).append("\n");
      } while (true);
      reqRes.getRequestData().setXml(requestSB.toString());
      System.currentTimeMillis();
    } catch (IOException e) {
      throw new TR069Exception(
          "TR-069 client aborted (not possible to read more input)",
          TR069ExceptionShortMessage.IOABORTED,
          e);
    }
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
  public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
    HTTPRequestResponseData reqRes = null;
    try {
      // Create the main object which contains all objects concerning the entire
      // session. This object also contains the SessionData object
      reqRes = getHttpReqResDate(req, res);
      // 2. Authenticate the client (first issue challenge, then authenticate)
      if (!Authenticator.authenticate(reqRes, properties)
          || (reqRes.getSessionData() != null
              && !ThreadCounter.isRequestAllowed(reqRes.getSessionData()))) {
        return;
      }
      // 4. Read the request from the client - store in reqRes object
      extractRequest(reqRes);
      // 5.Process request (parsing xml/data)
      HTTPRequestProcessor.processRequest(reqRes, tr069Method.getRequestMap(), properties);
      // 6. Decide next step in TR-069 session (sometimes trivial, sometimes complex)
      DecisionMaker.process(reqRes, tr069Method.getRequestMap());
      // 7. Create TR-069 response
      HTTPResponseCreator.createResponse(reqRes, tr069Method.getResponseMap());
      // 8. Set correct headers in response
      if (reqRes.getResponseData().getXml() != null && !reqRes.getResponseData().getXml().isEmpty()) {
        res.setHeader("SOAPAction", "");
        res.setContentType("text/xml");
      }
      // 8. No need to send Content-length as it will only be informational for 204 HTTP messages
      if ("Empty".equals(reqRes.getResponseData().getMethod())) {
        res.setStatus(HttpServletResponse.SC_NO_CONTENT);
      }
      // 9. Print response to output
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
          Log.error(Provisioning.class, "An error ocurred: " + t.getMessage(), stacktraceThrowable);
        }
        if (tex.getShortMsg() == TR069ExceptionShortMessage.IOABORTED) {
          Log.warn(Provisioning.class, t.getMessage());
        } else {
          Log.error(Provisioning.class, t.getMessage());
        } // No stacktrace printed to log
      }
      if (reqRes != null) {
        reqRes.setThrowable(t);
        Log.error(Provisioning.class, "Something went wrong", t);
      }
      res.setStatus(HttpServletResponse.SC_NO_CONTENT);
      res.getWriter().print("");
    } finally {
      // Run at end of every TR-069 session
      if (reqRes != null && endOfSession(reqRes)) {
        Log.debug(
            Provisioning.class,
            "End of session is reached, will write queued unit parameters if unit ("
                + reqRes.getSessionData().getUnit()
                + ") is not null");
        // Logging of the entire session, both to tr069-event.log and syslog
        if (reqRes.getSessionData().getUnit() != null) {
          //					reqRes.getSessionData().getUnit().toWriteQueue(SystemParameters.PROVISIONING_STATE,
          // ProvisioningState.READY.toString());
          writeQueuedUnitParameters(reqRes);
        }
        SessionLogging.log(reqRes, tr069Method.getAbbrevMap());
        BaseCache.removeSessionData(reqRes.getSessionData().getUnitId());
        BaseCache.removeSessionData(reqRes.getSessionData().getId());
        res.setHeader("Connection", "close");
      }
    }
    if (reqRes != null && reqRes.getSessionData() != null) {
      ThreadCounter.responseDelivered(reqRes.getSessionData());
    }
  }

  private void writeQueuedUnitParameters(HTTPRequestResponseData reqRes) {
    try {
      Unit unit = reqRes.getSessionData().getUnit();
      if (unit != null) {
        ACS acs = reqRes.getSessionData().getDbAccessSession().getAcs();
        ACSUnit acsUnit = DBAccess.getXAPSUnit(acs);
        acsUnit.addOrChangeQueuedUnitParameters(unit);
      }
    } catch (Throwable t) {
      Log.error(
          Provisioning.class,
          "An error occured when writing queued unit parameters to Fusion. May affect provisioning",
          t);
    }
  }

  private boolean endOfSession(HTTPRequestResponseData reqRes) {
    try {
      SessionData sessionData = reqRes.getSessionData();
      HTTPRequestData reqData = reqRes.getRequestData();
      HTTPResponseData resData = reqRes.getResponseData();
      if (reqRes.getThrowable() != null) {
        return true;
      }
      if (reqData.getMethod() != null
          && resData != null
          && TR069Method.EMPTY.equals(resData.getMethod())) {
        boolean terminationQuirk = properties.isTerminationQuirk(sessionData);
        return !terminationQuirk || TR069Method.EMPTY.equals(reqData.getMethod());
      }
      return false;
    } catch (Throwable t) {
      Log.warn(
          Provisioning.class,
          "An error occured when determining endOfSession. Does not affect provisioning",
          t);
      return false;
    }
  }

  public static ScriptExecutions getExecutions() {
    return executions;
  }
}
