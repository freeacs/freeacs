package com.owera.tr069client;

public class TagValue {
	private String value;
	private int endPos;

	public TagValue(String value, int endPos) {
		this.value = value;
		this.endPos = endPos;
	}

	public String getValue() {
		return value;
	}

	public int getEndPos() {
		return endPos;
	}
	
	public String toString() {
		return this.value;  
	}
}
