package com.github.freeacs.tr069;

import com.github.freeacs.base.Log;
import com.github.freeacs.dbi.Users;
import com.github.freeacs.dbi.util.ProvisioningMessage;
import com.github.freeacs.dbi.util.ProvisioningMessage.ErrorResponsibility;
import com.github.freeacs.dbi.util.ProvisioningMessage.ProvOutput;
import com.github.freeacs.dbi.util.ProvisioningMessage.ProvStatus;
import com.github.freeacs.dbi.util.ProvisioningMode;
import com.github.freeacs.dbi.util.SyslogClient;
import com.github.freeacs.tr069.methods.TR069Method;
import com.github.freeacs.tr069.xml.ParameterValueStruct;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * Responsible for logging to the tr069-event log
 *
 * @author Morten
 * @author Jarl
 */
public class SessionLogging {
    public static void log(HTTPReqResData reqRes, Map<String, String> abbrevMap) {
        try {
            SessionData sessionData = reqRes.getSessionData();
            // The old logging to eventlog
            String methodsUsed = abbreviate(sessionData.getReqResList(), abbrevMap);
            long diff = System.currentTimeMillis() - sessionData.getStartupTmsForSession();
            String eventMsg = makeEventMsg(reqRes, diff, methodsUsed);
            Log.event(eventMsg);

            // The new logging to syslog
            ProvisioningMessage pm = sessionData.getProvisioningMessage();

            if (pm.getProvOutput() == null)
                pm.setProvOutput(ProvOutput.EMPTY);
            if (reqRes.getThrowable() != null) {
                pm.setProvStatus(ProvStatus.ERROR);
                pm.setErrorResponsibility(ErrorResponsibility.SERVER);
                pm.setErrorMessage(reqRes.getThrowable().getMessage());
            } else if (pm.getProvStatus() == null)
                pm.setProvStatus(ProvStatus.OK);
            if (pm.getProvMode() == null) {
                if (sessionData.getUnit() != null)
                    pm.setProvMode(sessionData.getUnit().getProvisioningMode());
                else
                    pm.setProvMode(ProvisioningMode.REGULAR);
            }
            pm.setSessionLength((int) diff);
            pm.setIpAddress(reqRes.getReq().getRemoteHost());
            // We're not sending the facility-version, since the message is used in a report - where
            // the version of the TR-069 is much more interesting (will be added automatically be the syslog server)
            SyslogClient.send(pm.syslogMsg(16, null, Users.USER_ADMIN));
        } catch (Throwable t) {
            Log.warn(Provisioning.class, "An error ocurred when logging at endOfSession. Does not affect provisioning", t);
        }
    }

    private static String abbreviate(List<HTTPReqResData> reqResList, Map<String, String> abbrevMap) {
        String methodsUsed = "";
        for (int i = 0; i < reqResList.size(); i++) {
            HTTPReqResData reqRes = reqResList.get(i);
            HTTPReqData reqData = reqRes.getRequest();
            HTTPResData resData = reqRes.getResponse();
            String reqMethod = reqData.getMethod();
            if (reqMethod == null)
                continue;
            String resMethod = resData.getMethod();
            String reqShortname = abbrevMap.get(reqMethod);
            if (i > 0) {
                HTTPReqResData prevReqRes = reqResList.get(i - 1);
                String prevResMethod = prevReqRes.getResponse().getMethod();
                if (prevResMethod != null) {
                    if (!reqMethod.equals(TR069Method.EMPTY) && prevResMethod.equals(reqMethod))
                        reqShortname += "r";
                }
            }
            if (reqData.getFault() != null)
                reqShortname += "(FC:" + reqData.getFault().getFaultCode() + ")";
            String resShortname = abbrevMap.get(resMethod);
            if (!reqMethod.equals(TR069Method.EMPTY) && reqMethod.equals(resMethod))
                resShortname += "r";
            if (reqMethod.equals(TR069Method.SET_PARAMETER_VALUES) && !reqRes.getSessionData().isProvisioningAllowed())
                reqShortname += "lim";
            if (reqMethod.equals(TR069Method.INFORM))
                reqShortname += "(" + reqRes.getSessionData().getEventCodes() + ")";
            methodsUsed += "[" + reqShortname;
            if (reqRes.getThrowable() != null)
                methodsUsed += "-(F)" + resShortname + "] ";
            else
                methodsUsed += "-" + resShortname + "] ";
        }

        return methodsUsed;
    }

    private static String makeEventMsg(HTTPReqResData reqRes, long diff, String methodsUsed) {
        List<HTTPReqResData> reqResList = reqRes.getSessionData().getReqResList();
        HttpServletRequest req = reqRes.getReq();
        SessionData sessionData = reqRes.getSessionData();
        String eventMsg = String.format("%1$-16s %2$6dms %3$-60s", req.getRemoteHost(), diff, methodsUsed);
        //		String eventMsg = "[" + req.getRemoteHost() + "] [" + diff + " ms] [" + methodsUsed + "]";
        String job = "      ";
        if (sessionData.getJob() != null)
            job = String.format("%-6s", sessionData.getJob().getId());
        eventMsg += job;
        String parameterList = "";
        int paramsToCPE = 0;
        for (HTTPReqResData reqResItem : reqResList) {
            String resMethod = reqResItem.getResponse().getMethod();
            if (resMethod != null && resMethod.equals(TR069Method.SET_PARAMETER_VALUES)) {
                paramsToCPE = sessionData.getToCPE().getParameterValueList().size();
                for (ParameterValueStruct pvs : sessionData.getToCPE().getParameterValueList()) {
                    parameterList += pvs.getName() + "=" + pvs.getValue() + ", ";
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
