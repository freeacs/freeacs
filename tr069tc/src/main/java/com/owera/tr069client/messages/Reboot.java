package com.owera.tr069client.messages;

import java.io.IOException;

import com.owera.tr069client.Arguments;
import com.owera.tr069client.HttpHandler;
import com.owera.tr069client.OutOfConnections;
import com.owera.tr069client.monitor.Status;

public class Reboot {

	public static String execute(Arguments args, HttpHandler httpHandler, Status status) throws OutOfConnections,
			IOException {
		return httpHandler.send(makeRequest(), args, status, "RE");
	}

	private static String makeRequest() {
		StringBuilder message = new StringBuilder(
				"<soapenv:Envelope soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:cwmp=\"urn:dslforum-org:cwmp-1-0\">\n");
		message.append("\t<soapenv:Header>\n");
		message.append("\t\t<cwmp:ID soapenv:mustUnderstand=\"1\">TEST_CLIENT_INFORM</cwmp:ID>\n");
		message.append("\t\t<cwmp:HoldRequests soapenv:mustUnderstand=\"0\">0</cwmp:HoldRequests>\n");
		message.append("\t</soapenv:Header>\n");
		message.append("\t<soapenv:Body>\n");
		message.append("\t\t<cwmp:RebootResponse>\n");
		message.append("\t\t</cwmp:RebootResponse>\n");
		message.append("\t</soapenv:Body>\n");
		message.append("</soapenv:Envelope>\n");
		return message.toString();

	}

}
