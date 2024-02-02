package com.github.freeacs.tr069.xml;

import com.github.freeacs.tr069.exception.TR069Exception;
import com.github.freeacs.tr069.exception.TR069ExceptionShortMessage;
import com.github.freeacs.tr069.methods.ProvisioningMethod;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * The class is responsible for parsing the SOAP messages from the CPE. The messages could be a
 * TR-069 request or a TR-069 response.
 */
@Getter
@Slf4j
public class Parser extends DefaultHandler {
  public static final String FEATURE_DISALLOW_DOCTYPE_DECL = "http://apache.org/xml/features/disallow-doctype-decl";

  private static final String MAX_ENVELOPES_TAG = "MaxEnvelopes";
  private static final String CURRENT_TIME_TAG = "CurrentTime";
  private static final String RETRY_COUNT_TAG = "RetryCount";
  private static final String STATUS_TAG = "Status";
  private static final String START_TIME_TAG = "StartTime";
  private static final String COMPLETE_TIME_TAG = "CompleteTime";
  private static final String COMMAND_KEY_TAG = "CommandKey";
  private static final String FAULT_STRUCT_TAG = "FaultStruct";
  public static final String CWMP_VERSION_NAMESPACE_PREFIX = "urn:dslforum-org:cwmp-";

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
  private boolean insideBody;
  private ProvisioningMethod cwmpMethod;
  private String cwmpVersion;
  private String rawXMLForDebugging;

  /** Parse the soap messages using the standard SAX Parser. */
  public Parser(InputStream xmlInputStream, int contentLength, boolean debugXML) throws TR069Exception, IOException {
    initializeDataMappings();

    if (contentLength <= 0) {
      log.debug("THere is no xml payload to parse");
      return;
    }

    parsers = new HashMap<>();
    parsers.put(HeaderHandler.HEADER_TAG, new HeaderHandler(header, this));
    parsers.put(FaultHandler.FAULT_TAG, new FaultHandler(fault, this));
    parsers.put(DeviceIdHandler.DEVICE_ID_TAG, new DeviceIdHandler(deviceIdStruct, this));
    parsers.put(EventHandler.EVENT_TAG, new EventHandler(eventList, this));
    parsers.put(ParameterListHandler.PARAMETER_LIST_TAG, new ParameterListHandler(parameterList, this));
    parsers.put(MethodListHandler.METHOD_LIST_TAG, new MethodListHandler(methodList, this));

    try {
      factory = getParserFactory();
      factory.setNamespaceAware(true);
      xmlReader = factory.newSAXParser().getXMLReader();
      xmlReader.setContentHandler(this);
      xmlReader.setErrorHandler(new SOAPErrorHandler());

      if (debugXML) {
        // Efficiently read and duplicate the input stream for debugging
        byte[] bytes = xmlInputStream.readAllBytes();
        this.rawXMLForDebugging = new String(bytes, StandardCharsets.UTF_8);
        xmlInputStream = new ByteArrayInputStream(bytes); // Reset stream for parsing
      }

      xmlReader.parse(new InputSource(xmlInputStream));
    } catch (Exception ex) {
      // Enhanced error handling could go here
      throw new TR069Exception("Parsing of SOAP/XML request failed", TR069ExceptionShortMessage.MISC, ex);
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
    this.cwmpMethod = ProvisioningMethod.Empty;
    this.rawXMLForDebugging = null;
  }

  /** @return a new instance of a SAXParserFactory */
  private SAXParserFactory getParserFactory() throws SAXNotSupportedException, SAXNotRecognizedException, ParserConfigurationException {
    if (factory == null) {
      factory = SAXParserFactory.newInstance();
      factory.setFeature(FEATURE_DISALLOW_DOCTYPE_DECL, true);
      factory.setNamespaceAware(true);
    }

    return factory;
  }

  @Override
  public void startPrefixMapping(String prefix, String uri) {
    if ("cwmp".equals(prefix) && uri.startsWith(CWMP_VERSION_NAMESPACE_PREFIX)) {
      cwmpVersion = uri.replace(CWMP_VERSION_NAMESPACE_PREFIX, "");
    }
  }

  public void startElement(String namespaceURI, String localName, String qualifiedName, Attributes attributes) {
    currTextContent = new StringBuilder();
    if (isSoapBodyElement(namespaceURI, localName)) {
      handleSoapBodyStart();
    } else if (shouldHandleCwmpMethod()) {
      handleCwmpMethod(localName);
    } else {
      delegateToSpecificHandler(localName);
    }
  }

  private boolean isSoapBodyElement(String namespaceURI, String localName) {
    return "Body".equals(localName) && "http://schemas.xmlsoap.org/soap/envelope/".equals(namespaceURI);
  }

  private void handleSoapBodyStart() {
    insideBody = true;
  }

  private boolean shouldHandleCwmpMethod() {
    return insideBody && cwmpMethod == ProvisioningMethod.Empty;
  }

  private void handleCwmpMethod(String localName) {
    cwmpMethod = ProvisioningMethod.fromString(localName);
    insideBody = false;
  }

  private void delegateToSpecificHandler(String localName) {
    if (this.parsers.containsKey(localName)) {
      xmlReader.setContentHandler(this.parsers.get(localName));
    } else if (FAULT_STRUCT_TAG.equals(localName)) {
      FaultHandler faultHandler = new FaultHandler(this.fault = new Fault(), this);
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
