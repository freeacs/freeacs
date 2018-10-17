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
  enum MessageAttributeType {
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
  }

  int MAPPEDADDRESS = 0x0001;
  int RESPONSEADDRESS = 0x0002;
  int CHANGEREQUEST = 0x0003;
  int SOURCEADDRESS = 0x0004;
  int CHANGEDADDRESS = 0x0005;
  int USERNAME = 0x0006;
  int PASSWORD = 0x0007;
  int MESSAGEINTEGRITY = 0x0008;
  int ERRORCODE = 0x0009;
  int UNKNOWNATTRIBUTE = 0x000a;
  int REFLECTEDFROM = 0x000b;
  int CONNECTIONREQUESTBINDING = 0xC001;
  int BINDINGCHANGE = 0xC002;
  int DUMMY = 0x0000;
}
