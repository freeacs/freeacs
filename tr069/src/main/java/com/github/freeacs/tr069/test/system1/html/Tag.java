package com.github.freeacs.tr069.test.system1.html;

import java.util.ArrayList;
import java.util.List;

public class Tag {

	private String tag;

	private String value;

	private List<Attribute> attributes = new ArrayList<Attribute>();

	// Some tags should NOT be ended properly (<li>, <option>...)
	private boolean noEnd = false;

	public Tag(String t, String s) {
		tag = t;
		value = s;
	}

	public Tag(String t, String s, boolean noEnd) {
		tag = t;
		value = s;
		this.noEnd = noEnd;
	}

	public void attribute(String attribute) {
		attributes.add(new Attribute(attribute));
	}

	public String start(boolean noSubElements) {
		String s = "<" + tag;
		for (int i = 0; attributes != null && i < attributes.size(); i++) {
			Attribute att = attributes.get(i);
			s += " " + att.toString();
		}
		if ((noSubElements && value == null) || noEnd)
			s += " /";
		s += ">";
		return s;
	}

	public String value() {
		if (value == null)
			return "";
		return value;
	}

	public String end() {
		if (noEnd)
			return "";
		else
			return "</" + tag + ">";
	}
	
	public String toString() {
		return tag;
	}

}
