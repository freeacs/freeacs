package com.github.freeacs.tr069.methods;

import com.github.freeacs.tr069.xml.Body;

public class INres extends Body {

	private static final String START = "\t\t<cwmp:InformResponse>\n";
	private static final String CONTENT = "\t\t\t<MaxEnvelopes>1</MaxEnvelopes>\n";
	private static final String END = "\t\t</cwmp:InformResponse>\n";

	@Override
	public String toXmlImpl() {
		StringBuilder sb = new StringBuilder(3);
		sb.append(START);
		sb.append(CONTENT);
		sb.append(END);
		return sb.toString();
	}

}
