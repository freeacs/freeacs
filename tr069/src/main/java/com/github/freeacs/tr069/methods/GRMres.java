package com.github.freeacs.tr069.methods;

import com.github.freeacs.tr069.Namespace;
import com.github.freeacs.tr069.xml.Body;

public class GRMres extends Body {

	@Override
	public String toXmlImpl() {
		StringBuilder sb = new StringBuilder(3);
		sb.append("\t<cwmp:GetRPCMethodsResponse>\n");
		sb.append("\t\t<MethodList "+Namespace.getSoapEncNS()+":arrayType=\"xsd:string[3]\">\n");
		sb.append("\t\t\t<string>GetRPCMethods</string>\n");
		sb.append("\t\t\t<string>Inform</string>\n");
		sb.append("\t\t\t<string>TransferComplete</string>\n");
		sb.append("\t\t</MethodList>\n");
		sb.append("\t</cwmp:GetRPCMethodsResponse>\n");
		return sb.toString();
	}

}
