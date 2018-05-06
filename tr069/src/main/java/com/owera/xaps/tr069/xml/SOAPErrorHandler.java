package com.owera.xaps.tr069.xml;

import org.xml.sax.ErrorHandler;

import com.owera.common.log.Logger;

/**
 * 
 * @author knut petter
 *
 */

public class SOAPErrorHandler implements ErrorHandler {

	private static final transient Logger log = new Logger(SOAPErrorHandler.class);

	public SOAPErrorHandler() {
	}

	public void error(org.xml.sax.SAXParseException sAXParseException) throws org.xml.sax.SAXException {
		log.error(sAXParseException.getLocalizedMessage());
	}

	public void fatalError(org.xml.sax.SAXParseException sAXParseException) throws org.xml.sax.SAXException {
		log.fatal(sAXParseException.getLocalizedMessage());
	}

	public void warning(org.xml.sax.SAXParseException sAXParseException) throws org.xml.sax.SAXException {
		log.warn(sAXParseException.getLocalizedMessage());
	}

}
