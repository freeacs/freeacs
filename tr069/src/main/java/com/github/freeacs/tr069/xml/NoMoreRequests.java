package com.github.freeacs.tr069.xml;

public class NoMoreRequests {
  private boolean noMoreRequests;

  NoMoreRequests(String noMoreRequests) {
    this.noMoreRequests = !noMoreRequests.equals("0");
  }

  String toXml() {
    StringBuilder sb = new StringBuilder(3);
      sb.append("\t<cwmp:NoMoreRequests ")
        .append("soapenv")
        .append(":mustUnderstand=\"1\">");
    if (noMoreRequests) {
      sb.append("1");
    } else {
      sb.append("0");
    }
    sb.append("</cwmp:NoMoreRequests>\n");
    return sb.toString();
  }

  void setNoMoreRequestsFlag(String flag) {
    noMoreRequests = !flag.equals("0");
  }

  public boolean getNoMoreRequestFlag() {
    return this.noMoreRequests;
  }
}
