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

public interface MessageAttributeInterface {
  public enum MessageAttributeType {
    MappedAddress,
    ResponseAddress,
    ChangeRequest,
    SourceAddress,
    ChangedAddress,
    Username,
    Password,
    MessageIntegrity,
    ErrorCode,
    UnknownAttribute,
    ReflectedFrom,
    ConnectionRequestBinding,
    BindingChange,
    Dummy
  };

  static final int MAPPEDADDRESS = 0x0001;
  static final int RESPONSEADDRESS = 0x0002;
  static final int CHANGEREQUEST = 0x0003;
  static final int SOURCEADDRESS = 0x0004;
  static final int CHANGEDADDRESS = 0x0005;
  static final int USERNAME = 0x0006;
  static final int PASSWORD = 0x0007;
  static final int MESSAGEINTEGRITY = 0x0008;
  static final int ERRORCODE = 0x0009;
  static final int UNKNOWNATTRIBUTE = 0x000a;
  static final int REFLECTEDFROM = 0x000b;
  static final int CONNECTIONREQUESTBINDING = 0xC001;
  static final int BINDINGCHANGE = 0xC002;
  static final int DUMMY = 0x0000;
}
