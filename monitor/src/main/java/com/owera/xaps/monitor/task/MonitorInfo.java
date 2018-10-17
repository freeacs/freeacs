package com.owera.xaps.monitor.task;

public class MonitorInfo implements Comparable<MonitorInfo> {
  private String module;
  private String status;
  private String url;
  private String version;
  private String errorMessage;

  public MonitorInfo(String module) {
    this.module = module;
  }

  public String getModule() {
    return module;
  }

  public String getStatus() {
    return status;
  }

  public String getUrl() {
    return url;
  }

  public String getVersion() {
    return version;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setModule(String module) {
    this.module = module;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  @Override
  public int compareTo(MonitorInfo o) {
    if (o != null) {
      return getModule().compareTo(o.getModule());
    }
    return 0;
  }
}
