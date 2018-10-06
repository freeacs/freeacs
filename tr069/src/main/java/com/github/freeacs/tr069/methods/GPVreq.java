package com.github.freeacs.tr069.methods;

import com.github.freeacs.tr069.Namespace;
import com.github.freeacs.tr069.xml.Body;
import com.github.freeacs.tr069.xml.ParameterValueStruct;
import java.util.List;

public class GPVreq extends Body {

  private List<ParameterValueStruct> parameters;

  public GPVreq(List<ParameterValueStruct> parameters) {
    this.parameters = parameters;
  }

  public String toXmlImpl() {
    StringBuilder sb = new StringBuilder(3);
    sb.append("\t\t<cwmp:GetParameterValues>\n");
    sb.append("\t\t\t<ParameterNames ")
        .append(Namespace.getSoapEncNS())
        .append(":arrayType=\"xsd:string[")
        .append(parameters.size())
        .append("]\">\n");

    for (ParameterValueStruct parameter : parameters) {
      sb.append("\t\t\t\t<string>").append(parameter.getName()).append("</string>\n");
    }
    sb.append("\t\t\t</ParameterNames>\n");
    sb.append("\t\t</cwmp:GetParameterValues>\n");
    return sb.toString();
  }
}
