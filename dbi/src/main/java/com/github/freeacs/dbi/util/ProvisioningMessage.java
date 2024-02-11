package com.github.freeacs.dbi.util;

import com.github.freeacs.dbi.SyslogConstants;
import com.github.freeacs.dbi.Unittype.ProvisioningProtocol;
import lombok.Data;
import lombok.Getter;

import java.util.Date;

@Data
public class ProvisioningMessage {
  @Getter
  public enum ProvStatus {
    OK("OK"),
    DELAYED("DELAYED"),
    ERROR("ERROR");

    private final String str;

    ProvStatus(String str) {
      this.str = str;
    }

  }

  @Getter
  public enum ErrorResponsibility {
    CLIENT("CLIENT"),
    SERVER("SERVER");
    private final String str;

    ErrorResponsibility(String str) {
      this.str = str;
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

  public void setErrorMessage(String em) {
    if (em != null && em.length() > 250) {
      em = em.substring(0, 250);
    }
    this.errorMessage = em;
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
}
