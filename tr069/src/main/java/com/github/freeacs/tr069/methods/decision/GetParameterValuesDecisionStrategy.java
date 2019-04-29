package com.github.freeacs.tr069.methods.decision;

import com.github.freeacs.tr069.CPEParameters;
import com.github.freeacs.tr069.Properties;
import com.github.freeacs.tr069.SessionData;
import com.github.freeacs.tr069.base.DBIActions;
import com.github.freeacs.tr069.base.DownloadLogic;
import com.github.freeacs.tr069.base.JobLogic;
import com.github.freeacs.tr069.base.ResetUtil;
import com.github.freeacs.tr069.base.ServiceWindow;
import com.github.freeacs.tr069.base.UnitJob;
import com.github.freeacs.dbi.*;
import com.github.freeacs.dbi.tr069.TR069DMParameter;
import com.github.freeacs.dbi.tr069.TR069DMParameterMap;
import com.github.freeacs.dbi.util.ProvisioningMessage;
import com.github.freeacs.dbi.util.ProvisioningMode;
import com.github.freeacs.dbi.util.SystemParameters;
import com.github.freeacs.tr069.http.HTTPRequestResponseData;
import com.github.freeacs.tr069.background.ActiveDeviceDetectionTask;
import com.github.freeacs.tr069.exception.TR069DatabaseException;
import com.github.freeacs.tr069.exception.TR069Exception;
import com.github.freeacs.tr069.exception.TR069ExceptionShortMessage;
import com.github.freeacs.tr069.methods.*;
import com.github.freeacs.tr069.methods.decision.GetParameterValues.DownloadLogicTR069;
import com.github.freeacs.tr069.methods.decision.GetParameterValues.ShellJobLogic;
import com.github.freeacs.tr069.xml.ParameterList;
import com.github.freeacs.tr069.xml.ParameterValueStruct;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class GetParameterValuesDecisionStrategy implements DecisionStrategy {
    private final Properties properties;
    private final DBI dbi;
    private final ScriptExecutions scriptExecutions;

    GetParameterValuesDecisionStrategy(Properties properties, DBI dbi) {
        this.properties = properties;
        this.dbi = dbi;
        this.scriptExecutions = new ScriptExecutions(dbi.getDataSource());
    }

    @SuppressWarnings("Duplicates")
    @Override
    public void makeDecision(HTTPRequestResponseData reqRes) throws Exception {
        SessionData sessionData = reqRes.getSessionData();
        ProvisioningMode mode = sessionData.getUnit().getProvisioningMode();
        log.debug("Mode was detected to be: " + mode);
        ProvisioningMessage pm = sessionData.getProvisioningMessage();
        pm.setProvMode(mode);
        boolean PIIsupport = supportPII(sessionData);
        if (!PIIsupport) {
            reqRes.getResponseData().setMethod(ProvisioningMethod.Empty.name());
            pm.setProvOutput(ProvisioningMessage.ProvOutput.EMPTY);
            pm.setErrorMessage("The device does not support PII");
            pm.setProvStatus(ProvisioningMessage.ProvStatus.ERROR);
            pm.setErrorResponsibility(ProvisioningMessage.ErrorResponsibility.CLIENT);
        } else if (mode == ProvisioningMode.REGULAR) {
            processPeriodic(reqRes, properties.isDiscoveryMode(), properties.getPublicUrl(), properties.getConcurrentDownloadLimit());
        } else if (mode == ProvisioningMode.READALL) {
            processExtraction(reqRes);
        }
        updateActiveDeviceMap(reqRes);
        log.debug("GPV-Decision is " + reqRes.getResponseData().getMethod());
    }

    @SuppressWarnings("Duplicates")
    private void updateActiveDeviceMap(HTTPRequestResponseData reqRes) {
        boolean updated = false;
        SessionData sessionData = reqRes.getSessionData();
        if (ProvisioningMethod.SetParameterValues.name().equals(reqRes.getResponseData().getMethod())) {
            Long nextPII = null;
            CPEParameters cpeParams = sessionData.getCpeParameters();
            String PII = cpeParams.PERIODIC_INFORM_INTERVAL;
            for (ParameterValueStruct pvs : sessionData.getToDB()) {
                if (pvs.getName().equals(PII)) {
                    nextPII = Long.parseLong(pvs.getValue());
                }
            }
            if (nextPII != null) {
                updated = true;
                ActiveDeviceDetectionTask.addActiveDevice(sessionData.getUnitId(), nextPII * 1000);
            }
        }
        if (!updated) {
            ActiveDeviceDetectionTask.remove(sessionData.getUnitId());
        }
    }

    @SuppressWarnings("Duplicates")
    private void normalPriorityProvisioning(HTTPRequestResponseData reqRes,
                                            String publicUrl,
                                            int concurrentDownloadLimit) {
        ServiceWindow serviceWindow;
        SessionData sessionData = reqRes.getSessionData();
        String reset = sessionData.getAcsParameters().getValue(SystemParameters.RESET);
        String reboot = sessionData.getAcsParameters().getValue(SystemParameters.RESTART);
        if ("1".equals(reset)) {
            sessionData.getProvisioningMessage().setProvOutput(ProvisioningMessage.ProvOutput.RESET);
            serviceWindow = new ServiceWindow(sessionData, true);
            if (serviceWindow.isWithin()) {
                ResetUtil.resetReset(sessionData);
                reqRes.getResponseData().setMethod(ProvisioningMethod.FactoryReset.name());
                return;
            } else {
                sessionData.getPIIDecision().setDisruptiveSW(serviceWindow);
            }
        } else if ("1".equals(reboot)) {
            sessionData.getProvisioningMessage().setProvOutput(ProvisioningMessage.ProvOutput.REBOOT);
            serviceWindow = new ServiceWindow(sessionData, true);
            if (serviceWindow.isWithin()) {
                ResetUtil.resetReboot(sessionData);
                reqRes.getResponseData().setMethod(ProvisioningMethod.Reboot.name());
                return;
            } else {
                sessionData.getPIIDecision().setDisruptiveSW(serviceWindow);
            }
        } else if ((DownloadLogicTR069.isSoftwareDownloadSetup(reqRes, null, publicUrl)
                && DownloadLogic.downloadAllowed(null, concurrentDownloadLimit))
                || (DownloadLogicTR069.isScriptDownloadSetup(reqRes, null, publicUrl)
                && DownloadLogic.downloadAllowed(null, concurrentDownloadLimit))) {
            serviceWindow = new ServiceWindow(sessionData, true);
            if (serviceWindow.isWithin()) {
                reqRes.getResponseData().setMethod(ProvisioningMethod.Download.name());
                return;
            } else {
                sessionData.getPIIDecision().setDisruptiveSW(serviceWindow);
            }
        } else {
            sessionData.getProvisioningMessage().setProvOutput(ProvisioningMessage.ProvOutput.CONFIG);
            serviceWindow = new ServiceWindow(sessionData, false);
            if (serviceWindow.isWithin()) {
                prepareSPV(sessionData);
                if (!sessionData.getToCPE().getParameterValueList().isEmpty()) {
                    reqRes.getResponseData().setMethod(ProvisioningMethod.SetParameterValues.name());
                } else {
                    reqRes.getResponseData().setMethod(ProvisioningMethod.Empty.name());
                }
                return;
            }
        }
        prepareSPVLimited(reqRes);
        reqRes.getResponseData().setMethod(ProvisioningMethod.SetParameterValues.name());
    }

    @SuppressWarnings("Duplicates")
    private void processPeriodic(HTTPRequestResponseData reqRes,
                                 boolean isDiscoveryMode,
                                 String publicUrl,
                                 int concurrentDownloadLimit)
            throws TR069Exception {
        SessionData sessionData = reqRes.getSessionData();

        UnitJob uj = null;
        if (!sessionData.isJobUnderExecution()) {
            // update unit-parameters with data from CPE, to get correct
            // group-matching in job-search
            // will not affect the comparison in populateToCollections()
            updateUnitParameters(sessionData);
            uj = JobLogic.checkNewJob(sessionData, dbi, concurrentDownloadLimit); // may find a new job
        }
        Job job = sessionData.getJob();
        if (job != null) { // No job is present - process according to
            // profile/unit-parameters
            jobProvisioning(reqRes, job, uj, isDiscoveryMode, publicUrl);
        } else {
            normalPriorityProvisioning(reqRes, publicUrl, concurrentDownloadLimit);
        }
    }

    @SuppressWarnings("Duplicates")
    private void jobProvisioning(HTTPRequestResponseData reqRes,
                                 Job job,
                                 UnitJob unitJob,
                                 boolean isDiscoveryMode,
                                 String publicUrl)
            throws TR069Exception {
        SessionData sessionData = reqRes.getSessionData();
        sessionData.getProvisioningMessage().setJobId(job.getId());
        JobFlag.JobType type = job.getFlags().getType();
        if (type == JobFlag.JobType.RESET) {
            sessionData.getProvisioningMessage().setProvOutput(ProvisioningMessage.ProvOutput.RESET);
            reqRes.getResponseData().setMethod(ProvisioningMethod.FactoryReset.name());
        } else if (type == JobFlag.JobType.RESTART) {
            sessionData.getProvisioningMessage().setProvOutput(ProvisioningMessage.ProvOutput.REBOOT);
            reqRes.getResponseData().setMethod(ProvisioningMethod.Reboot.name());
        } else if (type == JobFlag.JobType.SOFTWARE) {
            sessionData.getProvisioningMessage().setProvOutput(ProvisioningMessage.ProvOutput.SOFTWARE);
            if (!DownloadLogicTR069.isSoftwareDownloadSetup(reqRes, job, publicUrl)) {
                throw new RuntimeException("Not possible to setup a Software Download job");
            }
            reqRes.getResponseData().setMethod(ProvisioningMethod.Download.name());
        } else if (type == JobFlag.JobType.TR069_SCRIPT) {
            sessionData.getProvisioningMessage().setProvOutput(ProvisioningMessage.ProvOutput.SCRIPT);
            if (!DownloadLogicTR069.isScriptDownloadSetup(reqRes, job, publicUrl)) {
                throw new RuntimeException("Not possible to setup a Script Download job");
            }
            reqRes.getResponseData().setMethod(ProvisioningMethod.Download.name());
        } else {
            if (type == JobFlag.JobType.SHELL) {
                sessionData.getProvisioningMessage().setProvOutput(ProvisioningMessage.ProvOutput.SHELL);
                ShellJobLogic.execute(sessionData, dbi, job, unitJob, isDiscoveryMode, scriptExecutions);
            } else { // type == JobType.CONFIG
                // The service-window is unimportant for next PII calculation, will
                // be set to 31 no matter what, since a job is "in the process".
                sessionData.getProvisioningMessage().setProvOutput(ProvisioningMessage.ProvOutput.CONFIG);
                // ServiceWindow serviceWindow = new ServiceWindow(sessionData, false);
                prepareSPVForConfigJob(sessionData);
            }
            reqRes.getResponseData().setMethod(ProvisioningMethod.SetParameterValues.name());
        }
    }

    @SuppressWarnings("Duplicates")
    private boolean supportPII(SessionData sessionData) {
        CPEParameters cpeParams = sessionData.getCpeParameters();
        String PII = cpeParams.PERIODIC_INFORM_INTERVAL;
        UnittypeParameters utps = sessionData.getUnittype().getUnittypeParameters();
        if (utps.getByName(PII) != null && cpeParams.getValue(PII) != null) {
            log.debug("CPE supports PeriodicInformInterval");
            return true;
        } else {
            if (utps.getByName(PII) != null) {
                log.error("The CPE did not return PeriodicInformInterval, terminating the conversation.");
            } else { // (cpeParams.getValue(PII) == null)
                log.error("The unittype does not contain PeriodicInformInterval, terminating the conversation.");
            }
            return false;
        }
    }

    @SuppressWarnings("Duplicates")
    private void prepareSPVLimited(HTTPRequestResponseData reqRes) {
        SessionData sessionData = reqRes.getSessionData();
        sessionData.setProvisioningAllowed(false);
        sessionData.getProvisioningMessage().setProvStatus(ProvisioningMessage.ProvStatus.DELAYED);
        CPEParameters cpeParams = sessionData.getCpeParameters();
        String PII = cpeParams.PERIODIC_INFORM_INTERVAL;
        ParameterValueStruct pvs = cpeParams.getCpeParams().get(PII);
        ParameterList toCPE = new ParameterList();
        long nextPII = sessionData.getPIIDecision().nextPII();
        sessionData.getProvisioningMessage().setPeriodicInformInterval((int) nextPII);
        pvs.setValue(String.valueOf(nextPII));
        pvs.setType("xsd:unsignedInt");
        log.debug("All previous CPE parameter changes are cancelled, will only set PeriodicInformInterval ("
                        + pvs.getValue()
                        + ") to CPE and ACS");
        toCPE.addParameterValueStruct(pvs);
        sessionData.setToCPE(toCPE);
        sessionData.getToDB().add(new ParameterValueStruct(PII, String.valueOf(nextPII)));
        log.debug("-ACS->ACS      " + PII + " CPE[" + nextPII + "] ACS[" + nextPII + "] Decided by ACS");
        sessionData
                .getToDB()
                .add(new ParameterValueStruct(SystemParameters.PERIODIC_INTERVAL, String.valueOf(nextPII)));
        log.debug("-ACS->ACS      "
                        + SystemParameters.PERIODIC_INTERVAL
                        + " CPE["
                        + nextPII
                        + "] ACS["
                        + nextPII
                        + "] Decided by ACS");
        DBIActions.writeUnitParams(sessionData);
    }

    @SuppressWarnings("Duplicates")
    private void prepareSPVForConfigJob(SessionData sessionData) throws TR069Exception {
        // populate to collections from job-params
        // impl.
        ParameterList toCPE = new ParameterList();
        TR069DMParameterMap dataModel;
        try {
            dataModel = TR069DMParameterMap.getTR069ParameterMap();
        } catch (Exception e) {
            throw new TR069Exception("TR069 Data model not found", TR069ExceptionShortMessage.MISC, e);
        }
        for (JobParameter jp : sessionData.getJobParams().values()) {
            Parameter jup = jp.getParameter();
            String jpName = jup.getUnittypeParameter().getName();
            if (SystemParameters.JOB_CURRENT.equals(jpName)
                    || SystemParameters.JOB_HISTORY.equals(jpName)) {
                continue;
            }

            UnittypeParameterFlag upFlag = jup.getUnittypeParameter().getFlag();
            String jpValue = jup.getValue();
            // ParameterValueStruct jpPvs = new ParameterValueStruct(jpName, jpValue);

            if (upFlag.isSystem() || upFlag.isReadOnly()) {
                log.debug("Skipped "
                                + jpName
                                + " since it's a system/read-only parameter, cannot be set in the CPE");
            } else {
                log.debug("Added " + jpName + ", value:[" + jpValue + "] to session - will be asked for in GPV");
                TR069DMParameter dmp = dataModel.getParameter(jpName);
                if (dmp != null) {
                    toCPE.addParameterValueStruct(
                            new ParameterValueStruct(jpName, jpValue, dmp.getDatatype().getXsdType()));
                } else {
                    toCPE.addParameterValueStruct(new ParameterValueStruct(jpName, jpValue, "xsd:string"));
                }
            }
        }
        sessionData.setToCPE(toCPE);

        CPEParameters cpeParams = sessionData.getCpeParameters();
        String PII = cpeParams.PERIODIC_INFORM_INTERVAL;
        String nextPII = String.valueOf(sessionData.getPIIDecision().nextPII());
        sessionData.getProvisioningMessage().setPeriodicInformInterval(Integer.valueOf(nextPII));
        if (cpeParams.getValue(PII) != null && cpeParams.getValue(PII).equals(nextPII)) {
            log.debug("-No change     "
                            + PII
                            + " CPE["
                            + PII
                            + "] ACS["
                            + nextPII
                            + "] Default action, the values should be equal");
        } else {
            sessionData.getToCPE().addOrChangeParameterValueStruct(PII, nextPII, "xsd:unsignedInt");
            log.debug("-ACS->CPE      " + PII + " CPE[" + nextPII + "] ACS[" + nextPII + "] Decided by ACS");
            sessionData.getToDB().add(new ParameterValueStruct(PII, nextPII));
            log.debug("-ACS->ACS      " + PII + " CPE[" + nextPII + "] ACS[" + nextPII + "] Decided by ACS");
            sessionData
                    .getToDB()
                    .add(new ParameterValueStruct(SystemParameters.PERIODIC_INTERVAL, nextPII));
            log.debug("-ACS->ACS      "
                            + SystemParameters.PERIODIC_INTERVAL
                            + " CPE["
                            + nextPII
                            + "] ACS["
                            + nextPII
                            + "] Decided by ACS");
        }
        DBIActions.writeUnitParams(sessionData);
    }

    @SuppressWarnings("Duplicates")
    private void prepareSPV(SessionData sessionData) {
        populateToCollections(sessionData);
        CPEParameters cpeParams = sessionData.getCpeParameters();
        String PII = cpeParams.PERIODIC_INFORM_INTERVAL;
        String nextPII = String.valueOf(sessionData.getPIIDecision().nextPII());

        // Cleanup after all jobs have been completed
        String disruptiveJob = sessionData.getAcsParameters().getValue(SystemParameters.JOB_DISRUPTIVE);
        if ("1".equals(disruptiveJob)) {
            log.debug("No more jobs && disruptive flag set -> disruptive flag reset (to 0)");
            ParameterValueStruct disruptivePvs =
                    new ParameterValueStruct(SystemParameters.JOB_DISRUPTIVE, "0");
            sessionData.getToDB().add(disruptivePvs);
        }

        sessionData.getProvisioningMessage().setPeriodicInformInterval(Integer.valueOf(nextPII));
        if (cpeParams.getValue(PII) != null && cpeParams.getValue(PII).equals(nextPII)) {
            log.debug("-No change     "
                            + PII
                            + " CPE["
                            + PII
                            + "] ACS["
                            + nextPII
                            + "] Default action, the values should be equal");
        } else {
            sessionData.getToCPE().addOrChangeParameterValueStruct(PII, nextPII, "xsd:unsignedInt");
            log.debug("-ACS->CPE      " + PII + " CPE[" + nextPII + "] ACS[" + nextPII + "] Decided by ACS");
            sessionData.getToDB().add(new ParameterValueStruct(PII, nextPII));
            log.debug("-ACS->ACS      " + PII + " CPE[" + nextPII + "] ACS[" + nextPII + "] Decided by ACS");
            sessionData
                    .getToDB()
                    .add(new ParameterValueStruct(SystemParameters.PERIODIC_INTERVAL, nextPII));
            log.debug("-ACS->ACS      "
                            + SystemParameters.PERIODIC_INTERVAL
                            + " CPE["
                            + nextPII
                            + "] ACS["
                            + nextPII
                            + "] Decided by ACS");
        }
        DBIActions.writeUnitParams(sessionData);
    }

    /**
     * Loop through all parameters defined in the database, and see which ones are missing in the CPE.
     * We only do this if the GPV has been run twice, clearly indicating that the first GPV resulted
     * in FA, something that CAN happen because we ask for a parameter from the CPE which is not
     * there. Usually this will happen because the firmware is no longer in sync with the unittype
     * definition.
     */
    @SuppressWarnings("Duplicates")
    private void logMissingCPEParams(SessionData sessionData) {
        boolean parameterMissing = false;
        for (ParameterValueStruct pvsDB : sessionData.getFromDB().values()) {
            boolean match = false;
            for (ParameterValueStruct pvsCPE : sessionData.getValuesFromCPE()) {
                if (pvsDB.getName().equals(pvsCPE.getName())) {
                    match = true;
                    parameterMissing = true;
                }
            }
            if (!match) {
                String logMessage = "The parameter " + pvsDB.getName() + " is defined in the database,";
                logMessage +=
                        "but does not exist in the CPE. Delete it from the unittype or update the firmware! ";
                logMessage += "This situation will impact on the performance of the system.";
                log.warn(logMessage);
            }
        }
        if (parameterMissing) {
            log.warn("GPV has been issued twice, but apparantly the reason for the failure of the first GPV-response is not due to missing parameters in the CPE.");
        }
    }

    @SuppressWarnings("Duplicates")
    private String msg(UnittypeParameter utp, String cpeValue, String acsValue, String action, String cause) {
        if (utp.getFlag().isConfidential()) {
            return "-"
                    + String.format("%-15s", action)
                    + utp.getName()
                    + " CPE:[*****] ACS:[*****] Flags:["
                    + utp.getFlag()
                    + "] Cause:["
                    + cause
                    + "]";
        } else {
            return "-"
                    + String.format("%-15s", action)
                    + utp.getName()
                    + " CPE:["
                    + cpeValue
                    + "] ACS:["
                    + acsValue
                    + "] Flags:["
                    + utp.getFlag()
                    + "] Cause:["
                    + cause
                    + "]";
        }
    }

    @SuppressWarnings("Duplicates")
    private void populateToCollections(SessionData sessionData) {
        UnittypeParameters utps = sessionData.getUnittype().getUnittypeParameters();
        ParameterList toCPE = new ParameterList();
        List<ParameterValueStruct> toDB = new ArrayList<>();

        for (int i = 0; i < sessionData.getValuesFromCPE().size(); i++) {
            ParameterValueStruct pvsCPE = sessionData.getValuesFromCPE().get(i);
            ParameterValueStruct pvsDB = sessionData.getFromDB().get(pvsCPE.getName());
            UnittypeParameter utp = utps.getByName(pvsCPE.getName());
            String cpeV = pvsCPE.getValue();
            String acsV = null;
            if (pvsDB != null) {
                acsV = pvsDB.getValue();
            }
            if (utp == null) {
                log.debug("The parameter name "
                                + pvsCPE.getName()
                                + " was not recognized in ACS, could happen if a GPV on all params was issued.");
                continue;
            }
            if (acsV != null && !"ExtraCPEParam".equals(acsV) && !acsV.equals(cpeV)) {
                if (utp.getFlag().isReadWrite()) {
                    if (sessionData.getParameterKey().isEqual()) {
                        if (utp.getFlag().isConfidential()) {
                            String msg =
                                    msg(
                                            utp,
                                            cpeV,
                                            acsV,
                                            "No change",
                                            "Values differ & parameterkey is correct & param is confidential (C flag)");
                            log.debug(msg);
                        } else {
                            String unlc = utp.getName().toLowerCase();
                            String msg;
                            if (unlc.contains("password")
                                    || unlc.contains("secret")
                                    || unlc.contains("passphrase")) {
                                msg =
                                        msg(
                                                utp,
                                                cpeV,
                                                acsV,
                                                "ACS->CPE",
                                                "Values differ & parameterkey is correct -> parameter is probably a secret and should be set to confidential to avoid unnecessary prov. of secret");
                            } else {
                                msg =
                                        msg(
                                                utp,
                                                cpeV,
                                                acsV,
                                                "ACS->CPE",
                                                "Values differ & parameterkey is correct -> parameter must have been changed on device OR should be set to confidential to avoid unnecessary write");
                            }
                            if (pvsDB != null) {
                                pvsDB.setType(pvsCPE.getType());
                            }
                            toCPE.addParameterValueStruct(pvsDB);
                            log.warn(msg);
                        }
                    } else {
                        log.debug(msg(utp, cpeV, acsV, "ACS->CPE", "Param has ReadWrite-flag"));
                        if (pvsDB != null) {
                            pvsDB.setType(pvsCPE.getType());
                        }
                        toCPE.addParameterValueStruct(pvsDB);
                    }
                } else {
                    log.debug(msg(utp, cpeV, acsV, "CPE->ACS", "Param has ReadOnly-flag"));
                    toDB.add(pvsCPE);
                }
            } else if (pvsDB != null
                    && pvsDB.getValue() == null
                    && pvsCPE.getValue() != null
                    && !pvsCPE.getValue().isEmpty()) {
                log.debug(msg(utp, cpeV, acsV, "CPE->ACS", "Param new to ACS"));
                toDB.add(pvsCPE);
            } else if ("ExtraCPEParam".equals(acsV)) {
                log.debug(msg(utp, cpeV, acsV, "No change", "Ignore ExtraCPEParam - they're treated separately"));
            } else {
                log.debug(msg(utp, cpeV, acsV, "No change", "Default action, the values should be equal"));
            }
        }
        // List<RequestResponseData> reqResList = sessionData.getReqResList();
        String previousMethod = sessionData.getMethodBeforePreviousResponseMethod();
        log.debug("PreviousResponseMethod before deciding on log missing cpe params: " + previousMethod);
        if (ProvisioningMethod.GetParameterValues.name().equals(previousMethod)) {
            logMissingCPEParams(sessionData);
        }
        sessionData.setToCPE(toCPE);
        sessionData.setToDB(toDB);
        // sessionData.setToSyslog(toSyslog);
        log.debug(toCPE.getParameterValueList().size() + " params to CPE, " + toDB.size() + " params to ACS");
    }

    /** Make sure unit parameters accurately represents CPE parameters. */
    @SuppressWarnings("Duplicates")
    private void updateUnitParameters(SessionData sessionData) {
        if (sessionData.getUnit() != null && sessionData.getUnit().getUnitParameters() != null) {
            Map<String, String> parameters = sessionData.getUnit().getParameters();
            for (int i = 0;
                 sessionData.getValuesFromCPE() != null && i < sessionData.getValuesFromCPE().size();
                 i++) {
                ParameterValueStruct pvs = sessionData.getValuesFromCPE().get(i);
                if (pvs != null && pvs.getValue() != null) {
                    parameters.put(pvs.getName(), pvs.getValue());
                }
            }
        }
    }

    /**
     * Extraction mode will read all parameters from the device and write them to the
     * unit_param_session table. No data will be written to unit_param table (provisioned data).
     */
    @SuppressWarnings("Duplicates")
    private void processExtraction(HTTPRequestResponseData reqRes) throws TR069DatabaseException {
        SessionData sessionData = reqRes.getSessionData();
        UnittypeParameters utps = sessionData.getUnittype().getUnittypeParameters();
        List<ParameterValueStruct> toDB = new ArrayList<>();
        log.info("Provisioning in "
                        + ProvisioningMode.READALL
                        + " mode, "
                        + sessionData.getValuesFromCPE().size()
                        + " params from CPE may be copied to ACS session storage");
        //		log.info(GetParameterValuesDecisionStrategyExtraction.class, "Provisioning in EXTRACTION mode, " +
        // sessionData.getFromCPE().size() + " params from CPE may be copied to ACS session storage");
        for (int i = 0; i < sessionData.getValuesFromCPE().size(); i++) {
            ParameterValueStruct pvsCPE = sessionData.getValuesFromCPE().get(i);
            UnittypeParameter utp = utps.getByName(pvsCPE.getName());
            if (utp == null) {
                log.debug(pvsCPE.getName() + " could not be stored in ACS, since name was unrecognized in ACS");
                continue;
            }
            if ("(null)".equals(pvsCPE.getValue())) {
                log.debug(pvsCPE.getName() + " will not be stored in ACS, since value was '(null)' - indicating not implemented");
                continue;
            }
            toDB.add(pvsCPE);
        }
        sessionData.setToDB(toDB);
        DBIActions.writeUnitSessionParams(sessionData, dbi);
        log.debug(toDB.size() + " params written to ACS session storage");
        reqRes.getResponseData().setMethod(ProvisioningMethod.Empty.name());
        sessionData.getProvisioningMessage().setProvOutput(ProvisioningMessage.ProvOutput.EMPTY);
    }
}

