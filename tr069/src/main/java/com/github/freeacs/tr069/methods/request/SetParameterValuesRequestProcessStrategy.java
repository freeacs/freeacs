package com.github.freeacs.tr069.methods.request;

import com.github.freeacs.base.Log;
import com.github.freeacs.base.db.DBAccessSession;
import com.github.freeacs.dbi.SyslogConstants;
import com.github.freeacs.dbi.util.SyslogClient;
import com.github.freeacs.http.HTTPRequestResponseData;
import com.github.freeacs.tr069.SessionData;
import com.github.freeacs.tr069.methods.ProvisioningMethod;
import com.github.freeacs.tr069.xml.ParameterList;
import com.github.freeacs.tr069.xml.ParameterValueStruct;
import com.github.freeacs.tr069.xml.Parser;

public class SetParameterValuesRequestProcessStrategy implements RequestProcessStrategy {

    @SuppressWarnings("Duplicates")
    @Override
    public void process(HTTPRequestResponseData reqRes) throws Exception {
        reqRes.getRequestData().setMethod(ProvisioningMethod.SetParameterValues.name());
        Parser parser = new Parser(reqRes.getRequestData().getXml());
        if (parser.getHeader().getNoMoreRequests() != null
                && parser.getHeader().getNoMoreRequests().getNoMoreRequestFlag()) {
            reqRes.getSessionData().setNoMoreRequests(true);
        }
        SessionData sessionData = reqRes.getSessionData();
        ParameterList paramList = sessionData.getToCPE();
        for (ParameterValueStruct pvs : paramList.getParameterValueList()) {
            Log.notice(SetParameterValuesRequestProcessStrategy.class, "\t" + pvs.getName() + " : " + pvs.getValue());
            String user = new DBAccessSession(reqRes.getDbAccess().getDBI().getAcs())
                            .getAcs()
                            .getSyslog()
                            .getIdentity()
                            .getUser()
                            .getUsername();
            SyslogClient.notice(
                    sessionData.getUnitId(),
                    "ProvMsg: Written to CPE: " + pvs.getName() + " = " + pvs.getValue(),
                    SyslogConstants.FACILITY_TR069,
                    "latest",
                    user);
        }
    }
}
