package com.github.freeacs.tr069.xml;

public class ParameterInfoStruct {
	private String name;
	private boolean writable;
	private boolean inspect;

	public ParameterInfoStruct() {
		this.name = null;
		this.writable = false;
	}

	public ParameterInfoStruct(String name, boolean writable) {
		this.name = name;
		this.writable = writable;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isWritable() {
		return this.writable;
	}

	public void setWritable(boolean writable) {
		this.writable = writable;
	}

}
