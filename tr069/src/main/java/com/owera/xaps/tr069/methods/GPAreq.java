package com.owera.xaps.tr069.methods;

import java.util.List;

import com.owera.xaps.tr069.Namespace;
import com.owera.xaps.tr069.xml.Body;
import com.owera.xaps.tr069.xml.ParameterAttributeStruct;

public class GPAreq extends Body {

	private List<ParameterAttributeStruct> parameters;

	/**
	 * Valid XML: 
	 * <cwmp:GetParameterAttributes>
 	 *  <ParameterNames soapenc:arrayType="xsd:string[1]">
    *    <string>InternetGatewayDevice.WANDevice.1.WANConnectionDevice.1.WANIPConnection.1.</string>
    *  </ParameterNames>
    * </cwmp:GetParameterAttributes>
	 */
	
	public GPAreq(List<ParameterAttributeStruct> parameters) {
		this.parameters = parameters;
	}

	public String toXmlImpl() {
		StringBuilder sb = new StringBuilder(3);
		sb.append("\t\t<cwmp:GetParameterAttributes>\n");
		sb.append("\t\t\t<ParameterNames " + Namespace.getSoapEncNS() + ":arrayType=\"xsd:string[" + parameters.size() + "]\">\n");
		for (int i = 0; i < parameters.size(); i++) {
			sb.append("\t\t\t\t<string>" + parameters.get(i).getName() + "</string>\n");
		}
		sb.append("\t\t\t</ParameterNames>\n");
		sb.append("\t\t</cwmp:GetParameterAttributes>\n");
		return sb.toString();
	}
}
