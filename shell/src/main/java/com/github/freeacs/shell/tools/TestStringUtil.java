package com.github.freeacs.shell.tools;


import com.github.freeacs.shell.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class TestStringUtil {

//	private static Pattern pattern = Pattern.compile("(\"[^\"]+\")|([^ \"\t]+)");

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String s = "call script.xss : \"Testperf i dag\" TestperfP";
		System.out.println("New method:");
		for (String str : StringUtil.split(s)) {
			System.out.println(str);
		}
		System.out.println("\nOld method:");
		for (String str : split(s)) {
			System.out.println(str);
		}
	}

	//	private static String[] split(String s) {
	//		Matcher m = pattern.matcher(s);
	//		int pos = 0;
	//		List<String> commands = new ArrayList<String>();
	//		while (m.find(pos)) {
	//			String group1 = m.group(1);
	//			String group2 = m.group(2);
	//			if (group1 != null)
	//				commands.add(group1);
	//			else if (group2 != null)
	//				commands.add(group2);
	//			pos = m.end();
	//		}
	//		String[] strArray = new String[commands.size()];
	//		commands.toArray(strArray);
	//		return strArray;
	//	}

		public static String[] split(String string) {
		String s = string;
		if (s != null)
			s = s.trim();
		String[] sArr = s.split("([ \t])+");
		List<String> list = new ArrayList<String>();
		boolean insideQuote = false;
		String tmp = null;
		for (int i = 0; i < sArr.length; i++) {
			if (!insideQuote && sArr[i].indexOf("\"") == -1)
				list.add(sArr[i]);
			else if (!insideQuote && sArr[i].startsWith("\"")) {
				insideQuote = true;
				tmp = sArr[i].substring(1);
				if (tmp.endsWith("\"")) {
					insideQuote = false;
					tmp = tmp.substring(0, tmp.length() - 1);
					list.add(tmp);
					tmp = null;
				}
			} else if (insideQuote && sArr[i].endsWith("\"")) {
				insideQuote = false;
				tmp += " " + sArr[i].substring(0, sArr[i].length() - 1);
				list.add(tmp);
				tmp = null;
			} else { //insideQuote && !sArr[i].endsWith("\"")
				tmp += " " + sArr[i];
			}
		}
		String[] retArr = new String[list.size()];
		for (int i = 0; i < list.size(); i++) {
			retArr[i] = list.get(i);
		}
		return retArr;
	}

}
