/*
 * This file is part of JSTUN. 
 * 
 * Copyright (c) 2005 Thomas King <king@t-king.de> - All rights
 * reserved.
 * 
 * This software is licensed under either the GNU Public License (GPL),
 * or the Apache 2.0 license. Copies of both license agreements are
 * included in this distribution.
 */

package de.javawi.jstun.attribute;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResponseAddress extends MappedResponseChangedSourceAddressReflectedFrom {
	private static Logger LOGGER = LoggerFactory.getLogger(ResponseAddress.class);

	public ResponseAddress() {
		super(MessageAttribute.MessageAttributeType.ResponseAddress);
	}

	public static MessageAttribute parse(byte[] data) throws MessageAttributeParsingException {
		ResponseAddress ra = new ResponseAddress();
		MappedResponseChangedSourceAddressReflectedFrom.parse(ra, data);
		LOGGER.debug("Message Attribute: Response Address parsed: " + ra.toString() + ".");
		return ra;
	}
}
