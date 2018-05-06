package com.owera.tr069client.messages;

import java.io.IOException;

import com.owera.tr069client.Arguments;
import com.owera.tr069client.HttpHandler;
import com.owera.tr069client.OutOfConnections;
import com.owera.tr069client.TR069Client;
import com.owera.tr069client.monitor.Status;

public class Inform {

	//	private static Logger logger = Logger.getLogger(Inform.class);

	private final static String OUI = "000000";
	private final static String PRODUCT_CLASS = "TR069TestClient";

	public static String execute(Arguments args, HttpHandler httpHandler, Status status, TR069Client tr069Client) throws OutOfConnections,
			IOException {
		String req = makeRequest(args, httpHandler, tr069Client);
		return httpHandler.send(req, args, status, "IN");
		//		if (method)
		//		boolean isfault = false;
		//
		//		if (response == null)
		//			throw new OutOfConnections("The inform-request gets \"null\" in response from the server");
		//		if (response.indexOf("InformResponse") > -1) {
		//			return isfault;
		//		}
		//		else if (response.length() == 0) {
		//			logger.error("The reponse from the server was of length 0");
		//			isfault = true;
		//			return isfault;
		//		} else
		//			throw new RuntimeException("[INFORM] Wrong response from server");
	}

	private static String makeRequest(Arguments args, HttpHandler httpHandler, TR069Client tr069Client) {
		StringBuilder message = new StringBuilder(
				"<soapenv:Envelope soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:cwmp=\"urn:dslforum-org:cwmp-1-0\">\n");
		message.append("\t<soapenv:Header>\n");
		message.append("\t\t<cwmp:ID soapenv:mustUnderstand=\"1\">TEST_CLIENT_INFORM</cwmp:ID>\n");
		message.append("\t\t<cwmp:HoldRequests soapenv:mustUnderstand=\"0\">0</cwmp:HoldRequests>\n");
		message.append("\t</soapenv:Header>\n");
		message.append("\t<soapenv:Body>\n");
		message.append("\t\t<cwmp:Inform>\n");
		message.append("\t\t\t<DeviceId>\n");
		message.append("\t\t\t\t<Manufacturer>OWERA</Manufacturer>\n");
		message.append("\t\t\t\t<OUI>" + OUI + "</OUI>\n");
		message.append("\t\t\t\t<ProductClass>" + PRODUCT_CLASS + "</ProductClass>\n");
		message.append("\t\t\t\t<SerialNumber>" + httpHandler.getSerialNumber() + "</SerialNumber>\n");
		message.append("\t\t\t</DeviceId>\n");
		message.append("\t\t\t<Event soap:arrayType=\"cwmp:EventStruct[1]\">\n");
		message.append("\t\t\t\t<EventStruct>\n");
		message.append("\t\t\t\t\t<EventCode>");
		message.append("2 PERIODIC");
		message.append("</EventCode>\n");
		message.append("\t\t\t\t\t<CommandKey></CommandKey>\n");
		message.append("\t\t\t\t</EventStruct>\n");
		message.append("\t\t\t</Event>\n");
		message.append("\t\t\t<MaxEnvelopes>2</MaxEnvelopes>\n");
		message.append("\t\t\t<CurrentTime>1970-01-01T00:18:23Z</CurrentTime>\n");
		message.append("\t\t\t<RetryCount>17</RetryCount>\n");
		message.append("\t\t\t<ParameterList soap:arrayType=\"cwmp:ParameterValueStruct[11]\">\n");
		message.append("\t\t\t\t<ParameterValueStruct>\n");
		message.append("\t\t\t\t\t<Name>InternetGatewayDevice.DeviceInfo.SpecVersion</Name>\n");
		message.append("\t\t\t\t\t<Value xsi:type=\"xsd:string\">1</Value>\n");
		message.append("\t\t\t\t</ParameterValueStruct>\n");
		message.append("\t\t\t\t<ParameterValueStruct>\n");
		message.append("\t\t\t\t\t<Name>InternetGatewayDevice.DeviceInfo.HardwareVersion</Name>\n");
		message.append("\t\t\t\t\t<Value xsi:type=\"xsd:string\">1.0</Value>\n");
		message.append("\t\t\t\t</ParameterValueStruct>\n");
		message.append("\t\t\t\t<ParameterValueStruct>\n");
		message.append("\t\t\t\t\t<Name>InternetGatewayDevice.DeviceInfo.SoftwareVersion</Name>\n");
		message.append("\t\t\t\t\t<Value xsi:type=\"xsd:string\">"+tr069Client.getSoftwareVersion()+"</Value>\n");
		message.append("\t\t\t\t</ParameterValueStruct>\n");
		message.append("\t\t\t\t<ParameterValueStruct>\n");
		message.append("\t\t\t\t\t<Name>InternetGatewayDevice.DeviceInfo.ProvisioningCode</Name>\n");
		message.append("\t\t\t\t\t<Value xsi:type=\"xsd:string\">N/A</Value>\n");
		message.append("\t\t\t\t</ParameterValueStruct>\n");
		message.append("\t\t\t\t<ParameterValueStruct>\n");
		message.append("\t\t\t\t\t<Name>InternetGatewayDevice.DeviceInfo.VendorConfigFile.1.Name</Name>\n");
		message.append("\t\t\t\t\t<Value xsi:type=\"xsd:string\">Owera DHCP client</Value>\n");
		message.append("\t\t\t\t</ParameterValueStruct>\n");
		message.append("\t\t\t\t<ParameterValueStruct>\n");
		message.append("\t\t\t\t\t<Name>InternetGatewayDevice.DeviceInfo.VendorConfigFile.1.Version</Name>\n");
		message.append("\t\t\t\t\t<Value xsi:type=\"xsd:string\">1.0</Value>\n");
		message.append("\t\t\t\t</ParameterValueStruct>\n");
		message.append("\t\t\t\t<ParameterValueStruct>\n");
		message.append("\t\t\t\t\t<Name>InternetGatewayDevice.DeviceInfo.VendorConfigFile.1.Date</Name>\n");
		message.append("\t\t\t\t\t<Value xsi:type=\"xsd:string\">Configuration modified b</Value>\n");
		message.append("\t\t\t\t</ParameterValueStruct>\n");
		message.append("\t\t\t\t<ParameterValueStruct>\n");
		message.append("\t\t\t\t\t<Name>InternetGatewayDevice.DeviceInfo.VendorConfigFile.1.Description</Name>\n");
		message.append("\t\t\t\t\t<Value xsi:type=\"xsd:string\">DHCP client</Value>\n");
		message.append("\t\t\t\t</ParameterValueStruct>\n");
		message.append("\t\t\t\t<ParameterValueStruct>\n");
		message.append("\t\t\t\t\t<Name>InternetGatewayDevice.ManagementServer.ConnectionRequestURL</Name>\n");
		message.append("\t\t\t\t\t<Value xsi:type=\"xsd:string\">"+tr069Client.getIp()+"</Value>\n");
		message.append("\t\t\t\t</ParameterValueStruct>\n");
		message.append("\t\t\t\t<ParameterValueStruct>\n");
		message.append("\t\t\t\t\t<Name>InternetGatewayDevice.ManagementServer.ParameterKey</Name>\n");
		message.append("\t\t\t\t\t<Value xsi:type=\"xsd:string\">" + tr069Client.getParameterKey() + "</Value>\n");
		message.append("\t\t\t\t</ParameterValueStruct>\n");
		message.append("\t\t\t\t<ParameterValueStruct>\n");
		message.append("\t\t\t\t\t<Name>.ExternalIPAddress</Name>\n");
		message.append("\t\t\t\t\t<Value xsi:type=\"xsd:string\">10.0.0.138</Value>\n");
		message.append("\t\t\t\t</ParameterValueStruct>\n");
		message.append("\t\t\t</ParameterList>\n");
		message.append("\t\t</cwmp:Inform>\n");
		message.append("\t</soapenv:Body>\n");
		message.append("</soapenv:Envelope>\n");
		return message.toString();

	}

}
