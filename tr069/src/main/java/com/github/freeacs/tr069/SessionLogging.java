package com.github.freeacs.tr069;

import com.github.freeacs.tr069.base.Log;
import com.github.freeacs.dbi.Users;
import com.github.freeacs.dbi.util.ProvisioningMessage;
import com.github.freeacs.dbi.util.ProvisioningMessage.ErrorResponsibility;
import com.github.freeacs.dbi.util.ProvisioningMessage.ProvOutput;
import com.github.freeacs.dbi.util.ProvisioningMessage.ProvStatus;
import com.github.freeacs.dbi.util.ProvisioningMode;
import com.github.freeacs.dbi.util.SyslogClient;
import com.github.freeacs.tr069.http.HTTPRequestData;
import com.github.freeacs.tr069.http.HTTPRequestResponseData;
import com.github.freeacs.tr069.http.HTTPResponseData;
import com.github.freeacs.tr069.methods.ProvisioningMethod;
import com.github.freeacs.tr069.xml.ParameterValueStruct;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import javax.servlet.http.HttpServletRequest;

/**
 * Responsible for logging to the tr069-event log.
 *
 * @author Morten
 * @author Jarl
 */
@Slf4j
public class SessionLogging {
  public static void log(HTTPRequestResponseData reqRes) {
    try {
      SessionData sessionData = reqRes.getSessionData();
      // The old logging to eventlog
      String methodsUsed = abbreviate(sessionData.getReqResList());
      long diff = System.currentTimeMillis() - sessionData.getStartupTmsForSession();
      String eventMsg = makeEventMsg(reqRes, diff, methodsUsed);
      Log.event(eventMsg);

      // The new logging to syslog
      ProvisioningMessage pm = sessionData.getProvisioningMessage();

      if (pm.getProvOutput() == null) {
        pm.setProvOutput(ProvOutput.EMPTY);
      }
      if (reqRes.getThrowable() != null) {
        pm.setProvStatus(ProvStatus.ERROR);
        pm.setErrorResponsibility(ErrorResponsibility.SERVER);
        pm.setErrorMessage(reqRes.getThrowable().getMessage());
      } else if (pm.getProvStatus() == null) {
        pm.setProvStatus(ProvStatus.OK);
      }
      if (pm.getProvMode() == null) {
        if (sessionData.getUnit() != null) {
          pm.setProvMode(sessionData.getUnit().getProvisioningMode());
        } else {
          pm.setProvMode(ProvisioningMode.REGULAR);
        }
      }
      pm.setSessionLength((int) diff);
      pm.setIpAddress(reqRes.getRawRequest().getRemoteHost());
      // We're not sending the facility-version, since the message is used in a report - where
      // the version of the TR-069 is much more interesting (will be added automatically be the
      // syslog server)
      SyslogClient.send(pm.syslogMsg(16, null, Users.USER_ADMIN));
    } catch (Throwable t) {
      log.warn("An error ocurred when logging at endOfSession. Does not affect provisioning", t);
    }
  }

  private static String abbreviate(List<HTTPRequestResponseData> reqResList) {
    StringBuilder methodsUsed = new StringBuilder();
    for (int i = 0; i < reqResList.size(); i++) {
      HTTPRequestResponseData reqRes = reqResList.get(i);
      HTTPRequestData reqData = reqRes.getRequestData();
      HTTPResponseData resData = reqRes.getResponseData();
      String reqMethod = reqData.getMethod();
      if (reqMethod == null) {
        continue;
      }
      String resMethod = resData.getMethod();
      String reqShortname = ProvisioningMethod.valueOf(reqMethod).getAbbreviation();
      if (i > 0) {
        HTTPRequestResponseData prevReqRes = reqResList.get(i - 1);
        String prevResMethod = prevReqRes.getResponseData().getMethod();
        if (prevResMethod != null
            && !ProvisioningMethod.Empty.name().equals(reqMethod)
            && prevResMethod.equals(reqMethod)) {
          reqShortname += "r";
        }
      }
      if (reqData.getFault() != null) {
        reqShortname += "(FC:" + reqData.getFault().getFaultCode() + ")";
      }
      String resShortname = resMethod != null
              ? ProvisioningMethod.valueOf(resMethod).getAbbreviation()
              : ProvisioningMethod.Empty.getAbbreviation();
      if (!ProvisioningMethod.Empty.name().equals(reqMethod) && reqMethod.equals(resMethod)) {
        resShortname += "r";
      }
      if (ProvisioningMethod.SetParameterValues.name().equals(reqMethod)
          && !reqRes.getSessionData().isProvisioningAllowed()) {
        reqShortname += "lim";
      }
      if (ProvisioningMethod.Inform.name().equals(reqMethod)) {
        reqShortname += "(" + reqRes.getSessionData().getEventCodes() + ")";
      }
      methodsUsed.append("[").append(reqShortname);
      if (reqRes.getThrowable() != null) {
        methodsUsed.append("-(F)").append(resShortname).append("] ");
      } else {
        methodsUsed.append("-").append(resShortname).append("] ");
      }
    }

    return methodsUsed.toString();
  }

  private static String makeEventMsg(HTTPRequestResponseData reqRes, long diff, String methodsUsed) {
    List<HTTPRequestResponseData> reqResList = reqRes.getSessionData().getReqResList();
    HttpServletRequest req = reqRes.getRawRequest();
    SessionData sessionData = reqRes.getSessionData();
    String eventMsg =
        String.format("%1$-16s %2$6dms %3$-60s", req.getRemoteHost(), diff, methodsUsed);
    //		String eventMsg = "[" + req.getRemoteHost() + "] [" + diff + " ms] [" + methodsUsed + "]";
    String job = "      ";
    if (sessionData.getJob() != null) {
      job = String.format("%-6s", sessionData.getJob().getId());
    }
    eventMsg += job;
    StringBuilder parameterList = new StringBuilder();
    int paramsToCPE = 0;
    for (HTTPRequestResponseData reqResItem : reqResList) {
      String resMethod = reqResItem.getResponseData().getMethod();
      if (ProvisioningMethod.SetParameterValues.name().equals(resMethod)) {
        paramsToCPE = sessionData.getToCPE().getParameterValueList().size();
        for (ParameterValueStruct pvs : sessionData.getToCPE().getParameterValueList()) {
          parameterList.append(pvs.getName()).append("=").append(pvs.getValue()).append(", ");
        }
      }
    }
    if (paramsToCPE > 0) {
      eventMsg += " [" + paramsToCPE + " params set to CPE: ";
      eventMsg += parameterList.substring(0, parameterList.length() - 2) + "]";
    }
    return eventMsg;
  }
}
