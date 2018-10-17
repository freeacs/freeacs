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

public class ChangedAddress extends MappedResponseChangedSourceAddressReflectedFrom {
  private static Logger LOGGER = LoggerFactory.getLogger(ChangedAddress.class);

  public ChangedAddress() {
    super(MessageAttribute.MessageAttributeType.ChangedAddress);
  }

  public static MessageAttribute parse(byte[] data) throws MessageAttributeParsingException {
    ChangedAddress ca = new ChangedAddress();
    MappedResponseChangedSourceAddressReflectedFrom.parse(ca, data);
    LOGGER.debug("Message Attribute: Changed Address parsed: " + ca + ".");
    return ca;
  }
}
