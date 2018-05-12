package com.github.freeacs.tr069.xml;

public class SetParameterValuesFault {

	private String faultCode;
	private String faultString;
	private String parameterName;

	public SetParameterValuesFault() {
		this.faultCode = null;
		this.faultString = null;
		this.parameterName = null;
	}

	public SetParameterValuesFault(String faultCode, String faultString, String parameterName) {
		this.faultCode = faultCode;
		this.faultString = faultString;
		this.parameterName = parameterName;
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

	public String getParameterName() {
		return parameterName;
	}

	public void setParameterName(String parameterName) {
		this.parameterName = parameterName;
	}

	public void print() {
		System.out.println(this.toString());
	}

	public String toString() {
		StringBuffer str = new StringBuffer("--- SetParameterValueFault>");
		str.append("  ParameterName: " + this.parameterName);
		str.append("  FaultCode: " + this.faultCode);
		str.append("  FaultString: " + this.faultString);

		return String.valueOf(str);
	}
}
