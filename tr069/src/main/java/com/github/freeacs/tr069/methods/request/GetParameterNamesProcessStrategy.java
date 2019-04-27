package com.github.freeacs.tr069.methods.request;

import com.github.freeacs.tr069.base.DBIActions;
import com.github.freeacs.dbi.DBI;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.dbi.UnittypeParameter;
import com.github.freeacs.dbi.UnittypeParameterFlag;
import com.github.freeacs.tr069.http.HTTPRequestResponseData;
import com.github.freeacs.tr069.Properties;
import com.github.freeacs.tr069.SessionData;
import com.github.freeacs.tr069.exception.TR069Exception;
import com.github.freeacs.tr069.exception.TR069ExceptionShortMessage;
import com.github.freeacs.tr069.methods.ProvisioningMethod;
import com.github.freeacs.tr069.xml.ParameterInfoStruct;
import com.github.freeacs.tr069.xml.ParameterList;
import com.github.freeacs.tr069.xml.Parser;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class GetParameterNamesProcessStrategy implements RequestProcessStrategy {
    private final DBI dbi;

    private Properties properties;

    GetParameterNamesProcessStrategy(Properties properties, DBI dbi) {
        this.properties = properties;
        this.dbi = dbi;
    }

    @SuppressWarnings("Duplicates")
    @Override
    public void process(HTTPRequestResponseData reqRes) throws Exception {
        reqRes.getRequestData().setMethod(ProvisioningMethod.GetParameterNames.name());
        Parser parser = new Parser(reqRes.getRequestData().getXml());

        ParameterList parameterList = parser.getParameterList();
        List<ParameterInfoStruct> pisList = parameterList.getParameterInfoList();
        SessionData sessionData = reqRes.getSessionData();
        if (parser.getHeader().getNoMoreRequests() != null
                && parser.getHeader().getNoMoreRequests().getNoMoreRequestFlag()) {
            sessionData.setNoMoreRequests(true);
        }
        try {
            Unittype ut = sessionData.getUnittype();
            List<UnittypeParameter> utpList = new ArrayList<>();
            for (ParameterInfoStruct pis : pisList) {
                if (pis.getName().endsWith(".")) {
                    continue;
                }
                String newFlag;
                if (pis.isWritable()) {
                    newFlag = "RW";
                } else {
                    newFlag = "R";
                }
                UnittypeParameter utp = ut.getUnittypeParameters().getByName(pis.getName());
                if (utp == null) {
                    utp = new UnittypeParameter(ut, pis.getName(), new UnittypeParameterFlag(newFlag));
                } else { // modify existing flag - only change (if necessary) R->RW or RW->R, leave other
                    // flags untouched!
                    String existingFlag = utp.getFlag().getFlag();
                    if ("R".equals(newFlag)) { // delete W from existsingFlag if necessary
                        newFlag = existingFlag.replace("W", "");
                    } else { // newFlag == 'RW' - remove W and then replace R with RW (make the flag easier to
                        // read for humans)
                        newFlag = existingFlag.replace("W", "");
                        newFlag = newFlag.replace("R", "RW");
                    }
                    utp.setFlag(new UnittypeParameterFlag(newFlag));
                }
                if (!utpList.contains(utp)) {
                    utpList.add(utp);
                } else {
                    log.debug("The unittype parameter " + utp.getName() + " was found more than once in the GPNRes");
                }
            }
            DBIActions.writeUnittypeParameters(sessionData, utpList, dbi);
            log.debug("Unittype parameters (" + pisList.size() + ") is written to DB, will now reload unit");
            sessionData.setFromDB(null);
            sessionData.setAcsParameters(null);
            DBIActions.updateParametersFromDB(sessionData, properties.isDiscoveryMode(), dbi);
        } catch (Throwable t) {
            throw new TR069Exception("Processing GPNRes failed", TR069ExceptionShortMessage.MISC, t);
        }
    }
}
