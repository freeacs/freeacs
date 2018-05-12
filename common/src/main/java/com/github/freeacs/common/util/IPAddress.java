package com.github.freeacs.common.util;

public class IPAddress {

	/**
	 * Returns true if and only if the given IP address is outside the ranges
	 * 10.0.0.0-10.255.255.255, 172.16.0.0-172.31.255.255 or 192.168.0.0-192.168.255.255
	 * @param ipString An IPv4 address on standard dot-separated form,
	 * e.g. 192.168.1.1 or 85.112.159.45. 
	 * @return Whether the IP address is public or not.
	 */
	public static boolean isPublic(String addr) {
		if (addr.startsWith("192.168.") || addr.startsWith("10."))
			return false;
		String[] octets = addr.split("\\.");
		int secondOctet;
		try {
			secondOctet = Integer.parseInt(octets[1]);
		} catch (NumberFormatException e) {
			return false;
		}
		if (addr.startsWith("172.") && secondOctet >= 16 && secondOctet < 32)
			return false;
		return true;
	}

}
