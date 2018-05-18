package com.github.freeacs.tr069.xml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ErrorHandler;

public class SOAPErrorHandler implements ErrorHandler {

	private static final transient Logger log = LoggerFactory.getLogger(SOAPErrorHandler.class);

	public SOAPErrorHandler() {
	}

	public void error(org.xml.sax.SAXParseException sAXParseException) throws org.xml.sax.SAXException {
		log.error(sAXParseException.getLocalizedMessage());
	}

	public void fatalError(org.xml.sax.SAXParseException sAXParseException) throws org.xml.sax.SAXException {
		log.error(sAXParseException.getLocalizedMessage());
	}

	public void warning(org.xml.sax.SAXParseException sAXParseException) throws org.xml.sax.SAXException {
		log.warn(sAXParseException.getLocalizedMessage());
	}

}
