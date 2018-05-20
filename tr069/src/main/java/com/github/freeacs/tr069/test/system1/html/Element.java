package com.github.freeacs.tr069.test.system1.html;

import java.util.ArrayList;
import java.util.List;

public class Element {
	private List<Element> elements = new ArrayList<Element>();

	private Tag tag;

	public Element() {

	}

	public Element(String tag) {
		this.tag = new Tag(tag, null);
	}

	public Element(String tag, String val) {
		this.tag = new Tag(tag, val);
	}

	public Element(String tag, String val, boolean noEnd) {
		this.tag = new Tag(tag, val, noEnd);
	}

	private Element tag(String tag) {
		elements.add(new Element(tag));
		return elements.get(elements.size() - 1);
	}

	private Element tag(String tag, String s) {
		elements.add(new Element(tag, s));
		return elements.get(elements.size() - 1);
	}

	private Element tag(String tag, String s, boolean noEnd) {
		elements.add(new Element(tag, s, noEnd));
		return elements.get(elements.size() - 1);
	}

	public void add(String s) {
		elements.add(new StringElement(s));
	}

	public void add(Element e) {
		elements.add(e);
	}

	public void add(List<Element> elems) {
		for (Element e : elems) {
			elements.add(e);
		}
	}

	public void attribute(String attribute) {
		tag.attribute(attribute);
	}

	protected Element addAttributes(Element e, String... attributes) {
		if (attributes != null) {
			for (String att : attributes) {
				e.attribute(att);
			}
		}
		return e;
	}

	public String toString() {
		return tag.toString();
	}

	public String toString(String tab) {
		StringBuilder sb = new StringBuilder();
		if (tab == null)
			tab = "";
		boolean noSubElements = (elements.size() == 0);
		boolean quickEnd = false;
		if (tag != null) {
			String tagStart = tag.start(noSubElements);
			if (tagStart.endsWith("/>"))
				quickEnd = true;
			sb.append(tab + tagStart + "\n");
		}
		for (int i = 0; i < elements.size(); i++) {
			String elemStr = elements.get(i).toString(tab + "\t");
			if (elemStr.length() < 130) {
				elemStr = elemStr.replaceAll("\n\t+", "");
			}
			sb.append(elemStr);
		}
		if (tag != null && !quickEnd) {
			if (tag.value().length() > 0)
				sb.append(tab + "\t" + tag.value() + "\n");
			sb.append(tab + tag.end() + "\n");
		}
		return sb.toString();
	}

	public List<Element> getSubElements() {
		return elements;
	}

	public Tag getTag() {
		return tag;
	}

	/*
	 * HTML SECTION, only HTML tags are listed below in alphabetical order
	 */

	public Element a(String s, String... attributes) {
		return addAttributes(tag("a", s), attributes);
	}

	public Element b(String s) {
		return tag("b", s);
	}

	public Element body() {
		return tag("body");
	}

	public Element body(String... attributes) {
		return addAttributes(tag("body"), attributes);
	}

	public Element br() {
		return tag("br", null, true);
	}

	public Element button() {
		return tag("button");
	}

	public Element button(String buttontext, String name, String value, String type, String... attributes) {
		Element e = addAttributes(tag("button", buttontext), attributes);
		e.attribute("name=" + name);
		e.attribute("value=" + value);
		e.attribute("type=" + type);
		return e;
	}

	public Element div(String... attributes) {
		return addAttributes(tag("div"), attributes);
	}

	public Element fieldset() {
		return tag("fieldset");
	}

	public Element font(String value, String... attributes) {
		return addAttributes(tag("font", value), attributes);
	}

	public Element form(String action, String method, String name) {
		return addAttributes(tag("form"), "action=" + action, "method=" + method, "name=" + name);
	}

	public Element form(String action, String method, String name, String... attributes) {
		Element e = addAttributes(tag("form"), "action=" + action, "method=" + method, "name=" + name);
		e.addAttributes(e, attributes);
		return e;
	}

	public Element h(int i, String s) {
		if (i < 0 && i > 7)
			i = 1;
		return tag("h" + i, s);
	}

	public Element head() {
		return tag("head");
	}

	public Element html() {
		return tag("html");
	}

	public Element i(String s) {
		return tag("i", s);
	}

	public Element img() {
		return tag("img");
	}

