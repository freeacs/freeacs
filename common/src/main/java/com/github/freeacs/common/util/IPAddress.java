package com.github.freeacs.common.util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class IPAddress {
  /**
   * Returns true if and only if the given IP address is outside the ranges 10.0.0.0-10.255.255.255,
   * 172.16.0.0-172.31.255.255 or 192.168.0.0-192.168.255.255
   *
   * @param ip An IPv4 address on standard dot-separated form, e.g. 192.168.1.1 or 85.112.159.45.
   * @return Whether the IP address is public or not.
   */
  public static boolean isPublic(String ip) {
    Inet4Address address;
    try {
      address = (Inet4Address) InetAddress.getByName(ip);
    } catch (UnknownHostException exception) {
      return false; // assuming no logging, exception handling required
    }
    return !address.isSiteLocalAddress()
        && !address.isAnyLocalAddress()
        && !address.isLinkLocalAddress()
        && !address.isLoopbackAddress()
        && !address.isMulticastAddress();
  }
}
