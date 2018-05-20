package com.github.freeacs.tr069.test.system1.html;

import java.util.ArrayList;
import java.util.List;

public class InputMaker {

	private String name;

	private String type;

	private String value;

	private Integer size;

	private Integer maxLength;

	private Boolean checked;

	List<String> attributes = new ArrayList<String>();

	public InputMaker(String name, String type, String value) {
		this.name = name;
		this.type = type;
		this.value = value;
	}

	public InputMaker(String name, String type) {
		this.name = name;
		this.type = type;
	}

	public void addAttribute(String attribute) {
		attributes.add(attribute);
	}

	public void setSize(int size) {
		this.size = size;
	}

	public void maxLength(int maxLength) {
		this.maxLength = maxLength;
	}

	public void checked(boolean checked) {
		this.checked = checked;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Element makeHtml() {
		Element input = new Element("input");
		input.attribute("name=" + name);
		input.attribute("type=" + type);
		if (value != null)
			input.attribute("value=" + value);
		if (size != null)
			input.attribute("size=" + size.toString());
		if (maxLength != null)
			input.attribute("maxlength=" + maxLength.toString());
		if (checked != null && checked == true)
			input.attribute("checked");
		for (String att : attributes) {
			input.attribute(att);
		}
		return input;
	}
}
