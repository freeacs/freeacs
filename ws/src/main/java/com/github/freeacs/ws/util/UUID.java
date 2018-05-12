package com.github.freeacs.ws.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class UUID {

	private final long mostSigBits;

	//	public final static String NAMESPACE_URL = "6ba7b811-9dad-11d1-80b4-00c04fd430c8";
	/*
	 * The least significant 64 bits of this UUID.
	 *
	 * @serial
	 */
	private final long leastSigBits;

	public static void main(String[] args) {
		String MAC = "00219400151c";
		//		String str = "xaps://vyke.com?uniqueid=freyr";
		String str = "opp://owera.com/;mac=" + MAC;
		UUID uuid = UUID.nameUUIDFromBytes(str.getBytes());
		System.out.println(uuid);

	}

	public static UUID nameUUIDFromBytes(byte[] name) {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException nsae) {
			throw new InternalError("MD5 not supported");
		}
		//		byte[] bytes = md.digest(name);
		md.reset();
		//		md.update(NAMESPACE_URL.getBytes());
		md.update(name);
		byte[] bytes = md.digest(name);
		bytes[6] &= 0x0f; /* clear version        */
		bytes[6] |= 0x30; /* set to version 3     */
		bytes[8] &= 0x3f; /* clear variant        */
		bytes[8] |= 0x80; /* set to IETF variant  */
		return new UUID(bytes);
	}

	private UUID(byte[] data) {
		long msb = 0;
		long lsb = 0;
		assert data.length == 16;
		for (int i = 0; i < 8; i++)
			msb = (msb << 8) | (data[i] & 0xff);
		for (int i = 8; i < 16; i++)
			lsb = (lsb << 8) | (data[i] & 0xff);
		this.mostSigBits = msb;
		this.leastSigBits = lsb;
	}

	public String toString() {
		return (digits(mostSigBits >> 32, 8) + "-" + digits(mostSigBits >> 16, 4) + "-" + digits(mostSigBits, 4) + "-" + digits(leastSigBits >> 48, 4) + "-" + digits(leastSigBits, 12));
	}

	/** Returns val represented by the specified number of hex digits. */
	private static String digits(long val, int digits) {
		long hi = 1L << (digits * 4);
		return Long.toHexString(hi | (val & (hi - 1))).substring(1);
	}

}
