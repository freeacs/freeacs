package com.github.freeacs.tr069.xml;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * TR069SessionID is a container of the TR-069 Session ID sent from the CPE to the ACS.
 *
 * @author morten
 */
@Data
@AllArgsConstructor
public class TR069TransactionID {
  private String id;

  String toXml() {
    StringBuilder sb = new StringBuilder(3);
      sb.append("\t<cwmp:ID ").append("soapenv").append(":mustUnderstand=\"1\">");
    sb.append(id);
    sb.append("</cwmp:ID>\n");
    return sb.toString();
  }
}
