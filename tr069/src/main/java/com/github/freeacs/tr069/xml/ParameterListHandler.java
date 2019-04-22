package com.github.freeacs.tr069.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * The class is responsible for parsing the ParameterList entity and populating the ParameterList
 * which is a list of ParameterValueStruct or ParameterInfoStruct objects (which depends on the RPC
 * method performed).
 */
public class ParameterListHandler extends DefaultHandler {
  private enum ParameterType {
    VALUE,
    INFO,
    ATTRIBUTE
  }

  public static final String PARAMETER_LIST_TAG = "ParameterList";
  public static final String PARAMETER_VALUE_STRUCT_TAG = "ParameterValueStruct";
  public static final String PARAMETER_INFO_STRUCT_TAG = "ParameterInfoStruct";
  public static final String PARAMETER_ATTRIBUTE_STRUCT_TAG = "ParameterAttributeStruct";
  public static final String NAME_TAG = "Name";
  public static final String VALUE_TAG = "Value";
  public static final String WRITABLE_TAG = "Writable";
  public static final String NOTIFICATION_TAG = "Notification";

  private Parser owner;
  private ParameterList params;
  private ParameterValueStruct pvs;
  private ParameterInfoStruct pis;
  private ParameterAttributeStruct pas;
  private StringBuilder currTextContent = new StringBuilder();

  private ParameterType parameterType;

  public ParameterListHandler(ParameterList params, Parser owner) {
    this.owner = owner;
    this.params = params;
  }

  public void startElement(
      String namespaceURI, String localName, String qualifiedName, Attributes attributes) {
    currTextContent = new StringBuilder();
    if (PARAMETER_VALUE_STRUCT_TAG.equals(localName)) {
      parameterType = ParameterType.VALUE;
      pvs = new ParameterValueStruct();
    } else if (PARAMETER_INFO_STRUCT_TAG.equals(localName)) {
      parameterType = ParameterType.INFO;
      pis = new ParameterInfoStruct();
    } else if (PARAMETER_ATTRIBUTE_STRUCT_TAG.equals(localName)) {
      parameterType = ParameterType.ATTRIBUTE;
      pas = new ParameterAttributeStruct();
    } else if (VALUE_TAG.equals(localName) && pvs != null && attributes != null) {
      for (int i = 0; i < attributes.getLength(); i++) {
        if (attributes.getQName(i).contains(":type")) {
          pvs.setType(attributes.getValue(i));
          break;
        }
      }
    }
  }

  public void endElement(String namespaceURI, String localName, String qualifiedName)
      throws SAXException {
    if (PARAMETER_LIST_TAG.equals(localName)) {
      owner.getXmlReader().setContentHandler(owner);
    } else if (PARAMETER_VALUE_STRUCT_TAG.equals(localName)) {
      this.params.addParameterValueStruct(pvs);
    } else if (PARAMETER_INFO_STRUCT_TAG.equals(localName)) {
      this.params.addParameterInfoStruct(pis);
    } else if (PARAMETER_ATTRIBUTE_STRUCT_TAG.equals(localName)) {
      this.params.addParameterAttributeStruct(pas);
    } else if (NAME_TAG.equals(localName)) {
      if (parameterType == ParameterType.VALUE) {
        pvs.setName(new String(currTextContent));
      } else if (parameterType == ParameterType.INFO) {
        pis.setName(new String(currTextContent));
      } else if (parameterType == ParameterType.ATTRIBUTE) {
        pas.setName(new String(currTextContent));
      }
    } else if (VALUE_TAG.equals(localName)) {
      pvs.setValue(new String(currTextContent));
    } else if (WRITABLE_TAG.equals(localName)) {
      String ct = new String(currTextContent);
      pis.setWritable(!"0".equals(ct) && !"false".equals(ct));
    } else if (NOTIFICATION_TAG.equals(localName)) {
      String ct = new String(currTextContent);
      try {
        pas.setNotifcation(Integer.parseInt(ct));
      } catch (NumberFormatException nfe) {
        throw new SAXException("The notification was not a number (" + ct + ")");
      }
    }
  }

  public void characters(char[] ch, int start, int length) {
    String content = String.valueOf(ch).substring(start, start + length);
    currTextContent.append(content.trim());
  }
}
