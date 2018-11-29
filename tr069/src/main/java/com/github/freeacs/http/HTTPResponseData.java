package com.github.freeacs.http;

public class HTTPResponseData {
  private String method;
  private String xml;

  protected HTTPResponseData() {

  }

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
}
