package com.github.freeacs.tr069.xml;

import com.github.freeacs.tr069.Namespace;

public class Header {

	private HoldRequests holdRequests;
	private TR069TransactionID id;
	private NoMoreRequests noMoreRequests;

	public Header() {
		this.id = null;
		this.holdRequests = null;
		this.noMoreRequests = null;
	}

	public Header(TR069TransactionID id, HoldRequests hr, NoMoreRequests nmr) {
		this.id = id;
		this.holdRequests = hr;
		this.noMoreRequests = nmr;
	}

	public Header getHeader() {
		return this;
	}

	public void setHeaderField(String key, String value) {

		if (key.equals("ID")) {
			if (id != null)
				id.setId(value);
			else
				this.id = new TR069TransactionID(value);
		} else if (key.equals("NoMoreRequests")) {
			if (noMoreRequests != null)
				noMoreRequests.setNoMoreRequestsFlag(value);
			else
				this.noMoreRequests = new NoMoreRequests(value);
		} else if (key.equals("HoldRequests")) {
			if (holdRequests != null)
				holdRequests.setHoldRequestsFlag(value);
			else
				this.holdRequests = new HoldRequests(value);
		}
	}

	public String toXml() {
		StringBuilder sb = new StringBuilder(6);
		if (id != null || holdRequests != null) {
			sb.append("<" + Namespace.getSoapEnvNS() + ":Header>\n");
			if (id != null)
				sb.append(id.toXml());
			if (holdRequests != null)
				sb.append(holdRequests.toXml());
			if (noMoreRequests != null)
				sb.append(noMoreRequests.toXml());
			sb.append("</" + Namespace.getSoapEnvNS() + ":Header>\n");
		} else {
			sb.append("<" + Namespace.getSoapEnvNS() + ":Header/>\n");
		}
		return sb.toString();
	}

	public TR069TransactionID getId() {
		return id;
	}

	public HoldRequests getHoldRequests() {
		return holdRequests;
	}

	public NoMoreRequests getNoMoreRequests() {
		return noMoreRequests;
	}

}
