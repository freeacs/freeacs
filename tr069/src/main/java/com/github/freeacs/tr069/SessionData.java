package com.github.freeacs.tr069;

import com.github.freeacs.tr069.base.ACSParameters;
import com.github.freeacs.tr069.base.PIIDecision;
import com.github.freeacs.tr069.base.SessionDataI;
import com.github.freeacs.dbi.File;
import com.github.freeacs.dbi.Job;
import com.github.freeacs.dbi.JobParameter;
import com.github.freeacs.dbi.Profile;
import com.github.freeacs.dbi.Unit;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.dbi.Unittype.ProvisioningProtocol;
import com.github.freeacs.dbi.util.ProvisioningMessage;
import com.github.freeacs.dbi.util.SystemParameters;
import com.github.freeacs.tr069.http.HTTPRequestResponseData;
import com.github.freeacs.tr069.xml.ParameterList;
import com.github.freeacs.tr069.xml.ParameterValueStruct;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Slf4j
public class SessionData implements SessionDataI {
  private static final Logger LOGGER = LoggerFactory.getLogger(SessionData.class);

  /** The session-id. */
  private String id;
  /** Data for monitoring/logging. */
  private List<HTTPRequestResponseData> reqResList = new ArrayList<>();
  /** When did the session start? */
  private Long startupTmsForSession;

  /** The unique id for the CPE. */
  private String unitId;
  /** The unit-object. */
  private Unit unit;
  /** The profile name for this CPE (defined i the DB). */
  private Profile profile;
  /** The unittype for this CPE (defined in the DB). */
  private Unittype unittype;
  /** The keyroot of this CPE (e.g. InternetGatewayDevice.) */
  private String keyRoot;

  private String serialNumber;

  /** Tells whether the CPE is doing a periodic inform or not. */
  private boolean periodic;
  /* other event codes */
  private boolean factoryReset;
  private boolean valueChange;
  private boolean kicked;
  private boolean transferComplete;
  private boolean autonomousTransferComplete;
  private boolean diagnosticsComplete;
  private boolean booted;

  /** Tells whether a job is under execution - important not to start on another job. */
  private boolean jobUnderExecution;
  /** The event code of the inform. */
  private String eventCodes;

  /** Owera parameters. */
  private ACSParameters acsParameters;
  /** Special parameters, will always be retrieved. */
  private CPEParameters cpeParameters;
  /** Special parameter, will only be retrieved from the Inform. */
  private InformParameters informParameters;

  /** All parameters found in the DB, except system parameters (X). */
  private Map<String, ParameterValueStruct> fromDB;
  /** All parameters read from the CPE. */
  private List<ParameterValueStruct> valuesFromCPE;
  /** All parameters that shall be written to the CPE. */
  private ParameterList toCPE;
  /** All parameters that shall be written to the DB. */
  private List<ParameterValueStruct> toDB;
  /** All parameters requested from CPE. */
  private List<ParameterValueStruct> requestedCPE;

  /** Job. */
  private Job job;
  /** All parameters from a job. */
  private Map<String, JobParameter> jobParams;

  /** Parameterkey contains a hash of all values sent to CPE. */
  private ParameterKey parameterKey;
  /** Commandkey contains the version number of the last download - if a download was sent. */
  private CommandKey commandKey;
  /** Provisioning allowed. False if outside servicewindow or not allowed by unitJob */
  private boolean provisioningAllowed = true;

  /** The secret obtained by discovery-mode, basic auth. */
  private String secret;
  /** The flag signals a first-time connect in discovery-mode. */
  private boolean firstConnect;
  /** Unittype has been created, but unitId remains unknown, only for discovery-mode. */
  private boolean unittypeCreated = true;

  /** PIIDecision is important to decide the final outcome of the next Periodic Inform Interval. */
  private PIIDecision piiDecision;

  /** An object to store all kinds of data about the provisioning. */
  private ProvisioningMessage provisioningMessage = new ProvisioningMessage();

  /** An object to store data about a download. */
  private Download download;

  private String cwmpVersionNumber;

  public SessionData(String id) {
    this.id = id;
    provisioningMessage.setProvProtocol(ProvisioningProtocol.TR069);
  }

  public void setKeyRoot(String keyRoot) {
    if (keyRoot != null) {
      this.keyRoot = keyRoot;
    }
  }

  public void setUnitId(String unitId) {
    if (unitId != null) {
      this.unitId = unitId;
      this.provisioningMessage.setUniqueId(unitId);
    }
  }

  public void setNoMoreRequests(boolean noMoreRequests) {
    LOGGER.warn("Setting unused noMoreRequests field to " + noMoreRequests);
  }

  public void setToDB(List<ParameterValueStruct> toDB) {
    if (toDB == null) {
      toDB = new ArrayList<>();
    }
    this.toDB = toDB;
  }

  public String getMethodBeforePreviousResponseMethod() {
    if (reqResList != null && reqResList.size() > 2) {
      return reqResList.get(reqResList.size() - 3).getResponseData().getMethod();
    } else {
      return null;
    }
  }

  public String getPreviousResponseMethod() {
    if (reqResList != null && reqResList.size() > 1) {
      return reqResList.get(reqResList.size() - 2).getResponseData().getMethod();
    } else {
      return null;
    }
  }

  public void setEventCodes(String eventCodes) {
    this.eventCodes = eventCodes;
    this.provisioningMessage.setEventCodes(eventCodes);
  }

  public String getSoftwareVersion() {
    CPEParameters cpeParams = getCpeParameters();
    if (cpeParams != null) {
      return cpeParams.getValue(cpeParams.SOFTWARE_VERSION);
    }
    return null;
  }

  public void setSoftwareVersion(String softwareVersion) {
    CPEParameters cpeParams = getCpeParameters();
    if (cpeParams != null) {
      cpeParams.getCpeParams().put(
          cpeParams.SOFTWARE_VERSION,
          new ParameterValueStruct(cpeParams.SOFTWARE_VERSION, softwareVersion));
    }
  }

  public boolean lastProvisioningOK() {
    return getParameterKey().isEqual() && getCommandKey().isEqual();
  }

  @Override
  public PIIDecision getPIIDecision() {
    if (piiDecision == null) {
      piiDecision = new PIIDecision(this);
    }
    return piiDecision;
  }

  public boolean discoverUnittype() {
    if (acsParameters != null
        && acsParameters.getValue(SystemParameters.DISCOVER) != null
        && "1".equals(acsParameters.getValue(SystemParameters.DISCOVER))) {
      return true;
    } else if (acsParameters == null) {
      log.debug("freeacsParameters not found in discoverUnittype()");
    } else if (acsParameters.getValue(SystemParameters.DISCOVER) != null) {
      log.debug("DISCOVER parameter value is "
              + acsParameters.getValue(SystemParameters.DISCOVER)
              + " in discoverUnittype()");
    } else {
      log.debug("DISCOVER parameter not found of value is null in discoverUnittype() ");
    }
    return false;
  }

  String getUnittypeName() {
    String unittypeName = null;
    if (unittype != null) {
      unittypeName = unittype.getName();
    }
    return unittypeName;
  }

  public String getVersion() {
    String version = null;
    if (cpeParameters != null) {
      version = cpeParameters.getValue(cpeParameters.SOFTWARE_VERSION);
    }
    return version;
  }

  @Data
  @AllArgsConstructor
  public static class Download {
    private String url;
    private File file;
  }
}
