package com.github.freeacs.shell.tools;


import com.github.freeacs.shell.util.StringUtil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CompareTF {

	/**
	 * The whole purpose of this program is to show that
	 * file 2 is a subset of file 1. You can choose
	 * which column to compare in args 2 and 4. Each line
	 * of the file is expected to be a set of arguments
	 * delimited by whitespace.
	 * @param args
	 * @throws IOException 
	 */
	public static void main2(String[] args) throws IOException {
		BufferedReader br1 = new BufferedReader(new FileReader(args[0]));
		Integer columnIndex1 = new Integer(args[1]);
		BufferedReader br2 = new BufferedReader(new FileReader(args[2]));
		Integer columnIndex2 = new Integer(args[3]);
		String line = null;
		Set<String> set1 = new HashSet<String>();
		Set<String> set2 = new HashSet<String>();
		while ((line = br1.readLine()) != null) {
			String[] strArr = StringUtil.split(line);
			set1.add(strArr[columnIndex1]);
		}
		while ((line = br2.readLine()) != null) {
			String[] strArr = StringUtil.split(line);
			set2.add(strArr[columnIndex2]);
		}
		for (String s : set2) {
			if (!set1.contains(s)) {
				System.err.println(args[1] + " does not contain " + s + " from " + args[3]);
			} else {
				System.out.println(s + " is OK");
			}
		}
	}

	public static void main(String[] args) throws IOException {
		BufferedReader br1 = new BufferedReader(new FileReader(args[0]));
		Integer columnIndex1 = new Integer(args[1]);
		BufferedReader br2 = new BufferedReader(new FileReader(args[2]));
		Integer columnIndex2 = new Integer(args[3]);
		String line = null;
		Map<String, String> map1 = new HashMap<String, String>();
		Map<String, String> map2 = new HashMap<String, String>();
		while ((line = br1.readLine()) != null) {
			String[] strArr = StringUtil.split(line);
			map1.put(strArr[columnIndex1], line);
		}
		while ((line = br2.readLine()) != null) {
			String[] strArr = StringUtil.split(line);
			map2.put(strArr[columnIndex2], line);
		}
		if (map1.size() >= map2.size()) {
			for (String key : map1.keySet()) {
				if (!map2.containsKey(key)) {
					System.out.println(map1.get(key));
				}
			}
		}
	}

}
