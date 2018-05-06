package com.owera.common.nicetohave;

import java.util.ArrayList;
import java.util.List;

public class StringUtility {

	public static String join(String[] stringArray, String delimiterString, boolean joinNullValues) {
		List<String> list = new ArrayList<String>();
		for (int i = 0; stringArray != null && i < stringArray.length; i++) {
			list.add(stringArray[i]);
		}
		return join(list, delimiterString, joinNullValues);
	}

	/**
	 * En metode for å joine en liste av strings til en lang string, delt med delimiterString
	 * (fex: " " eller "!::!"). Dersom joinNullValues == true, så vil også null-objekter i listen
	 * bli joinet slik: (":" er delimiter, liste er [Hei, på, NULL, deg])
	 * Hei:på::deg
	 * (NB: Ingen delimiterString først eller sist i retur-stringen)
	 * (NB: Unntak fra regelen over: Dersom det første eller siste elementet i listen er NULL
	 * så vil delimiterStringen kunne komme først (og/eller) sist i retur-stringen)
	 */

	public static String join(List<String> stringList, String delimiterString, boolean joinNullValues) {
		String retStr = "";
		for (int i = 0; stringList != null && i < stringList.size(); i++) {
			String stringElem = null;
			if (stringList.get(i) != null)
				retStr += stringList.get(i);
			if (i < stringList.size() - 1) {
				if (joinNullValues && stringElem == null)
					retStr += delimiterString;
				else if (stringElem != null)
					retStr += delimiterString;
			}
		}
		return retStr;
	}
	
}
