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

public class ReflectedFrom extends MappedResponseChangedSourceAddressReflectedFrom {
  private static Logger LOGGER = LoggerFactory.getLogger(ReflectedFrom.class);

  public ReflectedFrom() {
    super(MessageAttribute.MessageAttributeType.ReflectedFrom);
  }

  public static ReflectedFrom parse(byte[] data) throws MessageAttributeParsingException {
    ReflectedFrom result = new ReflectedFrom();
    MappedResponseChangedSourceAddressReflectedFrom.parse(result, data);
    LOGGER.debug("Message Attribute: ReflectedFrom parsed: " + result + ".");
    return result;
  }
}
