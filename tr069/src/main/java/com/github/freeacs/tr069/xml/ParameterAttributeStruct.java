package com.github.freeacs.tr069.xml;

public class ParameterAttributeStruct {
	private String name;
	private int notifcation;

	public ParameterAttributeStruct() {
	}

	public ParameterAttributeStruct(String name, int notifcation) {
		this.name = name;
		this.notifcation = notifcation;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getNotifcation() {
		return this.notifcation;
	}

	public void setNotifcation(int notifcation) {
		this.notifcation = notifcation;
	}
}
