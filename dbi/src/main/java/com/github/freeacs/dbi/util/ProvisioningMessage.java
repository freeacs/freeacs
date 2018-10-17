package com.github.freeacs.dbi.util;

import com.github.freeacs.dbi.Syslog;
import com.github.freeacs.dbi.SyslogConstants;
import com.github.freeacs.dbi.Unittype.ProvisioningProtocol;
import java.util.Date;

public class ProvisioningMessage {
  public enum ProvStatus {
    OK("OK"),
    DELAYED("DELAYED"),
    ERROR("ERROR");

    private String str;

    ProvStatus(String str) {
      this.str = str;
    }

    public String getStr() {
      return str;
    }
  }

  public enum ErrorResponsibility {
    CLIENT("CLIENT"),
    SERVER("SERVER");
    private String str;

    ErrorResponsibility(String str) {
      this.str = str;
    }

    public String getStr() {
      return str;
    }
  }

  public enum ProvOutput {
    EMPTY,
    CONFIG,
    SOFTWARE,
    SCRIPT,
    REBOOT,
    RESET,
    SHELL
  }

  private ProvisioningProtocol provProtocol;
  private ProvStatus provStatus;
  private ErrorResponsibility errorResponsibility;
  private Integer errorCode;
  private String errorMessage;
  private ProvOutput provOutput;
  private Integer jobId;
  private Integer paramsRead;
  private Integer paramsWritten;
  private int sessionLength;
  private String fileVersion;
  private String eventCodes;
  private ProvisioningMode provMode;
  private Integer periodicInformInterval;
  private String uniqueId;
  private String ipAddress;

  public void setProvStatus(ProvStatus ps) {
    this.provStatus = ps;
  }

  public void setProvOutput(ProvOutput o) {
    this.provOutput = o;
  }

  public void setSessionLength(int sl) {
    this.sessionLength = sl;
  }

  public void setErrorResponsibility(ErrorResponsibility er) {
    this.errorResponsibility = er;
  }

  public void setErrorCode(int ec) {
    this.errorCode = ec;
  }

  public void setErrorMessage(String em) {
    if (em != null && em.length() > 250) {
      em = em.substring(0, 250);
    }
    this.errorMessage = em;
  }

  public void setErrorMessage(String em, Throwable t) {
    StringBuilder message = new StringBuilder();
    if (t != null) {
      message.append(t.getClass());
      message.append(":");
      message.append(t.getMessage());
      message.append("\n");
      StackTraceElement[] steArr = t.getStackTrace();
      for (StackTraceElement ste : steArr) {
        message.append("\t");
        message.append(ste);
        message.append("\n");
      }
      em += " " + message;
    }
    setErrorMessage(em);
  }

  public void setJobId(Integer ji) {
    this.jobId = ji;
  }

  public void setParamsRead(Integer pr) {
    this.paramsRead = pr;
  }

  public void setParamsWritten(Integer pw) {
    this.paramsWritten = pw;
  }

  public void setEventCodes(String evc) {
    this.eventCodes = evc;
  }

  public void setProvMode(ProvisioningMode pm) {
    this.provMode = pm;
  }

  public void setPeriodicInformInterval(Integer pii) {
    this.periodicInformInterval = pii;
  }

  public void setFileVersion(String fileVersion) {
    this.fileVersion = fileVersion;
  }

  public void setProvProtocol(ProvisioningProtocol provProtocol) {
    this.provProtocol = provProtocol;
  }

  public Integer getErrorCode() {
    return errorCode;
  }

  public void setErrorCode(Integer errorCode) {
    this.errorCode = errorCode;
  }

  public ProvisioningProtocol getProvProtocol() {
    return provProtocol;
  }

  public ProvStatus getProvStatus() {
    return provStatus;
  }

  public ErrorResponsibility getErrorResponsibility() {
    return errorResponsibility;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public ProvOutput getProvOutput() {
    return provOutput;
  }

  public Integer getJobId() {
    return jobId;
  }

  public Integer getParamsRead() {
    return paramsRead;
  }

  public Integer getParamsWritten() {
    return paramsWritten;
  }

  public int getSessionLength() {
    return sessionLength;
  }

  public String getFileVersion() {
    return fileVersion;
  }

  public String getEventCodes() {
    return eventCodes;
  }

  public ProvisioningMode getProvMode() {
    return provMode;
  }

  public Integer getPeriodicInformInterval() {
    return periodicInformInterval;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

  public void setUniqueId(String uniqueId) {
    this.uniqueId = uniqueId;
  }

  public String syslogMsg(Syslog syslog) {
    return syslogMsg(
        syslog.getIdentity().getFacility(),
        syslog.getIdentity().getFacilityVersion(),
        syslog.getIdentity().getUser().getUsername());
  }

  public String syslogMsg(int facility, String facilityVersion, String user) {
    int severity = SyslogConstants.SEVERITY_NOTICE;
    if (provStatus == ProvStatus.ERROR) {
      severity = SyslogConstants.SEVERITY_ERROR;
    } else if (provStatus == ProvStatus.DELAYED) {
      severity = SyslogConstants.SEVERITY_WARNING;
    }
    return SyslogClient.makeMessage(
        severity,
        new Date(),
        ipAddress,
        uniqueId,
        "ProvMsg: " + this,
        facility,
        facilityVersion,
        user);
  }

  /** Sent to log-file. */
  public String logMsg() {
    return toString();
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("PP:").append(provProtocol).append(", ");
    sb.append("ST:").append(provStatus.getStr()).append(", ");
    sb.append("PO:").append(provOutput).append(", ");
    sb.append("SL:").append(sessionLength).append(", ");
    if (paramsRead != null) {
      sb.append("PR:").append(paramsRead).append(", ");
    }
    if (paramsWritten != null) {
      sb.append("PW:").append(paramsWritten).append(", ");
    }
    if (periodicInformInterval != null) {
      sb.append("PI:").append(periodicInformInterval).append(", ");
    }
    if (jobId != null) {
      sb.append("JO:").append(jobId).append(", ");
    }
    if (eventCodes != null) {
      sb.append("EV:").append(eventCodes).append(", ");
    }
    if (provMode != null) {
      sb.append("PM:").append(provMode).append(", ");
    }
    if (fileVersion != null) {
      sb.append("FV:").append(fileVersion).append(", ");
    }
    if (errorResponsibility != null) {
      sb.append("ER:").append(errorResponsibility).append(", ");
    }
    if (errorCode != null) {
      sb.append("EC:").append(errorCode).append(", ");
    }
    if (errorMessage != null) {
      sb.append("EM:").append(errorMessage);
    }
    String str = sb.toString().trim();
    if (str.endsWith(",")) {
      str = str.substring(0, str.length() - 1);
    }
    return str;
  }
}
