package com.github.freeacs.ws.impl;

import com.github.freeacs.common.util.Sleep;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;

public class XMLServer extends HttpServlet {

	private static final long serialVersionUID = 7084562311369022182L;
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(XMLServer.class);
	private static org.slf4j.Logger xmlLogger = LoggerFactory.getLogger("Xml");

	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		doPost(req, res);
	}

	public void destroy() {
		Sleep.terminateApplication();
	}

	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		String xmlIn = convert2Str(new InputStreamReader(req.getInputStream()));
		logger.debug("Received an xml from " + req.getRemoteHost());
		String xmlInFormatted = prettyFormat(xmlIn);
		String url = Properties.REDIRECT_URL;
		PrintWriter pw = res.getWriter();
		if (url != null) {
			if (url.startsWith("https")) {
				logger.debug("Provider URL is HTTPS, will check certificates and install if needed");
				try {
					InstallCert.installCertificate(url);
				} catch (Exception e) {
					String errorMsg = "An error occured in installation of certificate: " + e.getMessage();
					logger.error(errorMsg);
					xmlLogger.info("Conversation\n====================================\nRequest:\n" + xmlInFormatted + "\nResponse:\n" + errorMsg + "\n");
					pw.print(soapFault(errorMsg, req.getRemoteHost()));
				}
			}
			HttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
			HttpClient client = new HttpClient(connectionManager);
			client.getParams().setParameter(HttpClientParams.USE_EXPECT_CONTINUE, new Boolean(true));
			client.getParams().setParameter(HttpConnectionParams.CONNECTION_TIMEOUT, 2000);
			PostMethod pm = new PostMethod(url);
			RequestEntity requestEntity = new StringRequestEntity(xmlIn, "text/xml", "ISO-8859-1");
			pm.setRequestEntity(requestEntity);
			pm.setRequestHeader(new Header("SOAPAction", "http://http://xapsws.owera.com/xapsws/soap"));
			logger.debug("Redirects received xml " + url);
			int statusCode = client.executeMethod(pm);
			String xmlOut = convert2Str(new InputStreamReader(pm.getResponseBodyAsStream(), pm.getResponseCharSet()));
			logger.debug("Received xml response from " + url);
			String xmlOutFormatted = prettyFormat(xmlOut);
			if (statusCode == 200)
				logger.debug("The status code 200 indicated OK response from " + url);
			else
				logger.error("The status code " + statusCode + " indicated ERROR response from " + url);
			pw.print(xmlOut);
			xmlLogger.info("Conversation\n====================================\nRequest:\n" + xmlInFormatted + "\nResponse: (HTTP return code: " + statusCode + ")\n" + xmlOutFormatted + "\n");
		} else {
			String errorMsg = "No redirect-url defined in xaps-ws.properties";
			logger.error(errorMsg);
			xmlLogger.info("Conversation\n====================================\nRequest:\n" + xmlInFormatted + "\nResponse:\n" + errorMsg + "\n");
			pw.print(soapFault(errorMsg, req.getRemoteHost()));
		}

	}

	private static Document parseXmlFile(String in) {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(in));
			return db.parse(is);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static String prettyFormat(String unformattedXml) {
		try {
			final Document document = parseXmlFile(unformattedXml);
			OutputFormat format = new OutputFormat(document);
			format.setLineWidth(65);
			format.setIndenting(true);
			format.setIndent(2);
			Writer out = new StringWriter();
			XMLSerializer serializer = new XMLSerializer(out, format);
			serializer.serialize(document);
			return out.toString();
		} catch (IOException e) {
			return unformattedXml;
		}
	}

	private static String convert2Str(InputStreamReader isr) throws IOException {
		BufferedReader br = new BufferedReader(isr);
		StringBuilder requestSB = new StringBuilder(1000);
		while (true) {
			String line = br.readLine();
			if (line == null)
				break;
			requestSB.append(line);
		}
		return requestSB.toString();

	}

	private static String soapFault(String errorMsg, String hostname) {
		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		sb
				.append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">");
		sb.append("<soapenv:Body>");
		sb.append("<soapenv:Fault>");
		sb.append("<faultcode>soapenv:Server.userException</faultcode>");
		sb.append("<faultstring>").append(errorMsg).append("</faultstring>");
		sb.append("<detail>");
		sb.append("<ns1:hostname xmlns:ns1=\"http://xml.apache.org/axis/\">").append(hostname).append("</ns1:hostname>");
		sb.append("</detail>");
		sb.append("</soapenv:Fault>");
		sb.append("</soapenv:Body>");
		sb.append("</soapenv:Envelope>");
		return sb.toString();
	}

}
