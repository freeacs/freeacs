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
package de.javawi.jstun.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.StringTokenizer;

public class Address {
  int firstOctet;
  int secondOctet;
  int thirdOctet;
  int fourthOctet;

  public Address(int... octets) throws UtilityException {
    if (octets.length != 4) {
      throw new UtilityException("Address is malformed.");
    }
    for (int octet : octets) {
      if (octet < 0 || octet > 255) {
        throw new UtilityException("Address is malformed.");
      }
    }
    this.firstOctet = octets[0];
    this.secondOctet = octets[1];
    this.thirdOctet = octets[2];
    this.fourthOctet = octets[3];
  }

  public Address(String address) throws UtilityException {
    StringTokenizer st = new StringTokenizer(address, ".");
    if (st.countTokens() != 4) {
      throw new UtilityException("4 octets in address string are required.");
    }
    int i = 0;
    while (st.hasMoreTokens()) {
      int temp = Integer.parseInt(st.nextToken());
      if (temp < 0 || temp > 255) {
        throw new UtilityException("Address is in incorrect format.");
      }
      switch (i) {
        case 0:
          firstOctet = temp;
          ++i;
          break;
        case 1:
          secondOctet = temp;
          ++i;
          break;
        case 2:
          thirdOctet = temp;
          ++i;
          break;
        case 3:
          fourthOctet = temp;
          ++i;
          break;
      }
    }
  }

  public Address(byte[] address) throws UtilityException {
    if (address.length < 4) {
      throw new UtilityException("4 bytes are required.");
    }
    firstOctet = Utility.oneByteToInteger(address[0]);
    secondOctet = Utility.oneByteToInteger(address[1]);
    thirdOctet = Utility.oneByteToInteger(address[2]);
    fourthOctet = Utility.oneByteToInteger(address[3]);
  }

  public String toString() {
    return firstOctet + "." + secondOctet + "." + thirdOctet + "." + fourthOctet;
  }

  public byte[] getBytes() throws UtilityException {
    byte[] result = new byte[4];
    result[0] = Utility.integerToOneByte(firstOctet);
    result[1] = Utility.integerToOneByte(secondOctet);
    result[2] = Utility.integerToOneByte(thirdOctet);
    result[3] = Utility.integerToOneByte(fourthOctet);
    return result;
  }

  public InetAddress getInetAddress() throws UtilityException, UnknownHostException {
    byte[] address = new byte[4];
    address[0] = Utility.integerToOneByte(firstOctet);
    address[1] = Utility.integerToOneByte(secondOctet);
    address[2] = Utility.integerToOneByte(thirdOctet);
    address[3] = Utility.integerToOneByte(fourthOctet);
    return InetAddress.getByAddress(address);
  }

  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    try {
      byte[] data1 = getBytes();
      byte[] data2 = ((Address) obj).getBytes();
      return data1[0] == data2[0]
          && data1[1] == data2[1]
          && data1[2] == data2[2]
          && data1[3] == data2[3];
    } catch (UtilityException ue) {
      return false;
    }
  }

  public int hashCode() {
    return (firstOctet << 24) + (secondOctet << 16) + (thirdOctet << 8) + fourthOctet;
  }
}
