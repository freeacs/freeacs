package com.github.freeacs.tr069.xml;

import com.github.freeacs.tr069.Namespace;

/**
 * TR069SessionID is a container of the TR-069 Session ID
 * sent from the CPE to the ACS. 
 * 
 * @author morten
 *
 */
public class TR069TransactionID {

	private String id;

	public TR069TransactionID(String id) {
		this.id = id;
	}

	public String toXml() {
		StringBuilder sb = new StringBuilder(3);
		sb.append("\t<cwmp:ID " + Namespace.getSoapEnvNS() + ":mustUnderstand=\"1\">");
		sb.append(id);
		sb.append("</cwmp:ID>\n");
		return sb.toString();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
