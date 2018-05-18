package com.github.freeacs.tr069.xml;

public class SetParameterValuesFault {

	private String faultCode;
	private String faultString;
	private String parameterName;

	public SetParameterValuesFault(String faultCode, String faultString, String parameterName) {
		this.faultCode = faultCode;
		this.faultString = faultString;
		this.parameterName = parameterName;
	}

	public String toString() {
		StringBuffer str = new StringBuffer("--- SetParameterValueFault>");
		str.append("  ParameterName: " + this.parameterName);
		str.append("  FaultCode: " + this.faultCode);
		str.append("  FaultString: " + this.faultString);

		return String.valueOf(str);
	}
}
