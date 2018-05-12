package com.github.freeacs.tr069.xml;

public class EmptyResponse extends Response {

	public EmptyResponse() {
		super(null, null);
	}

	public String toXml() {
		return "";
	}
}
