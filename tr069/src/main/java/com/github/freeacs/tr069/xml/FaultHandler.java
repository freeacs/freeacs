package com.github.freeacs.tr069.xml;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParserFactory;


/**
 * The class is responsible for parsing the SOAP Fault entity received from the CPE.
 */
public class FaultHandler extends DefaultHandler {

	public static final String DSLFORUM_NS = "urn:dslforum-org:cwmp-1-0";
	public static final String SOAP_ENV_NS = "http://schemas.xmlsoap.org/soap/envelope/";
	
	public static final String FAULT_TAG = "Fault";
	public static final String SOAP_FAULT_CODE_TAG = "faultcode";
	public static final String SOAP_FAULT_STRING_TAG = "faultstring";
	public static final String SOAP_DETAIL_TAG = "detail";
	public static final String FAULT_CODE_TAG = "FaultCode";
	public static final String FAULT_STRING_TAG = "FaultString";
	public static final String SET_PARAMETER_VALUES_FAULT_TAG = "SetParameterValuesFault";
	public static final String PARAMETER_NAME_TAG = "ParameterName";
	public static final String FAULT_STRUCT_TAG = "FaultStruct";

	private SAXParserFactory factory;
	private XMLReader reader;
	private Fault fault;
	private Parser owner;
	private StringBuilder currTextContent = new StringBuilder();
	
	public FaultHandler(Fault fault, Parser owner) {
		this.fault = fault;
		this.owner = owner;
	}

	public FaultHandler(InputSource xmlSource) {
		this.fault = null;
		try {
			SAXParserFactory factory = getParserFactory();
			factory.setNamespaceAware(true);

			reader = factory.newSAXParser().getXMLReader();
			reader.setContentHandler(this);
			reader.setErrorHandler(new SOAPErrorHandler());
			reader.parse(xmlSource);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private SAXParserFactory getParserFactory() {
		if (factory == null) {
			factory = SAXParserFactory.newInstance();
			factory.setNamespaceAware(true);
		}

		return factory;
	}

	public Fault getFault() {
		return this.fault;
	}

	private boolean isParameterFault = false;
	private String faultCode = null;
	private String faultString = null;
	private String parameterName = null;

	public void startElement(String namespaceURI, String localName, String qualifiedName, Attributes attributes) throws SAXException {
		currTextContent = new StringBuilder();
		if (SOAP_ENV_NS.equals(namespaceURI) && FAULT_TAG.equals(localName)) {
			this.fault = new Fault();
		} else if (SOAP_FAULT_CODE_TAG.equals(localName)) {
		} else if (SOAP_FAULT_STRING_TAG.equals(localName)) {
		} else if (SOAP_DETAIL_TAG.equals(localName)) {
		} else if (DSLFORUM_NS.equals(namespaceURI) && FAULT_TAG.equals(localName)) {
		} else if (FAULT_CODE_TAG.equals(localName)) {
		} else if (FAULT_STRING_TAG.equals(localName)) {
		} else if (SET_PARAMETER_VALUES_FAULT_TAG.equals(localName)) {
			isParameterFault = true;
		} else if (PARAMETER_NAME_TAG.equals(localName)) {
		}

	}

	public void endElement(String namespaceURI, String localName, String qualifiedName) {
		if (SOAP_ENV_NS.equals(namespaceURI) && FAULT_TAG.equals(localName)) {
		}
		if (FAULT_STRUCT_TAG.equals(localName)) {
			if (owner != null) {
				owner.getXMLReader().setContentHandler(owner);
			}
		} else if (SOAP_FAULT_CODE_TAG.equals(localName)) {
			if (this.fault != null) {
				this.fault.setSoapFaultCode(new String(currTextContent));
			}
		} else if (SOAP_FAULT_STRING_TAG.equals(localName)) {
			if (this.fault != null) {
				this.fault.setSoapFaultString(new String(currTextContent));
			}
		} else if (SOAP_DETAIL_TAG.equals(localName)) {
		} else if (DSLFORUM_NS.equals(namespaceURI) && FAULT_TAG.equals(localName)) {

		} else if (FAULT_CODE_TAG.equals(localName)) {
			this.faultCode = new String(currTextContent);
			if (!isParameterFault) {
				this.fault.setFaultCode(this.faultCode);
			}
		} else if (FAULT_STRING_TAG.equals(localName)) {
			this.faultString = new String(currTextContent);
			if (!isParameterFault) {
				this.fault.setFaultString(this.faultString);
			}
		} else if (SET_PARAMETER_VALUES_FAULT_TAG.equals(localName)) {
			this.fault.addParameterValuesFault(new SetParameterValuesFault(faultCode, faultString, parameterName));
			isParameterFault = false;
		} else if (PARAMETER_NAME_TAG.equals(localName)) {
			this.parameterName = new String(currTextContent);
		}
	}

	public void characters(char[] ch, int start, int length) throws SAXException {
		String content = String.valueOf(ch).substring(start, (start + length));
		currTextContent.append(content.trim());
	}
}
