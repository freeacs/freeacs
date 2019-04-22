package com.github.freeacs.tr069.xml;

public abstract class Body {
  public abstract String toXmlImpl();

  public String toXml() {
    StringBuilder sb = new StringBuilder();
      sb.append("<").append("soapenv").append(":Body>\n");
    sb.append(toXmlImpl());
      sb.append("</").append("soapenv").append(":Body>\n");
    return sb.toString();
  }
}
