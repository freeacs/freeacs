package com.github.freeacs.tr069.xml;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SetParameterValuesFault {
  private String faultCode;
  private String faultString;
  private String parameterName;

  public String toString() {
    StringBuffer str = new StringBuffer("--- SetParameterValueFault>");
    str.append("  ParameterName: ").append(this.parameterName);
    str.append("  FaultCode: ").append(this.faultCode);
    str.append("  FaultString: ").append(this.faultString);

    return String.valueOf(str);
  }
}