	public Element img(String... attributes) {
		return addAttributes(tag("img"), attributes);
	}

	public Element input(String name, String type) {
		return addAttributes(tag("input"), "name=" + name, "type=" + type);
	}

	public Element input(String name, String type, String value) {
		Element input = input(name, type);
		if (value != null && value.trim().length() > 0)
			input.attribute("value=" + value);
		return input;
	}

	public Element input(String name, String type, String value, Integer size, Integer maxLength, Boolean checked) {
		Element input = input(name, type);
		if (value != null && value.trim().length() > 0)
			input.attribute("value=" + value);
		if (size != null)
			input.attribute("size=" + size);
		if (maxLength != null)
			input.attribute("maxLength=" + maxLength);
		if (checked != null && checked == true)
			input.attribute("checked");
		return input;
	}

	/**
	 * Not a standard HTML element, but used in CSS
	 */
	public Element legend(String value) {
		return tag("legend", value);
	}

	public Element li(String value, String... attributes) {
		return addAttributes(tag("li", value), attributes);
	}

	public Element link(String... attributes) {
		return addAttributes(tag("link"), attributes);
	}

	public Element option(String value, Boolean selected) {
		Element e = tag("option", value);
		if (selected != null && selected == true)
			e.attribute("selected=selected");
		return e;
	}

	public Element p() {
		return tag("p");
	}

	public Element script(String... attributes) {
		return addAttributes(tag("script"), attributes);
	}

	public Element select(String name, Integer size, Boolean multiple) {
		Element e = tag("select");
		e.attribute("name=" + name);
		if (size != null)
			e.attribute("size=" + size);
		if (multiple != null && multiple == true)
			e.attribute("multiple");
		return e;
	}

	public Element table() {
		return tag("table");
	}

	public Element table(String... attributes) {
		return addAttributes(tag("table"), attributes);
	}

	public Element td() {
		return tag("td");
	}

	public Element td(String s) {
		return tag("td", s);
	}

	public Element td(String s, String... attributes) {
		return addAttributes(tag("td", s), attributes);
	}

	/**
	 * A special method to add many cells in a table with one statement.
	 * Does not return an element, nor is it possible to set attributes.
	 * To also set attributes, use tds(Attribute[], String...)
	 * @param cellcontents
	 */
	public void tds(String... cellcontents) {
		for (String cellcontent : cellcontents)
			tag("td", cellcontent);
	}

	/**
	 * A special method to add many cells in a table with one statement.
	 * Does not return an element. The attributes are applied to
	 * all cells.
	 * @param cellcontents
	 */
	public void tds(Attribute[] attributes, String... cellcontents) {
		for (String cellcontent : cellcontents) {
			Element e = tag("td", cellcontent);
			for (int i = 0; attributes != null && i < attributes.length; i++) {
				e.attribute(attributes[i].toString());
			}
		}
	}

	public TextAreaElement textarea(String name, int cols, int rows, boolean wrap) {
		TextAreaElement tae = new TextAreaElement(name, cols, rows, wrap);
		add(tae);
		return tae;
		//		return addAttributes(tag("textarea"), "name="+name, "cols="+cols, "rows="+rows);
	}

	public Element th() {
		return tag("th");
	}

	public Element th(String s) {
		return tag("th", s);
	}

	public Element th(String s, String... attributes) {
		return addAttributes(tag("th", s), attributes);
	}

	public void ths(String... cellcontents) {
		for (String cellcontent : cellcontents)
			tag("th", cellcontent);
	}

	/**
	 * A special method to add many cells in a table with one statement.
	 * Does not return an element. The attributes are applied to
	 * all cells.
	 * @param cellcontents
	 */
	public void ths(Attribute[] attributes, String... cellcontents) {
		for (String cellcontent : cellcontents) {
			Element e = tag("th", cellcontent);
			for (int i = 0; attributes != null && i < attributes.length; i++) {
				e.attribute(attributes[i].toString());
			}
		}
	}

	public Element title(String s) {
		return tag("title", s);
	}

	public Element tr() {
		return tag("tr");
	}

	public Element tr(String... attributes) {
		return addAttributes(tag("tr"), attributes);
	}

	public Element u(String s) {
		return tag("u", s);
	}

	public Element ul(String... attributes) {
		return addAttributes(tag("ul"), attributes);
	}

}
