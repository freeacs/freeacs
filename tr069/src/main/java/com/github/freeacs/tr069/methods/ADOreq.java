package com.github.freeacs.tr069.methods;

import com.github.freeacs.tr069.xml.Body;

public class ADOreq extends Body {

	private String objectName, parameterKey;

	public ADOreq(String objectName, String parameterKey) {
		this.objectName = objectName;
		this.parameterKey = parameterKey;
	}

	public String toXmlImpl() {
		StringBuilder sb = new StringBuilder(3);
		sb.append("\t\t<cwmp:AddObject>\n");
		sb.append("\t\t\t<ObjectName>" + objectName +  "</ObjectName>\n");
		sb.append("\t\t\t<ParameterKey>" + parameterKey + "</ParameterKey>\n");
		sb.append("\t\t</cwmp:AddObject>\n");
		return sb.toString();
	}
}
