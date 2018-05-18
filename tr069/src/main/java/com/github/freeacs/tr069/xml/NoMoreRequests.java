package com.github.freeacs.tr069.xml;

import com.github.freeacs.tr069.Namespace;

public class NoMoreRequests {
	private boolean noMoreRequests;

	public NoMoreRequests(String noMoreRequests) {
		if (noMoreRequests == "0")
			this.noMoreRequests = false;
		else
			this.noMoreRequests = true;
	}

	public String toXml() {
		StringBuilder sb = new StringBuilder(3);
		sb.append("\t<cwmp:NoMoreRequests " + Namespace.getSoapEnvNS() + ":mustUnderstand=\"1\">");
		if (noMoreRequests)
			sb.append("1");
		else
			sb.append("0");
		sb.append("</cwmp:NoMoreRequests>\n");
		return sb.toString();
	}

	public void setNoMoreRequestsFlag(String flag) {
		if (flag == "0")
			noMoreRequests = false;
		else
			noMoreRequests = true;
	}

	public boolean getNoMoreRequestFlag() {
		return this.noMoreRequests;
	}
}
