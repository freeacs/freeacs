package com.github.freeacs.tr069.methods;

import com.github.freeacs.base.DownloadLogic;
import com.github.freeacs.base.JobLogic;
import com.github.freeacs.base.Log;
import com.github.freeacs.base.ServiceWindow;
import com.github.freeacs.base.UnitJob;
import com.github.freeacs.base.Util;
import com.github.freeacs.base.db.DBAccessSessionTR069;
import com.github.freeacs.dbi.Job;
import com.github.freeacs.dbi.JobFlag.JobType;
import com.github.freeacs.dbi.JobParameter;
import com.github.freeacs.dbi.Parameter;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.dbi.UnittypeParameter;
import com.github.freeacs.dbi.UnittypeParameterFlag;
import com.github.freeacs.dbi.UnittypeParameters;
import com.github.freeacs.dbi.tr069.TR069DMParameter;
import com.github.freeacs.dbi.tr069.TR069DMParameterMap;
import com.github.freeacs.dbi.util.ProvisioningMessage;
import com.github.freeacs.dbi.util.ProvisioningMessage.ErrorResponsibility;
import com.github.freeacs.dbi.util.ProvisioningMessage.ProvOutput;
import com.github.freeacs.dbi.util.ProvisioningMessage.ProvStatus;
import com.github.freeacs.dbi.util.ProvisioningMode;
import com.github.freeacs.dbi.util.SystemParameters;
import com.github.freeacs.tr069.CPEParameters;
import com.github.freeacs.tr069.DownloadLogicTR069;
import com.github.freeacs.http.HTTPRequestResponseData;
import com.github.freeacs.tr069.SessionData;
import com.github.freeacs.tr069.background.ActiveDeviceDetectionTask;
import com.github.freeacs.tr069.decision.shelljob.ShellJobLogic;
import com.github.freeacs.tr069.exception.TR069Exception;
import com.github.freeacs.tr069.exception.TR069ExceptionShortMessage;
import com.github.freeacs.tr069.xml.ParameterList;
import com.github.freeacs.tr069.xml.ParameterValueStruct;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Decision is the main class in the decision package and it contains the core logic of what to-do
 * after the initial IN-EM-GPV sequence is performed in the TR-069 conversation. At this point there
 * are many options, all information about the xAPS database and the CPE has been gathered, and we
 * must decide what the next step should be.
 *
 * <p>The next method may be one out of three, in this priority:
 *
 * <p>1. DO (Download) 2. SPV (SetParameterValues) 3. EM (Empty)
 *
 * <p>Which one to choose is decided in the constructor of this class. More details are explained in
 * that comment.
 */
public class GPVDecision {
  public static void process(
      HTTPRequestResponseData reqRes, boolean isDiscoveryMode, String publicUrl, int concurrentDownloadLimit)
      throws TR069Exception, SQLException {
    SessionData sessionData = reqRes.getSessionData();
    ProvisioningMode mode = sessionData.getUnit().getProvisioningMode();
    Log.debug(GPVDecision.class, "Mode was detected to be: " + mode);
    ProvisioningMessage pm = sessionData.getProvisioningMessage();
    pm.setProvMode(mode);
    boolean PIIsupport = supportPII(sessionData);
    if (!PIIsupport) {
      reqRes.getResponseData().setMethod(TR069Method.EMPTY);
      pm.setProvOutput(ProvOutput.EMPTY);
      pm.setErrorMessage("The device does not support PII");
      pm.setProvStatus(ProvStatus.ERROR);
      pm.setErrorResponsibility(ErrorResponsibility.CLIENT);
    } else if (mode == ProvisioningMode.REGULAR) {
      processPeriodic(reqRes, isDiscoveryMode, publicUrl, concurrentDownloadLimit);
    } else if (mode == ProvisioningMode.READALL) {
      GPVDecisionExtraction.processExtraction(reqRes);
    }
    updateActiveDeviceMap(reqRes);
    Log.debug(GPVDecision.class, "GPV-Decision is " + reqRes.getResponseData().getMethod());
  }

