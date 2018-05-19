package com.github.freeacs.base.http;


import com.github.freeacs.base.Log;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class Util {

	public static String getRealm() {
		return "xaps";
	}

	public static boolean startsWithIgnoreCase(String str, String prefix) {
		String str_prefix = str.substring(0, prefix.length());
		return (str_prefix.compareToIgnoreCase(prefix) == 0);
	}

	public static String removePrefix(String str, String prefix) {
		if (startsWithIgnoreCase(str, prefix))
			return str.substring(prefix.length(), str.length());
		else
			return null;
	}

	public static String base64decode(String str) {
		byte[] data1 = str.getBytes();
		byte[] data2 = org.apache.commons.codec.binary.Base64.decodeBase64(data1);
		return new String(data2);
	}

	/**
	 * Removes the quotes on a string. RFC2617 states quotes are optional for
	 * all parameters except realm.
	 */
	public static String removeQuotes(String quotedString, boolean quotesRequired) {
		//support both quoted and non-quoted
		if (quotedString.length() > 0 && quotedString.charAt(0) != '"' && !quotesRequired) {
			return quotedString;
		} else if (quotedString.length() > 2) {
			return quotedString.substring(1, quotedString.length() - 1);
		} else {
			return "";
		}
	}

	/**
	 * Removes the quotes on a string.
	 */
	public static String removeQuotes(String quotedString) {
		return removeQuotes(quotedString, false);
	}

	/**
	 * Convert the authentication username to unitid (should be 1:1, but there might be some 
	 * vendor specific problems to solve...
	 * @throws UnsupportedEncodingException 
	 */
	public static String username2unitId(String username) {
		String unitId;

		// Fix for Thompson ST780
		unitId = username.replaceFirst("SpeedTouch780", "SpeedTouch 780");
		//TODO: Hack with Zyxel - remove some time in the future (added fall 2008)
		unitId = unitId.replaceFirst("P-2602HW-F3", "P2602HWF3");
		// This will ensure that the OUI part of the unitId is always uppercase-
		// since that is consistent with the OUI sent in the Inform (ref TR-106)
		// The reason to add this at all, is that A1/2 of TR-069 no longer 
		// requires that the OUI transmitted in authentication must be uppercase.
		//		unitId = unitId.toUpperCase().substring(0,6) + unitId.substring(6);
		try {
			unitId = URLDecoder.decode(unitId, "UTF-8");
		} catch (UnsupportedEncodingException uee) {
			Log.warn(Util.class, "Not possible to decode username using UTF-8 - username may possibly be wrong", uee);
		}
		return unitId;
	}

}
