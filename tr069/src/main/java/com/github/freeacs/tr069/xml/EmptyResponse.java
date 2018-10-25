package com.github.freeacs.tr069.xml;

public class EmptyResponse extends Response {
  public EmptyResponse(String cwmpVersionNumber) {
    super(null, null, cwmpVersionNumber);
  }

  public String toXml() {
    return "";
  }
}
