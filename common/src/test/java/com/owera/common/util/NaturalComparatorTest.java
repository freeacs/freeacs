package com.owera.common.util;

import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import com.owera.common.util.NaturalComparator;

public class NaturalComparatorTest {

	public static void main(String[] args) {
		Map<String, String> m = new TreeMap<String, String>(new NaturalComparator());

		m.put("1", "1");
		m.put("2", "2");
		m.put("3", "3");
		m.put("10", "10");
		m.put("Abc", "Abc");
		m.put("abc", "abc");

		for (Entry<String, String> e : m.entrySet()) {
			System.out.println(e.getKey());
		}
		System.out.println(m.get(null));

	}

}
