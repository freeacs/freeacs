package com.github.freeacs.tr069.xml;

import com.github.freeacs.tr069.exception.TR069Exception;
import com.github.freeacs.tr069.exception.TR069ExceptionShortMessage;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.SAXParserFactory;

import lombok.Data;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * The class is responsible for parsing the SOAP messages from the CPE. The messages could be a
 * TR-069 request or a TR-069 response.
 */
@Data
public class Parser extends DefaultHandler {
  private static final String MAX_ENVELOPES_TAG = "MaxEnvelopes";
  private static final String CURRENT_TIME_TAG = "CurrentTime";
  private static final String RETRY_COUNT_TAG = "RetryCount";
  private static final String STATUS_TAG = "Status";
  private static final String START_TIME_TAG = "StartTime";
  private static final String COMPLETE_TIME_TAG = "CompleteTime";
  private static final String COMMAND_KEY_TAG = "CommandKey";
  private static final String FAULT_STRUCT_TAG = "FaultStruct";

  private SAXParserFactory factory;
  private XMLReader xmlReader;
  private Map<String, ContentHandler> parsers;
  private StringBuilder currTextContent = new StringBuilder();

  private Header header;
  private DeviceIdStruct deviceIdStruct;
  private EventList eventList;
  private ParameterList parameterList;
  private MethodList methodList;
  private String maxEnvelopes;
  private String currentTime;
  private String retryCount;
  private String status;
  private String startTime;
  private String completeTime;
  private String commandKey;
  private Fault fault;

  /** Parse the soap messages using the standard SAX Parser. */
  public Parser(String soapmsg) throws TR069Exception {
    InputSource xmlSource = getStringAsSource(soapmsg);
    initializeDataMappings();

    parsers = new HashMap<>();
    parsers.put(HeaderHandler.HEADER_TAG, new HeaderHandler(header, this));
    parsers.put(FaultHandler.FAULT_TAG, new FaultHandler(fault, this));
    parsers.put(DeviceIdHandler.DEVICE_ID_TAG, new DeviceIdHandler(deviceIdStruct, this));
    parsers.put(EventHandler.EVENT_TAG, new EventHandler(eventList, this));
    parsers.put(ParameterListHandler.PARAMETER_LIST_TAG, new ParameterListHandler(parameterList, this));
    parsers.put(MethodListHandler.METHOD_LIST_TAG, new MethodListHandler(methodList, this));

    try {
      // the "SAXParserFactory" class indication has been removed.
      factory = getParserFactory();
      factory.setNamespaceAware(true);
      xmlReader = factory.newSAXParser().getXMLReader();
      xmlReader.setContentHandler(this);
      xmlReader.setErrorHandler(new SOAPErrorHandler());
      xmlReader.parse(xmlSource);
    } catch (Exception ex) {
      throw new TR069Exception(
          "Parsing of SOAP/XML request failed", TR069ExceptionShortMessage.MISC, ex);
    }
  }

  /** Initializes data mapping members. */
  private void initializeDataMappings() {
    this.deviceIdStruct = new DeviceIdStruct();
    this.eventList = new EventList();
    this.header = new Header();
    this.parameterList = new ParameterList();
    this.methodList = new MethodList();
    this.fault = new Fault();
  }

  /** @return a new instance of a SAXParserFactory */
  private SAXParserFactory getParserFactory() {
    if (factory == null) {
      factory = SAXParserFactory.newInstance();
      factory.setNamespaceAware(true);
    }

    return factory;
  }

  private static InputSource getStringAsSource(String xml) {
    if (xml != null && !"".equals(xml)) {
      StringReader xmlReader = new StringReader(xml);
      return new InputSource(xmlReader);
    }
    return null;
  }

  public void startElement(
      String namespaceURI, String localName, String qualifiedName, Attributes attributes) {
    currTextContent = new StringBuilder();
    if (this.parsers.containsKey(localName)) {
      xmlReader.setContentHandler(this.parsers.get(localName));
    } else if (FAULT_STRUCT_TAG.equals(localName)) {
      this.fault = new Fault();
      FaultHandler faultHandler = new FaultHandler(this.fault, this);
      xmlReader.setContentHandler(faultHandler);
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

  public void characters(char[] ch, int start, int length) {
    String content = String.valueOf(ch).substring(start, start + length);
    currTextContent.append(content.trim());
  }
}
