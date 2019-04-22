package com.github.freeacs.tr069.xml;

class HoldRequests {
  private boolean holdRequests;

  HoldRequests(String holdRequests) {
    this.holdRequests = !holdRequests.equals("0");
  }

  String toXml() {
    StringBuilder sb = new StringBuilder(3);
      sb.append("\t<cwmp:HoldRequests ")
        .append("soapenv")
        .append(":mustUnderstand=\"1\">");
    if (holdRequests) {
      sb.append("1");
    } else {
      sb.append("0");
    }
    sb.append("</cwmp:HoldRequests>\n");
    return sb.toString();
  }

  void setHoldRequestsFlag(String flag) {
    holdRequests = !flag.equals("0");
  }
}
