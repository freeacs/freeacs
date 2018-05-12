package com.github.freeacs.tr069.methods;


import com.github.freeacs.tr069.xml.Body;

public class FRreq extends Body {

	@Override
	public String toXmlImpl() {
		StringBuilder sb = new StringBuilder(3);
        sb.append("\t<cwmp:FactoryReset xmlns:cwmp=\"urn:dslforum-org:cwmp-1-0\">\n");
        sb.append("\t</cwmp:FactoryReset>\n");
		return sb.toString();
	}

}
