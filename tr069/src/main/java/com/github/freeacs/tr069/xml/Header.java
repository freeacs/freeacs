package com.github.freeacs.tr069.xml;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Header {
  private TR069TransactionID id;
  private HoldRequests holdRequests;
  private NoMoreRequests noMoreRequests;

  public Header getHeader() {
    return this;
  }

  void setHeaderField(String key, String value) {
    if ("ID".equals(key)) {
      if (id != null) {
        id.setId(value);
      } else {
        this.id = new TR069TransactionID(value);
      }
    } else if ("NoMoreRequests".equals(key)) {
      if (noMoreRequests != null) {
        noMoreRequests.setNoMoreRequestsFlag(value);
      } else {
        this.noMoreRequests = new NoMoreRequests(value);
      }
    } else if ("HoldRequests".equals(key)) {
      if (holdRequests != null) {
        holdRequests.setHoldRequestsFlag(value);
      } else {
        this.holdRequests = new HoldRequests(value);
      }
    }
  }

  String toXml() {
    StringBuilder sb = new StringBuilder(6);
    if (id != null || holdRequests != null) {
      sb.append("<").append("soapenv").append(":Header>\n");
      if (id != null) {
        sb.append(id.toXml());
      }
      if (holdRequests != null) {
        sb.append(holdRequests.toXml());
      }
      if (noMoreRequests != null) {
        sb.append(noMoreRequests.toXml());
      }
      sb.append("</").append("soapenv").append(":Header>\n");
    } else {
      sb.append("<").append("soapenv").append(":Header/>\n");
    }
    return sb.toString();
  }
}
