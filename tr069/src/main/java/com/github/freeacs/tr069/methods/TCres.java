package com.owera.xaps.tr069.methods;

import com.owera.xaps.tr069.xml.Body;

public class TCres extends Body {

	private static final String BODY = "\t\t<cwmp:TransferCompleteResponse />\n";

	@Override
	public String toXmlImpl() {
		return BODY;
	}

}
