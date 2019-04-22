package com.github.freeacs.tr069.xml;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 * The class is responsible for parsing the DeviceId entity and populating the DeviceIdStruct object
 * fields (manufacturer, oui, productClass, serialNumber).
 */
public class DeviceIdHandler extends DefaultHandler {
  static final String DEVICE_ID_TAG = "DeviceId";
  private static final String MANUFACTURER_TAG = "Manufacturer";
  private static final String OUI_TAG = "OUI";
  private static final String PRODUCT_CLASS_TAG = "ProductClass";
  private static final String SERIAL_NUMBER_TAG = "SerialNumber";

  private Parser owner;
  private DeviceIdStruct didStruct;
  private StringBuilder currTextContent = new StringBuilder();

  DeviceIdHandler(DeviceIdStruct didStruct, Parser owner) {
    this.didStruct = didStruct;
    this.owner = owner;
  }

  public void startElement(
      String namespaceURI, String localName, String qualifiedName, Attributes attributes) {
    currTextContent = new StringBuilder();
    if (DEVICE_ID_TAG.equals(localName)) {
      didStruct = new DeviceIdStruct();
    }
  }

  public void endElement(String namespaceURI, String localName, String qualifiedName) {
    if (DEVICE_ID_TAG.equals(localName)) {
      owner.getXmlReader().setContentHandler(owner);
    } else if (MANUFACTURER_TAG.equals(localName)) {
      if (didStruct != null && currTextContent != null) {
        didStruct.setManufacturer(new String(currTextContent));
      }
    } else if (OUI_TAG.equals(localName)) {
      if (didStruct != null && currTextContent != null) {
        didStruct.setOui(new String(currTextContent));
      }
    } else if (PRODUCT_CLASS_TAG.equals(localName)) {
      if (didStruct != null && currTextContent != null) {
        didStruct.setProductClass(new String(currTextContent));
      }
    } else if (SERIAL_NUMBER_TAG.equals(localName)
        && didStruct != null
        && currTextContent != null) {
      didStruct.setSerialNumber(new String(currTextContent));
    }
  }

  public void characters(char[] ch, int start, int length) {
    String content = String.valueOf(ch).substring(start, start + length);
    currTextContent.append(content.trim());
  }
}
