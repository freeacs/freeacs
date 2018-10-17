package com.github.freeacs.dbi;

import java.util.Date;

public class ScriptExecution {
  private Integer id;
  private Unittype unittype;
  private File scriptFile;
  private String arguments;
  private Date requestTms;
  private String requestId;
  private Date startTms;
  private Date endTms;
  private Boolean exitStatus;
  private String errorMessage;

  public File getScriptFile() {
    return scriptFile;
  }

  public void setScriptFile(File scriptFile) {
    this.scriptFile = scriptFile;
  }

  public String getArguments() {
    return arguments;
  }

  public void setArguments(String arguments) {
    this.arguments = arguments;
  }

  public Date getRequestTms() {
    return requestTms;
  }

  public void setRequestTms(Date requestTms) {
    this.requestTms = requestTms;
  }

  public Date getStartTms() {
    return startTms;
  }

  public void setStartTms(Date startTms) {
    this.startTms = startTms;
  }

  public Date getEndTms() {
    return endTms;
  }

  public void setEndTms(Date endTms) {
    this.endTms = endTms;
  }

  /**
   * ExitStatus == null : Not completed exitStatus == 0 : OK - SUCCESS exitStatus == 1 : ERROR -
   * could expect an errorMessage
   *
   * @return
   */
  public Boolean getExitStatus() {
    return exitStatus;
  }

  public void setExitStatus(Boolean exitStatus) {
    this.exitStatus = exitStatus;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Unittype getUnittype() {
    return unittype;
  }

  public void setUnittype(Unittype unittype) {
    this.unittype = unittype;
  }

  public String getRequestId() {
    return requestId;
  }

  public void setRequestId(String requestId) {
    this.requestId = requestId;
  }
}
