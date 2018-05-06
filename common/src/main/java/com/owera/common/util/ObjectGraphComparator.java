package com.owera.common.util;

import java.util.Comparator;

public class ObjectGraphComparator implements Comparator<Object> {

	public int compare(Object arg0, Object arg1) {
		int arrayPos1 = arrayPos((String) arg0);
		int arrayPos2 = -1;
		if (arrayPos1 > -1)
			arrayPos2 = arrayPos((String) arg1);
		if (arrayPos1 < arrayPos2)
			return -1;
		if (arrayPos1 == arrayPos2)
			return ((String) arg0).compareTo((String) arg1);
		if (arrayPos1 > arrayPos2)
			return 1;
		return ((String) arg0).compareTo((String) arg1);
	}

	private int arrayPos(String s) {
		int start = s.indexOf("[");
		int end = s.indexOf("]");
		if (start < end) {
			try {
				return Integer.parseInt(s.substring(start + 1, end));
			} catch (NumberFormatException nfe) {

			}
		}
		return -1;
	}

}
