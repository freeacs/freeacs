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

public class SourceAddress extends MappedResponseChangedSourceAddressReflectedFrom {
  private static Logger LOGGER = LoggerFactory.getLogger(SourceAddress.class);

  public SourceAddress() {
    super(MessageAttribute.MessageAttributeType.SourceAddress);
  }

  public static MessageAttribute parse(byte[] data) throws MessageAttributeParsingException {
    SourceAddress sa = new SourceAddress();
    MappedResponseChangedSourceAddressReflectedFrom.parse(sa, data);
    LOGGER.debug("Message Attribute: Source Address parsed: " + sa + ".");
    return sa;
  }
}
