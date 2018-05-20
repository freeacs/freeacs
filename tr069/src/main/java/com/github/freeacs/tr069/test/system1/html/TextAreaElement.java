package com.github.freeacs.tr069.test.system1.html;

public class TextAreaElement extends Element {

	private String name;

	private int cols;

	private int rows;

	private boolean wrap = true;

	private String text;

	public TextAreaElement(String name, int cols, int rows, boolean wrap) {
		this.name = name;
		this.cols = cols;
		this.wrap = wrap;
		this.rows = rows;
	}

	public void addText(String text) {
		this.text = text;
	}

	public String toString(String tab) {
		String wrapStr = "off";
		if (wrap)
			wrapStr = "on";

		String retStr = tab + "<textarea name=\"" + name + "\" cols=\"" + cols + "\" rows=\"" + rows + "\" wrap=\"" + wrapStr + "\">";
		retStr += text;
		retStr += "</textarea>\n";
		return retStr;
	}
}
