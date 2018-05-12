package com.github.freeacs.common.nicetohave;

/**
 * @author ME3
 */
public class ValidationInput {
	private String name;
	private String value;
	private boolean required;
	private int[][] length;
	private String format;
	private String[] set;

	public ValidationInput(String name, String value, boolean required) {
		this.name = name;
		this.value = value;
		this.required = required;
	}

	public String getFormat() {
		return format;
	}

	public int[][] getLength() {
		return length;
	}

	public String getName() {
		return name;
	}

	public boolean isRequired() {
		return required;
	}

	public String[] getSet() {
		return set;
	}

	public String getValue() {
		return value;
	}

	public void setFormat(String string) {
		format = string;
	}

	public void setLength(int[][] is) {
		length = is;
	}

	public void setLength(int[] i) {
		length = new int[][] { i };
	}

	public void setLength(int i) {
		int[] iArray = new int[] { i, i };
		length = new int[][] { iArray };
	}

	public void setName(String string) {
		name = string;
	}

	public void setRequired(boolean b) {
		required = b;
	}

	public void setSet(String[] strings) {
		set = strings;
	}

	public void setValue(String string) {
		value = string;
	}

}
