package com.github.freeacs.tr069.xml;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Fault {
  private String soapFaultCode;
  private String soapFaultString;
  private String faultCode;
  private String faultString;
  private List<SetParameterValuesFault> parameterFaults = new ArrayList<>();

  public Fault(String soapFaultCode, String soapFaultString, String faultCode, String faultString) {
    this.soapFaultCode = soapFaultCode;
    this.soapFaultString = soapFaultString;
    this.faultCode = faultCode;
    this.faultString = faultString;
    this.parameterFaults = new ArrayList<>();
  }

  void addParameterValuesFault(SetParameterValuesFault paramFault) {
    this.parameterFaults.add(paramFault);
  }

  public String toString() {
    StringBuilder str = new StringBuilder();
    if (this.soapFaultCode != null) {
      str.append("SOAP FaultCode       :  ").append(this.soapFaultCode).append("\n");
      str.append("SOAP FaultString     :  ").append(this.soapFaultString).append("\n");
    }
    if (this.faultCode != null) {
      str.append("FaultCode            :  ").append(this.faultCode).append("\n");
      str.append("FaultString          :  ").append(this.faultString).append("\n");
    }
    if (this.parameterFaults != null && !this.parameterFaults.isEmpty()) {
      for (SetParameterValuesFault paramFault : this.parameterFaults) {
        str.append(paramFault).append("\n");
      }
    }
    return str.toString();
  }
}
