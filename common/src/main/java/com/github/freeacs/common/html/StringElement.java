package com.github.freeacs.common.html;

public class StringElement extends Element {

	private String s;

	public StringElement(String s) {
		this.s = s;
	}

	public String toString(String tab) {
		if (s != null)
			return tab + s + "\n";
		return "";
	}
}
