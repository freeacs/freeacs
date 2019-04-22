package com.github.freeacs.tr069.xml;

import java.util.Optional;

/**
 * Represents the response from the ACS to the CPE.
 *
 * @author morten
 */
public class Response {
  private final Header header;
  private final Body body;
  private final String cwmpVersionNumber;

  public Response(Header header, Body body, String cwmpVersionNumber) {
    this.header = header;
    this.body = body;
    this.cwmpVersionNumber = cwmpVersionNumber;
  }

  public String toXml() {
    final StringBuilder sb = new StringBuilder(10);
      sb.append("<").append("soapenv").append(":Envelope ");
      sb.append("xmlns:")
        .append("soapenv")
        .append("=\"http://schemas.xmlsoap.org/soap/envelope/\" ");
    sb.append("xmlns:")
        .append("soapenc")
        .append("=\"http://schemas.xmlsoap.org/soap/encoding/\" ");
    sb.append("xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" ");
    sb.append("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ");
    sb.append("xmlns:cwmp=\"urn:dslforum-org:cwmp-")
        .append(Optional.ofNullable(cwmpVersionNumber).orElse("1-0"))
        .append("\">\n");
    sb.append(header.toXml());
    sb.append(body.toXml());
      sb.append("</").append("soapenv").append(":Envelope>\n");
    return sb.toString();
  }
}
