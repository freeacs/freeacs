package com.github.freeacs.tr069.methods;


import com.github.freeacs.tr069.xml.Body;

public class GRMreq extends Body {

	@Override
	public String toXmlImpl() {
		StringBuilder sb = new StringBuilder(2);
		sb.append("\t<cwmp:GetRPCMethods>\n");
		sb.append("\t</cwmp:GetRPCMethods>\n");
		return sb.toString();
	}

}
