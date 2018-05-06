package com.owera.tr069client.messages;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.owera.tr069client.Arguments;
import com.owera.tr069client.HttpHandler;
import com.owera.tr069client.TR069Client;
import com.owera.tr069client.monitor.Status;

public class GetParameterValues {

	public static String execute(Arguments args, HttpHandler httpHandler, Status status, TR069Client tr069Client) throws IOException {

		String req = makeRequest(httpHandler, args, tr069Client);
		return httpHandler.send(req, args, status, "GPV");
	}

	/*
	 * Håndter (antatt arbeid: 3-4 timer)
	 * 0. Parse GPV-request
	 * 1. En eller flere parametre
	 * 2. Software-version (her og i Inform)
	 */

	private static String makeRequest(HttpHandler httpHandler, Arguments args, TR069Client tr069Client) {
		Map<String, String> tmpParams = new TreeMap<String, String>(tr069Client.getParams());
		//		for (Entry<String, GPValue> entry : TR069Client.defaultParams.entrySet()) {
		//			if (tmpParams.get(entry.getKey()) == null) 
		//				tmpParams.put(entry.getKey(), entry.getValue().getValue());
		//		}
		StringBuilder msg = new StringBuilder(
				"<soapenv:Envelope soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:cwmp=\"urn:dslforum-org:cwmp-1-0\">\n");
		msg.append("\t<soapenv:Header>\n");
		msg.append("\t\t<cwmp:ID soapenv:mustUnderstand=\"1\">TEST_CLIENT_GET_PARAMETER_VALUES</cwmp:ID>\n");
		msg.append("\t\t<cwmp:HoldRequests soapenv:mustUnderstand=\"0\">1</cwmp:HoldRequests>\n");
		msg.append("\t</soapenv:Header>\n");
		msg.append("\t<soapenv:Body>\n");
		msg.append("\t\t<cwmp:GetParameterValuesResponse>\n");
		msg.append("\t\t\t<ParameterList soap:arrayType=\"cwmp:ParameterValueStruct["+tmpParams.size()+"]\">\n");
		for (Entry<String, String> entry : tmpParams.entrySet()) {
			msg.append("\t\t\t\t<ParameterValueStruct>\n");
			msg.append("\t\t\t\t\t<Name>"+entry.getKey()+"</Name>\n");
			msg.append("\t\t\t\t\t<Value xsi:type=\"xsd:string\">" + entry.getValue() + "</Value>\n");
			msg.append("\t\t\t\t</ParameterValueStruct>\n");			
		}
		msg.append("\t\t\t</ParameterList>\n");
		msg.append("\t\t</cwmp:GetParameterValuesResponse>\n");
		msg.append("\t</soapenv:Body>\n");
		msg.append("</soapenv:Envelope>\n");
		return msg.toString();
	}

}
