package com.github.freeacs.tr069.xml;

import java.util.ArrayList;

public class Fault {

	private String soapFaultCode;
	private String soapFaultString;
	private String faultCode;
	private String faultString;
	private ArrayList<SetParameterValuesFault> parameterFaults;

	public Fault() {
		this.soapFaultCode = null;
		this.soapFaultString = null;
		this.faultCode = null;
		this.faultString = null;
		this.parameterFaults = new ArrayList<SetParameterValuesFault>();
	}

	public Fault(String soapFaultCode, String soapFaultString, String faultCode, String faultString) {
		this.soapFaultCode = soapFaultCode;
		this.soapFaultString = soapFaultString;
		this.faultCode = faultCode;
		this.faultString = faultString;
		this.parameterFaults = new ArrayList<SetParameterValuesFault>();
	}

	public Fault(String soapFaultCode, String soapFaultString, String faultCode, String faultString, ArrayList<SetParameterValuesFault> parameterFaults) {
		this.soapFaultCode = soapFaultCode;
		this.soapFaultString = soapFaultString;
		this.faultCode = faultCode;
		this.faultString = faultString;
		this.parameterFaults = parameterFaults;
	}

	public String getSoapFaultCode() {
		return soapFaultCode;
	}

	public void setSoapFaultCode(String soapFaultCode) {
		this.soapFaultCode = soapFaultCode;
	}

	public String getSoapFaultString() {
		return soapFaultString;
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
			str.append("SOAP FaultCode       :  " + this.soapFaultCode + "\n");
			str.append("SOAP FaultString     :  " + this.soapFaultString + "\n");
		}
		if (this.faultCode != null) {
			str.append("FaultCode            :  " + this.faultCode + "\n");
			str.append("FaultString          :  " + this.faultString + "\n");
		}
		if (this.parameterFaults != null && this.parameterFaults.size() > 0) {
			for (SetParameterValuesFault paramFault : this.parameterFaults) {
				str.append(paramFault.toString() + "\n");
			}
		}
		return str.toString();
	}
}
