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

import de.javawi.jstun.util.Utility;
import de.javawi.jstun.util.UtilityException;

public class ConnectionRequestBinding extends MessageAttribute {
  private byte[] data;

  public ConnectionRequestBinding() {
    super(MessageAttribute.MessageAttributeType.ConnectionRequestBinding);
  }

  public void setData(byte[] data) {
    this.data = data;
  }

  public static ConnectionRequestBinding parse(byte[] data)
      throws MessageAttributeParsingException {
    String str = new String(data);
    if (!"dslforum.org/TR-111".equals(str.trim())) {
      throw new MessageAttributeParsingException("Attribute doesn't contain dslforum.org/TR-111");
    }
    ConnectionRequestBinding result = new ConnectionRequestBinding();
    result.setData(data);
    return result;
  }

  @Override
  public byte[] getBytes() throws UtilityException {
    int length = data.length;
    if (length % 4 != 0) {
      length += 4 - length % 4;
    }
    length += 4;
    byte[] result = new byte[length];
    // message attribute header
    // type
    System.arraycopy(Utility.integerToTwoBytes(typeToInteger(type)), 0, result, 0, 2);
    // length
    System.arraycopy(Utility.integerToTwoBytes(length - 4), 0, result, 2, 2);

    System.arraycopy(data, 0, result, 4, data.length);
    return result;
  }
}
