package com.github.freeacs.tr069.xml;

import com.github.freeacs.tr069.exception.TR069Exception;
import com.github.freeacs.tr069.exception.TR069ExceptionShortMessage;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParserFactory;
import java.io.StringReader;
import java.util.HashMap;

/**
 * The class is responsible for parsing the SOAP messages from the CPE. 
 * The messages could be a TR-069 request or a TR-069 response.
 */
public class Parser extends DefaultHandler {
	public static final String MAX_ENVELOPES_TAG = "MaxEnvelopes";
	public static final String CURRENT_TIME_TAG = "CurrentTime";
	public static final String RETRY_COUNT_TAG = "RetryCount";
	public static final String STATUS_TAG = "Status";
	public static final String START_TIME_TAG = "StartTime";
	public static final String COMPLETE_TIME_TAG = "CompleteTime";
	public static final String COMMAND_KEY_TAG = "CommandKey";
	public static final String FAULT_STRUCT_TAG = "FaultStruct";

	protected SAXParserFactory factory;
	protected XMLReader reader;
	protected HashMap<String, ContentHandler> parsers;
	private StringBuilder currTextContent = new StringBuilder();

	private Header headers;
	private DeviceIdStruct didStruct;
	private EventList events;
	private ParameterList params;
	private MethodList methods;
	private String maxEnvelopes;
	private String currentTime;
	private String retryCount;
	private String status;
	private String startTime;
	private String completeTime;
	private String commandKey;
	private Fault fault;

	/*
	 * Parse the soap messages using the standard SAX Parser
	 * 
	 */
	public Parser(String soapmsg) throws TR069Exception {

		InputSource xmlSource = getStringAsSource(soapmsg);
		initializeDataMappings();

		parsers = new HashMap<String, ContentHandler>();
		parsers.put(HeaderHandler.HEADER_TAG, new HeaderHandler(headers, this));
		parsers.put(FaultHandler.FAULT_TAG, new FaultHandler(fault, this));
		parsers.put(DeviceIdHandler.DEVICE_ID_TAG, new DeviceIdHandler(didStruct, this));
		parsers.put(EventHandler.EVENT_TAG, new EventHandler(events, this));
		parsers.put(ParameterListHandler.PARAMETER_LIST_TAG, new ParameterListHandler(params, this));
		parsers.put(MethodListHandler.METHOD_LIST_TAG, new MethodListHandler(methods, this));

		try {
			//the "SAXParserFactory" class indication has been removed.
			factory = getParserFactory();
			factory.setNamespaceAware(true);
			reader = factory.newSAXParser().getXMLReader();
			reader.setContentHandler(this);
			reader.setErrorHandler(new SOAPErrorHandler());
			reader.parse(xmlSource);
		} catch (Exception ex) {
			throw new TR069Exception("Parsing of SOAP/XML request failed", TR069ExceptionShortMessage.MISC, ex);
		}
	}

	/**
	 * Initializes data mapping members
	 */
	private void initializeDataMappings() {
		this.didStruct = new DeviceIdStruct();
		this.events = new EventList();
		this.headers = new Header();
		this.params = new ParameterList();
		this.methods = new MethodList();
		this.fault = new Fault();
	}

	/**
	 * @return a new instance of a SAXParserFactory
	 */
	private SAXParserFactory getParserFactory() {
		if (factory == null) {
			factory = SAXParserFactory.newInstance();
			factory.setNamespaceAware(true);
		}

		return factory;
	}

	public XMLReader getXMLReader() {
		return this.reader;
	}

	/**
	 * @return the map of handlers (ContentHandlers) available
	 */
	public HashMap<String, ContentHandler> getHandlerMap() {
		return this.parsers;
	}

	public DeviceIdStruct getDeviceIdStruct() {
		return this.didStruct;
	}

	public EventList getEventList() {
		return this.events;
	}

	public Header getHeader() {
		return this.headers;
	}

	public ParameterList getParameterList() {
		return this.params;
	}

	public MethodList getMethodList() {
		return this.methods;
	}

	public String getMaxEnvelopes() {
		return this.maxEnvelopes;
	}

	public String getCurrentTime() {
		return this.currentTime;
	}

	public String getRetryCount() {
		return this.retryCount;
	}

	public String getStatus() {
		return this.status;
	}

	public String getStartTime() {
		return this.startTime;
	}

	public String getCompleteTime() {
		return this.completeTime;
	}

	public String getCommandKey() {
		return this.commandKey;
	}

	public Fault getFault() {
		return this.fault;
	}

	private static InputSource getStringAsSource(String xml) {
		if (xml != null && !xml.equals("")) {
			StringReader xmlReader = new StringReader(xml);
			return new InputSource(xmlReader);
		}
		return null;
	}

	public void startElement(String namespaceURI, String localName, String qualifiedName, Attributes attributes) throws SAXException {
		currTextContent = new StringBuilder();
		if (getHandlerMap().containsKey(localName)) {
			reader.setContentHandler(getHandlerMap().get(localName));
		} else if (FAULT_STRUCT_TAG.equals(localName)) {
			this.fault = new Fault();
			FaultHandler faultHandler = new FaultHandler(this.fault, this);
			reader.setContentHandler(faultHandler);
		}
	}

	public void endElement(String namespaceURI, String localName, String qualifiedName) {
		if (MAX_ENVELOPES_TAG.equals(localName)) {
			this.maxEnvelopes = new String(currTextContent);
		} else if (CURRENT_TIME_TAG.equals(localName)) {
			this.currentTime = new String(currTextContent);
		} else if (RETRY_COUNT_TAG.equals(localName)) {
			this.retryCount = new String(currTextContent);
		} else if (STATUS_TAG.equals(localName)) {
			this.status = new String(currTextContent);
		} else if (START_TIME_TAG.equals(localName)) {
			this.startTime = new String(currTextContent);
		} else if (COMPLETE_TIME_TAG.equals(localName)) {
			this.completeTime = new String(currTextContent);
		} else if (COMMAND_KEY_TAG.equals(localName)) {
			this.commandKey = new String(currTextContent);
		}
	}

	public void characters(char[] ch, int start, int length) throws SAXException {
		String content = String.valueOf(ch).substring(start, (start + length));
		currTextContent.append(content.trim());
	}
}
