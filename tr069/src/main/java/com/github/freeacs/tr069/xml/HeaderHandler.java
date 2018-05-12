package com.github.freeacs.tr069.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


/**
 * The class is responsible for parsing the SOAP header entity and populating  
 * the header object fields (Transcation ID, HoldRequests).
 */

public class HeaderHandler extends DefaultHandler {
	public static final String HEADER_TAG = "Header";

	Parser owner;
	Header header;

	private StringBuilder currTextContent = new StringBuilder();

	public HeaderHandler(Header headers, Parser owner) {
		this.header = headers;
		this.owner = owner;
	}

	public void startElement(String namespaceURI, String localName, String qualifiedName, Attributes attributes) throws SAXException {
		currTextContent = new StringBuilder();
	}

	public void endElement(String namespaceURI, String localName, String qualifiedName) throws SAXException {
		if (HEADER_TAG.equals(localName)) {
			owner.getXMLReader().setContentHandler(owner);
		} else {
			this.header.setHeaderField(localName, new String(currTextContent));
		}
	}

	public void characters(char[] ch, int start, int length) throws SAXException {
		String content = String.valueOf(ch).substring(start, (start + length));
		currTextContent.append(content.trim());
	}
}
