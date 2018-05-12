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
		sb.append("\t\t\t<ParameterNames " + Namespace.getSoapEncNS() + ":arrayType=\"xsd:string[" + parameters.size() + "]\">\n");

		for (int i = 0; i < parameters.size(); i++) {
			sb.append("\t\t\t\t<string>" + parameters.get(i).getName() + "</string>\n");
		}
		sb.append("\t\t\t</ParameterNames>\n");
		sb.append("\t\t</cwmp:GetParameterValues>\n");
		return sb.toString();
	}
}
