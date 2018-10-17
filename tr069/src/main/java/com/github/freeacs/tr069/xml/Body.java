package com.github.freeacs.tr069.xml;

import com.github.freeacs.tr069.Namespace;

public abstract class Body {
  public abstract String toXmlImpl();

  public String toXml() {
    StringBuilder sb = new StringBuilder();
    sb.append("<").append(Namespace.getSoapEnvNS()).append(":Body>\n");
    sb.append(toXmlImpl());
    sb.append("</").append(Namespace.getSoapEnvNS()).append(":Body>\n");
    return sb.toString();
  }
}
