package com.github.freeacs.tr069.xml;

import java.util.ArrayList;
import java.util.List;

public class Fault {

  private String soapFaultCode;
  private String soapFaultString;
  private String faultCode;
  private String faultString;
  private List<SetParameterValuesFault> parameterFaults;

  public Fault() {
    this.soapFaultCode = null;
    this.soapFaultString = null;
    this.faultCode = null;
    this.faultString = null;
    this.parameterFaults = new ArrayList<>();
  }

  public Fault(String soapFaultCode, String soapFaultString, String faultCode, String faultString) {
    this.soapFaultCode = soapFaultCode;
    this.soapFaultString = soapFaultString;
    this.faultCode = faultCode;
    this.faultString = faultString;
    this.parameterFaults = new ArrayList<>();
  }

  public Fault(
      String soapFaultCode,
      String soapFaultString,
      String faultCode,
      String faultString,
      List<SetParameterValuesFault> parameterFaults) {
    this.soapFaultCode = soapFaultCode;
    this.soapFaultString = soapFaultString;
    this.faultCode = faultCode;
    this.faultString = faultString;
    this.parameterFaults = parameterFaults;
  }

  public void setSoapFaultCode(String soapFaultCode) {
    this.soapFaultCode = soapFaultCode;
  }

  public void setSoapFaultString(String soapFaultString) {
    this.soapFaultString = soapFaultString;
  }

  public String getFaultCode() {
    return faultCode;
  }

  public void setFaultCode(String faultCode) {
    this.faultCode = faultCode;
  }

  public String getFaultString() {
    return faultString;
  }

  public void setFaultString(String faultString) {
    this.faultString = faultString;
  }

  public void addParameterValuesFault(SetParameterValuesFault paramFault) {
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
    if (this.parameterFaults != null && this.parameterFaults.size() > 0) {
      for (SetParameterValuesFault paramFault : this.parameterFaults) {
        str.append(paramFault.toString()).append("\n");
      }
    }
    return str.toString();
  }
}
