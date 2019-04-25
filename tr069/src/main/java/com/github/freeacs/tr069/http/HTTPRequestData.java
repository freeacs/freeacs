package com.github.freeacs.tr069.http;

import com.github.freeacs.tr069.xml.Fault;

public class HTTPRequestData {
  private String method;
  private String xml;
  private Fault fault;
  private String contextPath;

  public String getXml() {
    return xml;
  }

  public void setXml(String xml) {
    this.xml = xml;
  }

  public String getMethod() {
    return method;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  public Fault getFault() {
    return fault;
  }

  public void setFault(Fault fault) {
    this.fault = fault;
  }

  public void setContextPath(String contextPath) {
    this.contextPath = contextPath;
  }

  public String getContextPath() {
    return contextPath;
  }
}