  private static void updateActiveDeviceMap(HTTPRequestResponseData reqRes) {
    boolean updated = false;
    SessionData sessionData = reqRes.getSessionData();
    if (TR069Method.SET_PARAMETER_VALUES.equals(reqRes.getResponseData().getMethod())) {
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

  private static void normalPriorityProvisioning(
      HTTPRequestResponseData reqRes, String publicUrl, int concurrentDownloadLimit) {
    ServiceWindow serviceWindow;
    SessionData sessionData = reqRes.getSessionData();
    String reset = sessionData.getAcsParameters().getValue(SystemParameters.RESET);
    String reboot = sessionData.getAcsParameters().getValue(SystemParameters.RESTART);
    if ("1".equals(reset)) {
      sessionData.getProvisioningMessage().setProvOutput(ProvOutput.RESET);
      serviceWindow = new ServiceWindow(sessionData, true);
      if (serviceWindow.isWithin()) {
        Util.resetReset(sessionData);
        reqRes.getResponseData().setMethod(TR069Method.FACTORY_RESET);
        return;
      } else {
        sessionData.getPIIDecision().setDisruptiveSW(serviceWindow);
      }
    } else if ("1".equals(reboot)) {
      sessionData.getProvisioningMessage().setProvOutput(ProvOutput.REBOOT);
      serviceWindow = new ServiceWindow(sessionData, true);
      if (serviceWindow.isWithin()) {
        Util.resetReboot(sessionData);
        reqRes.getResponseData().setMethod(TR069Method.REBOOT);
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
        reqRes.getResponseData().setMethod(TR069Method.DOWNLOAD);
        return;
      } else {
        sessionData.getPIIDecision().setDisruptiveSW(serviceWindow);
      }
    } else {
      sessionData.getProvisioningMessage().setProvOutput(ProvOutput.CONFIG);
      serviceWindow = new ServiceWindow(sessionData, false);
      if (serviceWindow.isWithin()) {
        prepareSPV(sessionData);
        if (!sessionData.getToCPE().getParameterValueList().isEmpty()) {
          reqRes.getResponseData().setMethod(TR069Method.SET_PARAMETER_VALUES);
        } else {
          reqRes.getResponseData().setMethod(TR069Method.EMPTY);
        }
        return;
      }
    }
    prepareSPVLimited(reqRes);
    reqRes.getResponseData().setMethod(TR069Method.SET_PARAMETER_VALUES);
  }

  private static void processPeriodic(
      HTTPRequestResponseData reqRes, boolean isDiscoveryMode, String publicUrl, int concurrentDownloadLimit)
      throws TR069Exception {
    SessionData sessionData = reqRes.getSessionData();

    UnitJob uj = null;
    if (!sessionData.isJobUnderExecution()) {
      // update unit-parameters with data from CPE, to get correct
      // group-matching in job-search
      // will not affect the comparison in populateToCollections()
      updateUnitParameters(sessionData);
      uj = JobLogic.checkNewJob(sessionData, concurrentDownloadLimit); // may find a new job
    }
    Job job = sessionData.getJob();
    if (job != null) { // No job is present - process according to
      // profile/unit-parameters
      jobProvisioning(reqRes, job, uj, isDiscoveryMode, publicUrl);
    } else {
      normalPriorityProvisioning(reqRes, publicUrl, concurrentDownloadLimit);
    }
  }

  private static void jobProvisioning(
      HTTPRequestResponseData reqRes, Job job, UnitJob uj, boolean isDiscoveryMode, String publicUrl)
      throws TR069Exception {
    SessionData sessionData = reqRes.getSessionData();
    sessionData.getProvisioningMessage().setJobId(job.getId());
    JobType type = job.getFlags().getType();
    if (type == JobType.RESET) {
      sessionData.getProvisioningMessage().setProvOutput(ProvOutput.RESET);
      reqRes.getResponseData().setMethod(TR069Method.FACTORY_RESET);
    } else if (type == JobType.RESTART) {
      sessionData.getProvisioningMessage().setProvOutput(ProvOutput.REBOOT);
      reqRes.getResponseData().setMethod(TR069Method.REBOOT);
    } else if (type == JobType.SOFTWARE) {
      sessionData.getProvisioningMessage().setProvOutput(ProvOutput.SOFTWARE);
      if (!DownloadLogicTR069.isSoftwareDownloadSetup(reqRes, job, publicUrl)) {
        throw new RuntimeException("Not possible to setup a Software Download job");
      }
      reqRes.getResponseData().setMethod(TR069Method.DOWNLOAD);
    } else if (type == JobType.TR069_SCRIPT) {
      sessionData.getProvisioningMessage().setProvOutput(ProvOutput.SCRIPT);
      if (!DownloadLogicTR069.isScriptDownloadSetup(reqRes, job, publicUrl)) {
        throw new RuntimeException("Not possible to setup a Script Download job");
      }
      reqRes.getResponseData().setMethod(TR069Method.DOWNLOAD);
    } else {
      if (type == JobType.SHELL) {
        sessionData.getProvisioningMessage().setProvOutput(ProvOutput.SHELL);
        ShellJobLogic.execute(sessionData, job, uj, isDiscoveryMode);
      } else { // type == JobType.CONFIG
        // The service-window is unimportant for next PII calculation, will
        // be set to 31 no matter what, since a job is "in the process".
        sessionData.getProvisioningMessage().setProvOutput(ProvOutput.CONFIG);
        // ServiceWindow serviceWindow = new ServiceWindow(sessionData, false);
        prepareSPVForConfigJob(sessionData);
      }
      reqRes.getResponseData().setMethod(TR069Method.SET_PARAMETER_VALUES);
    }
  }

  private static boolean supportPII(SessionData sessionData) throws SQLException {
    CPEParameters cpeParams = sessionData.getCpeParameters();
    String PII = cpeParams.PERIODIC_INFORM_INTERVAL;
    Unittype unittype = sessionData.getUnittype();
    UnittypeParameter piiParam = unittype.getUnittypeParameters().getByName(PII);
    if (piiParam == null) {
      piiParam = new UnittypeParameter(unittype, PII, new UnittypeParameterFlag("RW"));
      unittype.getUnittypeParameters().addOrChangeUnittypeParameter(piiParam, unittype.getAcs());
    }
    if (cpeParams.getValue(PII) != null) {
      Log.debug(GPVDecision.class, "CPE supports PeriodicInformInterval");
      return true;
    } else {
      Log.error(
          GPVDecision.class,
          "The CPE did not return PeriodicInformInterval, terminating the conversation.");
      return false;
    }
  }

  private static void prepareSPVLimited(HTTPRequestResponseData reqRes) {
    SessionData sessionData = reqRes.getSessionData();
    sessionData.setProvisioningAllowed(false);
    sessionData.getProvisioningMessage().setProvStatus(ProvStatus.DELAYED);
    CPEParameters cpeParams = sessionData.getCpeParameters();
    String PII = cpeParams.PERIODIC_INFORM_INTERVAL;
    ParameterValueStruct pvs = cpeParams.getPvs(PII);
    ParameterList toCPE = new ParameterList();
    long nextPII = sessionData.getPIIDecision().nextPII();
    sessionData.getProvisioningMessage().setPeriodicInformInterval((int) nextPII);
    pvs.setValue(String.valueOf(nextPII));
    pvs.setType("xsd:unsignedInt");
    Log.debug(
        GPVDecision.class,
        "All previous CPE parameter changes are cancelled, will only set PeriodicInformInterval ("
            + pvs.getValue()
            + ") to CPE and ACS");
    toCPE.addParameterValueStruct(pvs);
    sessionData.setToCPE(toCPE);
    sessionData.getToDB().add(new ParameterValueStruct(PII, String.valueOf(nextPII)));
    Log.debug(
        GPVDecision.class,
        "-ACS->ACS      " + PII + " CPE[" + nextPII + "] ACS[" + nextPII + "] Decided by ACS");
    sessionData
        .getToDB()
        .add(new ParameterValueStruct(SystemParameters.PERIODIC_INTERVAL, String.valueOf(nextPII)));
    Log.debug(
        GPVDecision.class,
        "-ACS->ACS      "
            + SystemParameters.PERIODIC_INTERVAL
            + " CPE["
            + nextPII
            + "] ACS["
            + nextPII
            + "] Decided by ACS");
    DBAccessSessionTR069.writeUnitParams(sessionData);
  }

  private static void prepareSPVForConfigJob(SessionData sessionData) throws TR069Exception {
    // populate to collections from job-params
    // impl.
    ParameterList toCPE = new ParameterList();
    TR069DMParameterMap dataModel;
    try {
      dataModel = HTTPRequestProcessor.getTR069ParameterMap();
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
        Log.debug(
            UnitJob.class,
            "Skipped "
                + jpName
                + " since it's a system/read-only parameter, cannot be set in the CPE");
      } else {
        Log.debug(
            UnitJob.class,
            "Added " + jpName + ", value:[" + jpValue + "] to session - will be asked for in GPV");
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
      Log.debug(
          GPVDecision.class,
          "-No change     "
              + PII
              + " CPE["
              + PII
              + "] ACS["
              + nextPII
              + "] Default action, the values should be equal");
    } else {
      sessionData.getToCPE().addOrChangeParameterValueStruct(PII, nextPII, "xsd:unsignedInt");
      Log.debug(
          GPVDecision.class,
          "-ACS->CPE      " + PII + " CPE[" + nextPII + "] ACS[" + nextPII + "] Decided by ACS");
      sessionData.getToDB().add(new ParameterValueStruct(PII, nextPII));
      Log.debug(
          GPVDecision.class,
          "-ACS->ACS      " + PII + " CPE[" + nextPII + "] ACS[" + nextPII + "] Decided by ACS");
      sessionData
          .getToDB()
          .add(new ParameterValueStruct(SystemParameters.PERIODIC_INTERVAL, nextPII));
      Log.debug(
          GPVDecision.class,
          "-ACS->ACS      "
              + SystemParameters.PERIODIC_INTERVAL
              + " CPE["
              + nextPII
              + "] ACS["
              + nextPII
              + "] Decided by ACS");
    }
    DBAccessSessionTR069.writeUnitParams(sessionData);
  }

  private static void prepareSPV(SessionData sessionData) {
    populateToCollections(sessionData);
    CPEParameters cpeParams = sessionData.getCpeParameters();
    String PII = cpeParams.PERIODIC_INFORM_INTERVAL;
    String nextPII = String.valueOf(sessionData.getPIIDecision().nextPII());

    // Cleanup after all jobs have been completed
    String disruptiveJob = sessionData.getAcsParameters().getValue(SystemParameters.JOB_DISRUPTIVE);
    if ("1".equals(disruptiveJob)) {
      Log.debug(
          GPVDecision.class, "No more jobs && disruptive flag set -> disruptive flag reset (to 0)");
      ParameterValueStruct disruptivePvs =
          new ParameterValueStruct(SystemParameters.JOB_DISRUPTIVE, "0");
      sessionData.getToDB().add(disruptivePvs);
    }

    sessionData.getProvisioningMessage().setPeriodicInformInterval(Integer.valueOf(nextPII));
    if (cpeParams.getValue(PII) != null && cpeParams.getValue(PII).equals(nextPII)) {
      Log.debug(
          GPVDecision.class,
          "-No change     "
              + PII
              + " CPE["
              + PII
              + "] ACS["
              + nextPII
              + "] Default action, the values should be equal");
    } else {
      sessionData.getToCPE().addOrChangeParameterValueStruct(PII, nextPII, "xsd:unsignedInt");
      Log.debug(
          GPVDecision.class,
          "-ACS->CPE      " + PII + " CPE[" + nextPII + "] ACS[" + nextPII + "] Decided by ACS");
      sessionData.getToDB().add(new ParameterValueStruct(PII, nextPII));
      Log.debug(
          GPVDecision.class,
          "-ACS->ACS      " + PII + " CPE[" + nextPII + "] ACS[" + nextPII + "] Decided by ACS");
      sessionData
          .getToDB()
          .add(new ParameterValueStruct(SystemParameters.PERIODIC_INTERVAL, nextPII));
      Log.debug(
          GPVDecision.class,
          "-ACS->ACS      "
              + SystemParameters.PERIODIC_INTERVAL
              + " CPE["
              + nextPII
              + "] ACS["
              + nextPII
              + "] Decided by ACS");
    }
    DBAccessSessionTR069.writeUnitParams(sessionData);
  }

  /**
   * Loop through all parameters defined in the database, and see which ones are missing in the CPE.
   * We only do this if the GPV has been run twice, clearly indicating that the first GPV resulted
   * in FA, something that CAN happen because we ask for a parameter from the CPE which is not
   * there. Usually this will happen because the firmware is no longer in sync with the unittype
   * definition.
   */
  private static void logMissingCPEParams(SessionData sessionData) {
    boolean parameterMissing = false;
    for (ParameterValueStruct pvsDB : sessionData.getFromDB().values()) {
      boolean match = false;
      for (ParameterValueStruct pvsCPE : sessionData.getFromCPE()) {
        if (pvsDB.getName().equals(pvsCPE.getName())) {
          match = true;
          parameterMissing = true;
          continue;
        }
      }
      if (!match) {
        String logMessage = "The parameter " + pvsDB.getName() + " is defined in the database,";
        logMessage +=
            "but does not exist in the CPE. Delete it from the unittype or update the firmware! ";
        logMessage += "This situation will impact on the performance of the system.";
        Log.warn(GPVDecision.class, logMessage);
      }
    }
    if (parameterMissing) {
      Log.warn(
          GPVDecision.class,
          "GPV has been issued twice, but apparantly the reason for the failure of the first GPV-response is not due to missing parameters in the CPE.");
    }
  }

  private static String msg(
      UnittypeParameter utp, String cpeValue, String acsValue, String action, String cause) {
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

  private static void populateToCollections(SessionData sessionData) {
    UnittypeParameters utps = sessionData.getUnittype().getUnittypeParameters();
    ParameterList toCPE = new ParameterList();
    List<ParameterValueStruct> toDB = new ArrayList<>();

    for (int i = 0; i < sessionData.getFromCPE().size(); i++) {
      ParameterValueStruct pvsCPE = sessionData.getFromCPE().get(i);
      ParameterValueStruct pvsDB = sessionData.getFromDB().get(pvsCPE.getName());
      UnittypeParameter utp = utps.getByName(pvsCPE.getName());
      String cpeV = pvsCPE.getValue();
      String acsV = null;
      if (pvsDB != null) {
        acsV = pvsDB.getValue();
      }
      if (utp == null) {
        Log.debug(
            GPVDecision.class,
            "The parameter name "
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
              Log.debug(GPVDecision.class, msg);
            } else {
              String unlc = utp.getName().toLowerCase();
              String msg = null;
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
              Log.warn(GPVDecision.class, msg);
            }
          } else {
            Log.debug(
                GPVDecision.class, msg(utp, cpeV, acsV, "ACS->CPE", "Param has ReadWrite-flag"));
            if (pvsDB != null) {
              pvsDB.setType(pvsCPE.getType());
            }
            toCPE.addParameterValueStruct(pvsDB);
          }
        } else {
          Log.debug(GPVDecision.class, msg(utp, cpeV, acsV, "CPE->ACS", "Param has ReadOnly-flag"));
          toDB.add(pvsCPE);
        }
      } else if (pvsDB != null
          && pvsDB.getValue() == null
          && pvsCPE.getValue() != null
          && !pvsCPE.getValue().isEmpty()) {
        Log.debug(GPVDecision.class, msg(utp, cpeV, acsV, "CPE->ACS", "Param new to ACS"));
        toDB.add(pvsCPE);
      } else if ("ExtraCPEParam".equals(acsV)) {
        Log.debug(
            GPVDecision.class,
            msg(utp, cpeV, acsV, "No change", "Ignore ExtraCPEParam - they're treated separately"));
      } else {
        Log.debug(
            GPVDecision.class,
            msg(utp, cpeV, acsV, "No change", "Default action, the values should be equal"));
      }
    }
    // List<RequestResponseData> reqResList = sessionData.getReqResList();
    String previousMethod = sessionData.getMethodBeforePreviousResponseMethod();
    Log.debug(
        GPVDecision.class,
        "PreviousResponseMethod before deciding on log missing cpe params: " + previousMethod);
    if (TR069Method.GET_PARAMETER_VALUES.equals(previousMethod)) {
      logMissingCPEParams(sessionData);
    }
    sessionData.setToCPE(toCPE);
    sessionData.setToDB(toDB);
    // sessionData.setToSyslog(toSyslog);
    Log.debug(
        GPVDecision.class,
        toCPE.getParameterValueList().size() + " params to CPE, " + toDB.size() + " params to ACS");
  }

  /** Make sure unit parameters accurately represents CPE parameters. */
  private static void updateUnitParameters(SessionData sessionData) {
    if (sessionData.getUnit() != null && sessionData.getUnit().getUnitParameters() != null) {
      Map<String, String> parameters = sessionData.getUnit().getParameters();
      for (int i = 0;
          sessionData.getFromCPE() != null && i < sessionData.getFromCPE().size();
          i++) {
        ParameterValueStruct pvs = sessionData.getFromCPE().get(i);
        if (pvs != null && pvs.getValue() != null) {
          parameters.put(pvs.getName(), pvs.getValue());
        }
      }
    }
  }
}
