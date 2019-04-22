package com.github.freeacs.tr069.xml;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 * The class is responsible for parsing the MethodList entity (Note that this will only occur when
 * ACS asks the CPE for the list of methods supported using the GetPPCMethod).
 */
public class MethodListHandler extends DefaultHandler {
  public static final String METHOD_LIST_TAG = "MethodList";
  public static final String STRING_TAG = "string";

  private Parser owner;
  private MethodList methods;

  private StringBuilder currTextContent = new StringBuilder();

  public MethodListHandler(MethodList methods, Parser owner) {
    this.methods = methods;
    this.owner = owner;
  }

  public void startElement(
      String namespaceURI, String localName, String qualifiedName, Attributes attributes) {
    currTextContent = new StringBuilder();
  }

  public void endElement(String namespaceURI, String localName, String qualifiedName) {
    if (METHOD_LIST_TAG.equals(localName)) {
      owner.getXmlReader().setContentHandler(owner);
    } else if (STRING_TAG.equals(localName)) {
      this.methods.addMethod(new String(currTextContent));
    }
  }

  public void characters(char[] ch, int start, int length) {
    String content = String.valueOf(ch).substring(start, start + length);
    currTextContent.append(content.trim());
  }
}
