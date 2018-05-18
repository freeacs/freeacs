package com.github.freeacs.tr069.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


/**
 * The class is responsible for parsing the DeviceId entity and populating the 
 * DeviceIdStruct object fields (manufacturer, oui, productClass, serialNumber).
 */

public class DeviceIdHandler extends DefaultHandler {
	public static final String DEVICE_ID_TAG = "DeviceId";
	public static final String MANUFACTURER_TAG = "Manufacturer";
	public static final String OUI_TAG = "OUI";
	public static final String PRODUCT_CLASS_TAG = "ProductClass";
	public static final String SERIAL_NUMBER_TAG = "SerialNumber";

	Parser owner;
	private DeviceIdStruct didStruct;
	private StringBuilder currTextContent = new StringBuilder();

	public DeviceIdHandler(DeviceIdStruct didStruct, Parser owner) {
		this.didStruct = didStruct;
		this.owner = owner;
	}

	public void startElement(String namespaceURI, String localName, String qualifiedName, Attributes attributes) throws SAXException {
		currTextContent = new StringBuilder();
		if (DEVICE_ID_TAG.equals(localName)) {
			didStruct = new DeviceIdStruct();
		}
	}

	public void endElement(String namespaceURI, String localName, String qualifiedName) throws SAXException {
		if (DEVICE_ID_TAG.equals(localName)) {
			owner.getXMLReader().setContentHandler(owner);
		} else if (MANUFACTURER_TAG.equals(localName)) {
			if (didStruct != null) {
				if (currTextContent != null) {
					didStruct.setManufacturer(new String(currTextContent));
				}
			}
		} else if (OUI_TAG.equals(localName)) {
			if (didStruct != null) {
				if (currTextContent != null) {
					didStruct.setOui(new String(currTextContent));
				}
			}
		} else if (PRODUCT_CLASS_TAG.equals(localName)) {
			if (didStruct != null) {
				if (currTextContent != null) {
					didStruct.setProductClass(new String(currTextContent));
				}
			}
		} else if (SERIAL_NUMBER_TAG.equals(localName)) {
			if (didStruct != null) {
				if (currTextContent != null) {
					didStruct.setSerialNumber(new String(currTextContent));
				}
			}
		}
	}

	public void characters(char[] ch, int start, int length) throws SAXException {
		String content = String.valueOf(ch).substring(start, (start + length));
		currTextContent.append(content.trim());
	}
}
