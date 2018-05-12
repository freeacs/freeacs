package com.github.freeacs.common.lang;

/**
 * I will put logic here for checking whether a string is
 * a number of sorts. This is done to avoid having 
 * try/catch-block for number-conversion in my code, since
 * that doesn't look too nice.
 */

public class Number {

	public static boolean isUnsignedNumber(String str) {
		char[] chars = str.toCharArray();
		int size = chars.length;
		for (int x = 0; x < size; x++) {
			if (chars[x] < 48 || chars[x] > 57)
				return false;
		}
		return true;
	}

	public static boolean isSignedNumber(String str) {
		if (isUnsignedNumber(str.substring(1)) && str.startsWith("-")) 
			return true;
		return false;
	}

	
}
