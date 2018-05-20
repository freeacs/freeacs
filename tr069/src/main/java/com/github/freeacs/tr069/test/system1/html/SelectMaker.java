package com.github.freeacs.tr069.test.system1.html;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class SelectMaker {

	private Object[] objects;

	private String selectName;

	private String selectedValue;

	private String nameMethod;
	
	private String valueMethod;

	private String extraChoice;

	private String eventAttribute;
	
	private List<String> attributes;

	private int size = 1;

	private Boolean multiple = false;

	private String extraChoiceValue;

	public SelectMaker(Object[] objects, String name, String value, String selectName) {
		this.attributes=new ArrayList<String>();
		this.selectName = selectName;
		this.objects = objects;
		this.nameMethod = name;
		this.valueMethod = value;
		
		if (selectName == null || objects == null || nameMethod == null) {
			throw new IllegalArgumentException("It is not allowed to pass any null-arguments to the constructor of SelectMaker");
		}
	}

	public void setSelectedKeyword(String selectedValue) {
		this.selectedValue = selectedValue;
	}

	public void setExtraChoice(String extraChoice,String value) {
		this.extraChoice = extraChoice;
		this.extraChoiceValue = value;
	}

	public void setEvent(String eventType, String action) {
		this.eventAttribute = eventType + "=\"" + action + "\"";
	}

	public void addAttribute(String attribute) {
		this.attributes.add(attribute);
	}
	
	public void setSize(int size) {
		this.size = size;
	}

	public void setMultiple(boolean multiple) {
		this.multiple = multiple;
	}

	public Element makeHtml() throws Exception {
		Element select = new Element("select");
		select.attribute("name=" + selectName);
		select.attribute("size=" + size);
		if (attributes != null)
			for(String s:attributes)
				select.attribute(s);
		if (multiple != null && multiple == true)
			select.attribute("multiple");
		if (eventAttribute != null)
			select.attribute(eventAttribute);
		if (extraChoice != null)
			select.option(extraChoice, false).attribute("value="+extraChoiceValue);
		for (Object o : objects) {
			Method m = o.getClass().getMethod(nameMethod, (Class[]) null);
			Object name = m.invoke(o, (Object[]) null);
			String nameStr;
			if(name instanceof String)
				nameStr = (String)name;
			else
				nameStr = name.toString();
			
			Element option;
			if (selectedValue != null && selectedValue.equals(nameStr)) {
				option = select.option(nameStr, true);
			} else {
				option = select.option(nameStr, false);
			}
			
			if(valueMethod!=null){
				m = o.getClass().getMethod(valueMethod, (Class[]) null);
				Object  value = m.invoke(o, (Object[]) null);
				String valueStr;
				if(value instanceof String)
					valueStr = (String)value;
				else
					valueStr = value.toString();
				option.attribute("value="+valueStr);
			}
		}
		return select;
	}
}
