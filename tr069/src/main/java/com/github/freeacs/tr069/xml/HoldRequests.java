package com.github.freeacs.tr069.xml;

import com.github.freeacs.tr069.Namespace;

public class HoldRequests {
  private boolean holdRequests;

  public HoldRequests(String holdRequests) {
    this.holdRequests = holdRequests != "0";
  }

  public String toXml() {
    StringBuilder sb = new StringBuilder(3);
    sb.append("\t<cwmp:HoldRequests " + Namespace.getSoapEnvNS() + ":mustUnderstand=\"1\">");
    if (holdRequests) sb.append("1");
    else sb.append("0");
    sb.append("</cwmp:HoldRequests>\n");
    return sb.toString();
  }

  public void setHoldRequestsFlag(String flag) {
    holdRequests = flag != "0";
  }
}
