package com.github.freeacs.tr069.test.system1.html;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Attribute {

	private String attribute;

	public Attribute(String attribute) {
		if (Pattern.matches("^[^\"=]+$", attribute)) {
			this.attribute = attribute;
			return;
		}
		if (Pattern.matches("^([^\"=]+)=(\"[^\"]+\")$", attribute)) {
			this.attribute = attribute;
			return;
		}
		Pattern p = Pattern.compile("^([^\"=]+)=([^\"]+)$");
		Matcher m = p.matcher(attribute);
		if (m.matches()) {
			setAttribute(m.group(1), m.group(2));
		} else {
			throw new IllegalArgumentException("The attribute is not one attribute only: " + attribute);
		}
	}

	private void setAttribute(String k, String v) {
		k = k.trim();
		v = v.trim();
		if (!v.startsWith("\""))
			v = "\"" + v;
		if (!v.endsWith("\""))
			v += "\"";
		attribute = k + "=" + v;
	}

	public String toString() {
		return attribute;
	}

}
