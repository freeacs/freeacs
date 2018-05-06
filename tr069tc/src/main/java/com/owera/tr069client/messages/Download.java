package com.owera.tr069client.messages;

import java.io.IOException;

import com.owera.tr069client.Arguments;
import com.owera.tr069client.HttpHandler;
import com.owera.tr069client.OutOfConnections;
import com.owera.tr069client.monitor.Status;

public class Download {

	public static int counter;

	public static String execute(Arguments args, HttpHandler httpHandler, Status status) throws OutOfConnections, IOException {
		counter++;
		String req = makeRequest();
		String response = httpHandler.send(req, args, status, "DO");
		return response;
	}

	private static String makeRequest() {
		StringBuilder msg = new StringBuilder("<soapenv:Envelope soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" ");
		msg.append("xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" ");
		msg.append("xmlns:soap=\"http://schemas.xmlsoap.org/soap/encoding/\" ");
		msg.append("xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" ");
		msg.append("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ");
		msg.append("xmlns:cwmp=\"urn:dslforum-org:cwmp-1-0\">\n");
		msg.append("\t<soapenv:Header>\n");
		msg.append("\t\t<cwmp:ID soapenv:mustUnderstand=\"1\">TEST_CLIENT_DOWNLOAD</cwmp:ID>\n");
		msg.append("\t\t<cwmp:HoldRequests soapenv:mustUnderstand=\"0\">1</cwmp:HoldRequests>\n");
		msg.append("\t</soapenv:Header>\n");
		msg.append("\t<soapenv:Body>\n");
		msg.append("\t\t<cwmp:DownloadResponse>\n");
		msg.append("\t\t\t<Status>1</Status>\n");
		msg.append("\t\t\t<StartTime>1970-01-01T00:18:23Z</StartTime>\n");
		msg.append("\t\t\t<CompleteTime></CompleteTime>\n");
		msg.append("\t\t</cwmp:DownloadResponse>\n");
		msg.append("\t</soapenv:Body>\n");
		msg.append("</soapenv:Envelope>\n");

		return String.valueOf(msg);
	}

}
