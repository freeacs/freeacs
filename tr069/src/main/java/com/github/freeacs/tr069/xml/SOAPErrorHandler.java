package com.github.freeacs.tr069.xml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ErrorHandler;

public class SOAPErrorHandler implements ErrorHandler {
  private static final transient Logger log = LoggerFactory.getLogger(SOAPErrorHandler.class);

  public void error(org.xml.sax.SAXParseException sAXParseException) {
    log.error(sAXParseException.getLocalizedMessage());
  }

  public void fatalError(org.xml.sax.SAXParseException sAXParseException) {
    log.error(sAXParseException.getLocalizedMessage());
  }

  public void warning(org.xml.sax.SAXParseException sAXParseException) {
    log.warn(sAXParseException.getLocalizedMessage());
  }
}
