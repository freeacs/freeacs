package com.github.freeacs.tr069.xml;

import java.util.ArrayList;

public class ParameterList {

	private ArrayList<ParameterValueStruct> params;
	private ArrayList<ParameterInfoStruct> info;
	private ArrayList<ParameterAttributeStruct> attributes;

	public ParameterList() {
		this.params = new ArrayList<ParameterValueStruct>();
		this.info = new ArrayList<ParameterInfoStruct>();
		this.attributes = new ArrayList<ParameterAttributeStruct>();
	}

	public void addParameterValueStruct(ParameterValueStruct param) {
		this.params.add(param);
	}
	
	public void addParameterInfoStruct(ParameterInfoStruct param) {
		this.info.add(param);
	}

	public void addParameterAttributeStruct(ParameterAttributeStruct attr) {
		this.attributes.add(attr);
	}

	public ArrayList<ParameterValueStruct> getParameterValueList() {
		return this.params;
	}

	public ArrayList<ParameterInfoStruct> getParameterInfoList() {
		return this.info;
	}

	public ArrayList<ParameterAttributeStruct> getParameterAttributeList() {
		return this.attributes;
	}

	public void addOrChangeParameterValueStruct(String key, String value, String type) {
		boolean changed = false;
		for (ParameterValueStruct struct : this.params) {
			if (struct.getName().equals(key)) {
				struct.setValue(value);
				changed = true;
				break;
			}
		}
		if (!changed)
			this.params.add(new ParameterValueStruct(key, value, type));

	}

}
