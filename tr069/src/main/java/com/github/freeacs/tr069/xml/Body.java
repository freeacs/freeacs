package com.github.freeacs.tr069.xml;

import com.github.freeacs.tr069.Namespace;

public abstract class Body {

	public abstract String toXmlImpl();

	public String toXml() {
		StringBuilder sb = new StringBuilder();
		sb.append("<"+Namespace.getSoapEnvNS()+":Body>\n");
		sb.append(toXmlImpl());
		sb.append("</"+Namespace.getSoapEnvNS()+":Body>\n");
		return sb.toString();
	}

}
