package com.github.freeacs.tr069.methods;

import com.github.freeacs.tr069.xml.Body;

public class REreq extends Body {

	@Override
	public String toXmlImpl() {
		StringBuilder sb = new StringBuilder(3);
        sb.append("\t<cwmp:Reboot xmlns:cwmp=\"urn:dslforum-org:cwmp-1-0\">\n");
        sb.append("\t\t<CommandKey>Reboot_FREEACS-"+System.currentTimeMillis()+"</CommandKey>\n");
        sb.append("\t</cwmp:Reboot>\n");
		return sb.toString();
	}

}
