package com.github.freeacs.tr069.xml;

import com.github.freeacs.tr069.Namespace;

public class HoldRequests {
	private boolean holdRequests;

	public HoldRequests(String holdRequests) {
		if (holdRequests == "0")
			this.holdRequests = false;
		else
			this.holdRequests = true;
	}

	public String toXml() {
		StringBuilder sb = new StringBuilder(3);
		sb.append("\t<cwmp:HoldRequests " + Namespace.getSoapEnvNS() + ":mustUnderstand=\"1\">");
		if (holdRequests)
			sb.append("1");
		else
			sb.append("0");
		sb.append("</cwmp:HoldRequests>\n");
		return sb.toString();
	}

	public void setHoldRequestsFlag(String flag) {
		if (flag == "0")
			holdRequests = false;
		else
			holdRequests = true;
	}

}
