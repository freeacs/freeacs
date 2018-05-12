package com.github.freeacs.tr069.xml;

import com.github.freeacs.tr069.Namespace;

/**
 * Represents the response from the ACS to the CPE. 
 * @author morten
 *
 */
public class Response {
	private Header header;
	private Body body;

	public Response(Header header, Body body) {
		this.header = header;
		this.body = body;
	}

	public String toXml() {
		StringBuilder sb = new StringBuilder(10);
		sb.append("<" + Namespace.getSoapEnvNS() + ":Envelope ");
		sb.append("xmlns:" + Namespace.getSoapEnvNS() + "=\"http://schemas.xmlsoap.org/soap/envelope/\" ");
		sb.append("xmlns:" + Namespace.getSoapEncNS() + "=\"http://schemas.xmlsoap.org/soap/encoding/\" ");
		sb.append("xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" ");
		sb.append("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ");
		sb.append("xmlns:cwmp=\"urn:dslforum-org:cwmp-1-0\">\n");
		sb.append(header.toXml());
		sb.append(body.toXml());
		sb.append("</" + Namespace.getSoapEnvNS() + ":Envelope>\n");
		return sb.toString();
	}
}
