package com.github.freeacs.tr069;


import com.github.freeacs.tr069.xml.Fault;
import com.github.freeacs.tr069.xml.XMLChar;

public class HTTPReqData {
	private String method;
	private String xml;
	private Fault fault;

	public enum NodeType {
		CONTENT, STARTTAG, ENDTAG;
	}

	public static class Node {
		private NodeType type;
		private int endPos;
		private String nodeString;

		public Node(NodeType type, int startPos, int endPos, String unformattedXml) {
			this.type = type;
			this.endPos = endPos;
			this.nodeString = unformattedXml.substring(startPos, endPos).trim();
		}

		public NodeType getType() {
			return type;
		}

		public int getEndPos() {
			return endPos;
		}

		public String toString() {
			return nodeString;
		}

	}

	public static class XMLFormatter {

		/**
		 * Filter away all illegal XML characters 
		 */
		public static String filter(String unfilteredXml) {
			StringBuilder sb = new StringBuilder();
		    for (int i = 0; i < unfilteredXml.length(); i++) {
		        char c = unfilteredXml.charAt(i);
		        if (XMLChar.isValid(c)) {
		            sb.append(c);
		        }
		    }
		    return sb.toString();
		}
		
		/**
		 * We do not use XML-parser here, because an XML-parser fails upon strange characters. 
		 * Or only concern here is to pretty-print xml, to make it more human readable in
		 * logs. If the unformattedXml is not well-formed, the output will not be "symmetric".
		 * If the unformattedXml is not XML, nothing will be printed.
		 */
		public static String prettyprint(String unformattedXml) {
			StringBuilder formattedXml = new StringBuilder();
			int currentPos = 0;
			int startTagCounter = 0;
			Node node = nextNode(unformattedXml, 0);
			Node previousNode = null;
			while (node != null) {
				if (node.getType() == NodeType.STARTTAG) {
					if (previousNode != null && previousNode.getType() == NodeType.STARTTAG)
						formattedXml.append("\n" + tabs(startTagCounter) + node); // start-tag following a start-tag
					else
						formattedXml.append(tabs(startTagCounter) + node); // start-tag following an end-tag
					startTagCounter++;
				} else if (node.getType() == NodeType.CONTENT) {
					formattedXml.append(node);
				} else { // NodeType.ENDTAG
					startTagCounter--;
					if (previousNode != null && previousNode.getType() == NodeType.ENDTAG)
						formattedXml.append(tabs(startTagCounter) + node);
					else
						formattedXml.append(node);
					formattedXml.append("\n");
				}
				currentPos = node.getEndPos();
				previousNode = node;
				node = nextNode(unformattedXml, currentPos);
			}
			return formattedXml.toString();
		}

		private static String tabs(int indentCount) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < indentCount; i++)
				sb.append("  ");
			return sb.toString();
		}

		private static Node nextNode(String unformattedXml, int readFromPos) {
			int ltPos = unformattedXml.indexOf("<", readFromPos);
			int gtPos = unformattedXml.indexOf(">", ltPos);
			if (ltPos == -1 || gtPos == -1)
				return null;
			if (ltPos > readFromPos)
				return new Node(NodeType.CONTENT, readFromPos, ltPos, unformattedXml);
			else if (unformattedXml.charAt(ltPos + 1) == '/')
				return new Node(NodeType.ENDTAG, ltPos, gtPos + 1, unformattedXml);
			else if (unformattedXml.charAt(gtPos - 1) == '/')
				return new Node(NodeType.ENDTAG, ltPos, gtPos + 1, unformattedXml);
			else
				return new Node(NodeType.STARTTAG, ltPos, gtPos + 1, unformattedXml);
		}

	}

	public String getXml() {
		return xml;
	}

	public void setXml(String xml) {
		this.xml = xml;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public Fault getFault() {
		return fault;
	}

	public void setFault(Fault fault) {
		this.fault = fault;
	}

	public static void main(String[] args) {
		try {
			String s = "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:soapenc=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:cwmp=\"urn:dslforum-org:cwmp-1-0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"><soap:Header><cwmp:ID soap:mustUnderstand=\"1\">ubicom_tr069_id_1</cwmp:ID><cwmp:NoMoreRequests>1</cwmp:NoMoreRequests></soap:Header><soap:Body><cwmp:Inform xmlns:cwmp=\"urn:dslforum-org:cwmp-1-0\"><DeviceId><Manufacturer>Ping Communication</Manufacturer><OUI>002194</OUI><ProductClass>NPA201E</ProductClass>\n<SerialNumber>0021940012AC</SerialNumber></DeviceId><Event soapenc:arrayType=\"cwmp:EventStruct[3]\"><EventStruct><EventCode>4 VALUE CHANGE</EventCode><CommandKey>ACTIVE</CommandKey></EventStruct><EventStruct><EventCode>1 BOOT</EventCode><CommandKey></CommandKey></EventStruct><EventStruct><EventCode>0 BOOTSTRAP</EventCode><CommandKey></CommandKey></EventStruct></Event><MaxEnvelopes>1</MaxEnvelopes><CurrentTime>0001-01-01T00:00:19</CurrentTime><RetryCount>0</RetryCount><ParameterList soapenc:arrayType=\"cwmp:ParameterValueStruct[8]\"><ParameterValueStruct><Name>InternetGatewayDevice.DeviceInfo.SpecVersion</Name><Value xsi:type=\"xsd:string\">1.0</Value></ParameterValueStruct><ParameterValueStruct><Name>InternetGatewayDevice.DeviceInfo.HardwareVersion</Name><Value xsi:type=\"xsd:string\">000001</Value></ParameterValueStruct><ParameterValueStruct><Name>InternetGatewayDevice.DeviceInfo.SoftwareVersion</Name><Value xsi:type=\"xsd:string\">npa201e-6.2.99-generic-tr069-r38612</Value></ParameterValueStruct><ParameterValueStruct><Name>InternetGatewayDevice.DeviceInfo.ProvisioningCode</Name><Value xsi:type=\"xsd:string\">168</Value></ParameterValueStruct><ParameterValueStruct><Name>InternetGatewayDevice.ManagementServer.ConnectionRequestURL</Name><Value xsi:type=\"xsd:string\">http://192.168.175.196:9699/conn_req_url</Value></ParameterValueStruct><ParameterValueStruct><Name>InternetGatewayDevice.ManagementServer.ParameterKey</Name><Value xsi:type=\"xsd:string\">3c4e7ec7b6643c51733f58fd78bcaf88</Value></ParameterValueStruct><ParameterValueStruct><Name>InternetGatewayDevice.WANDevice.1.WANConnectionDevice.1.WANIPConnection.1.ExternalIPAddress</Name><Value xsi:type=\"xsd:string\">192.168.175.196</Value></ParameterValueStruct><ParameterValueStruct><Name>InternetGatewayDevice.WANDevice.1.WANConnectionDevice.1.WANIPConnection.1.ExternalIPAddress</Name><Value xsi:type=\"xsd:string\">192.168.175.196</Value></ParameterValueStruct></ParameterList></cwmp:Inform></soap:Body></soap:Envelope>";
			System.out.println(XMLFormatter.prettyprint(s));
		} catch (Throwable t) {
			System.err.println("Error occurred: " + t);
		}
	}

}
