package com.github.freeacs.shell.output;

public class Column {
	private int maxWidth = 0;

	public void incWidthIfNecessary(int width) {
		if (width > maxWidth)
			maxWidth = width;
	}

	public int getMaxWidth() {
		return maxWidth;
	}
}
