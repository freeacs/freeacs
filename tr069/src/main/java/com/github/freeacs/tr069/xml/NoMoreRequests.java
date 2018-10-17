package com.github.freeacs.tr069.xml;

import com.github.freeacs.tr069.Namespace;

public class NoMoreRequests {
  private boolean noMoreRequests;

  public NoMoreRequests(String noMoreRequests) {
    this.noMoreRequests = noMoreRequests != "0";
  }

  public String toXml() {
    StringBuilder sb = new StringBuilder(3);
    sb.append("\t<cwmp:NoMoreRequests ")
        .append(Namespace.getSoapEnvNS())
        .append(":mustUnderstand=\"1\">");
    if (noMoreRequests) {
      sb.append("1");
    } else {
      sb.append("0");
    }
    sb.append("</cwmp:NoMoreRequests>\n");
    return sb.toString();
  }

  public void setNoMoreRequestsFlag(String flag) {
    noMoreRequests = flag != "0";
  }

  public boolean getNoMoreRequestFlag() {
    return this.noMoreRequests;
  }
}
