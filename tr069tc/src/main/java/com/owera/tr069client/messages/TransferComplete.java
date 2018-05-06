package com.owera.tr069client.messages;

import java.io.IOException;

import com.owera.tr069client.Arguments;
import com.owera.tr069client.HttpHandler;
import com.owera.tr069client.OutOfConnections;
import com.owera.tr069client.monitor.Status;

public class TransferComplete {

	public static String execute(Arguments args, HttpHandler httpHandler, Status status) throws OutOfConnections, IOException {
		String req = makeRequest(args, httpHandler);
		return httpHandler.send(req, args, status, "TC");
	}

	private static String makeRequest(Arguments args, HttpHandler httpHandler) {
		StringBuffer msg = new StringBuffer("<soapenv:Envelope soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" ");
		msg.append("xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" ");
		msg.append("xmlns:soap=\"http://schemas.xmlsoap.org/soap/encoding/\" ");
		msg.append("xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" ");
		msg.append("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ");
		msg.append("xmlns:cwmp=\"urn:dslforum-org:cwmp-1-0\">\n");
		msg.append("\t<soapenv:Header>\n");
		msg.append("\t\t<cwmp:ID soapenv:mustUnderstand=\"1\">TEST_CLIENT_TRANSFER_COMPLETE</cwmp:ID>\n");
		msg.append("\t\t<cwmp:HoldRequests soapenv:mustUnderstand=\"0\">1</cwmp:HoldRequests>\n");
		msg.append("\t</soapenv:Header>\n");
		msg.append("\t<soapenv:Body>\n");
		msg.append("\t\t<cwmp:TransferComplete>\n");
		msg.append("\t\t\t<CommandKey>Download_To_CPE_</CommandKey>\n");
		msg.append("\t\t\t<FaultStruct>\n");
		if (args.getFailureEvery() > 0 && httpHandler.getSerialNumberInt() % args.getFailureEvery() == 0)
			msg.append("\t\t\t\t<FaultCode>9</FaultCode>\n");
		else
			msg.append("\t\t\t\t<FaultCode>0</FaultCode>\n");
		msg.append("\t\t\t\t<FaultString>No Fault</FaultString>\n");
		msg.append("\t\t\t</FaultStruct>\n");
		msg.append("\t\t\t<StartTime>1970-01-01T00:18:23Z</StartTime>\n");
		msg.append("\t\t\t<CompleteTime>1970-01-01T00:18:24Z</CompleteTime>\n");
		msg.append("\t\t</cwmp:TransferComplete>\n");
		msg.append("\t</soapenv:Body>\n");
		msg.append("</soapenv:Envelope>\n");

		return String.valueOf(msg);
	}

}
