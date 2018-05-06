package com.owera.tr069client.messages;

import java.io.IOException;

import com.owera.tr069client.Arguments;
import com.owera.tr069client.HttpHandler;
import com.owera.tr069client.monitor.Status;

public class SetParameterValues {

	public static String execute(Arguments args, HttpHandler httpHandler, Status status) throws IOException {
		String req = makeRequest(args, httpHandler);
		return httpHandler.send(req, args, status, "SPV");
	}

	private static String makeRequest(Arguments args, HttpHandler httpHandler) {
		StringBuilder msg = new StringBuilder(
				"<soapenv:Envelope soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:cwmp=\"urn:dslforum-org:cwmp-1-0\">\n");

		msg.append("\t<soapenv:Header>\n");
		msg.append("\t\t<cwmp:ID soapenv:mustUnderstand=\"1\">TEST_CLIENT_SET_PARAMETER_VALUES</cwmp:ID>\n");
		msg.append("\t\t<cwmp:HoldRequests soapenv:mustUnderstand=\"0\">1</cwmp:HoldRequests>\n");
		msg.append("\t</soapenv:Header>\n");
		msg.append("\t<soapenv:Body>\n");
		if (args.getFailureEvery() > 0 && httpHandler.getSerialNumberInt() % args.getFailureEvery() == 0) {
			msg.append("\t\t<soap:Fault>\n");
			msg.append("\t\t\t<faultcode>Client</faultcode>\n");
			msg.append("\t\t\t<faultstring>CWMP fault</faultstring>\n");
			msg.append("\t\t\t\t<detail>\n");
			msg.append("\t\t\t\t\t<cwmp:Fault>\n");
			msg.append("\t\t\t\t\t\t<FaultCode>8003</FaultCode>\n");
			msg.append("\t\t\t\t\t\t<FaultString>Unexpected characters</FaultString>\n");
			msg.append("\t\t\t\t\t</cwmp:Fault>\n");
			msg.append("\t\t\t\t</detail>\n");     
			msg.append("\t\t</soap:Fault>\n");
		} else {
			msg.append("\t\t<cwmp:SetParameterValuesResponse>\n");
			msg.append("\t\t\t<Status>1</Status>\n");
			msg.append("\t\t</cwmp:SetParameterValuesResponse>\n");
		}
		msg.append("\t</soapenv:Body>\n");
		msg.append("</soapenv:Envelope>\n");

		return String.valueOf(msg);
	}

}
