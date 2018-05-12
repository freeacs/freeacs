package com.github.freeacs.common.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NaturalComparator implements Comparator<String> {

	Pattern numberPattern = Pattern.compile("\\d+");

	public static void main(String[] args) {
		List<String> list = new ArrayList<String>();
		list.add("Hei");
		list.add("Hei");
		list.add("Pa");
		list.add("Deg");
		list.add("Hei.10");
		list.add("Pa.20");
		list.add("Deg.101");
		list.add("Deg.000101");
		list.add("Deg.1010");
		list.add("Deg.0999");
		list.add("Hei.10.Pa.2.Deg");
		list.add("Hei.10.Pa.2");
		list.add("Hei.10.Pa.Deg");
		list.add("Hei.10.Pa.20.Deg");
		list.add("Hei.10.Pa.101.Deg");
		list.add("10.20.30.40");
		list.add("20.20.30.40");
		list.add("101.20.30.40");
		list.add("1.201.301.401");
		list.add("version-1.0.1");
		list.add("version-1.9.1");
		list.add("version-1.70.1");
		List<String> copy = new ArrayList<String>(list);
		Collections.sort(list, new NaturalComparator());
		Collections.sort(copy); // regular string-sort
		System.out.println(String.format("%30s", "NaturalComparator") + "\t" + "StringComparator");
		System.out.println(String.format("%30s", "-----------------") + "--" + "----------------");
		for (int i = 0; i < list.size(); i++) {
			System.out.println(String.format("%30s", list.get(i)) + "\t" + copy.get(i));
		}
	}

	public int compare(String s1, String s2) {
		if (s1 != null) {
			if (s2 != null)
				return compareImpl(s1, s2);
			else
				return 1;
		} else if (s2 != null)
			return -1;
		return 0;

	}

	private Integer powerOfTen(int exponent) {
		switch (exponent) {
		case 1:
			return 10;
		case 2:
			return 100;
		case 3:
			return 1000;
		case 4:
			return 10000;
		case 5:
			return 100000;
		case 6:
			return 1000000;
		case 7:
			return 10000000;
		case 8:
			return 100000000;
		default:
			break;
		}
		int result = 1;
		for (int i = 0; i < exponent; i++) {
			result *= 10;
		}
		return result;
	}

	private Integer toNumber(String s) {
		int number = 0;
		int exponent = -1;
		for (int i = s.length() - 1; i > -1; i--) {
			exponent++;
			int digit = s.charAt(i) - 48;
			number += digit * powerOfTen(exponent);
		}
		return number;
	}

	/*
	 * This compare routine takes about 9200 ns to perform. The logic goes like this:
	 * 1. Always search for a number.
	 * 2. If a number exists in both strings - perform special compare. Else: string compare.
	 * 3. Special compare:
	 * 3.1. Retrieve the strings before the number. Regular string compare. Return if difference.
	 * 3.2. Retrieve the numbers from both string. Regular int subtraction. Return if difference.
	 * 3.3. Continue with rest of the string, perform 3.1 and 3.2 in a loop.
	 * 3.4. If no more numbers are found : string compare.
	 */
	private int compareImpl(String str1, String str2) {
		Matcher matcher1 = numberPattern.matcher(str1);
		Matcher matcher2 = null;
		int lastMatchEndPos1 = 0;
		int lastMatchEndPos2 = 0;
		while (matcher1.find()) {
			if (matcher2 == null)
				matcher2 = numberPattern.matcher(str2);
			if (!matcher2.find())
				break;
			String str1BeforeNumber = str1.substring(lastMatchEndPos1, matcher1.start());
			String str2BeforeNumber = str2.substring(lastMatchEndPos2, matcher2.start());
			int compareStrBeforeNumber = str1BeforeNumber.compareTo(str2BeforeNumber);
			if (compareStrBeforeNumber != 0) {
				return compareStrBeforeNumber;
			}
			Integer i1 = toNumber(str1.substring(matcher1.start(), matcher1.end()));
			Integer i2 = toNumber(str2.substring(matcher2.start(), matcher2.end()));
			if (i1.intValue() == i2.intValue()) {
				lastMatchEndPos1 = matcher1.end();
				lastMatchEndPos2 = matcher2.end();
			} else {
				int result = i1 - i2;
				return result;
			}
		}
		String restOfStr1 = str1.substring(lastMatchEndPos1);
		String restOfStr2 = str2.substring(lastMatchEndPos2);
		int result = restOfStr1.compareTo(restOfStr2);
		return result;
	}
}
