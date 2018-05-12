package com.github.freeacs.shell.sync;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.util.ArrayList;
import java.util.List;

public class UnittypeXML {

	public static String getAttr(Node node, String attrName) throws Exception {
		Attribute attr = (Attribute) node.selectSingleNode("@" + attrName);
		if (attr != null)
			return attr.getValue();
		else
			return "";
	}

	public static String getNodeText(Node node, String nodeName) throws Exception {
		Node subnode = node.selectSingleNode(nodeName);
		if (subnode != null)
			return subnode.getText().trim();
		else
			return "";
	}

	public class Info {
		String version;
		String vendor;
		String description;
		String helptext;

		public void load(Node node) throws Exception {
			version = getNodeText(node, "version");
			vendor = getNodeText(node, "vendor");
			description = getNodeText(node, "description");
			helptext = getNodeText(node, "helptext");
		}
	}

	public class EnumEntry {
		String displayname;
		String value;

		EnumEntry() {
		}

		public void load(Node node) throws Exception {
			displayname = getAttr(node, "displayname");
			value = getAttr(node, "value");
		}

	}

	public class EnumEntries {
		List<EnumEntry> list = new ArrayList<EnumEntry>();

		EnumEntries() {
		}

		@SuppressWarnings("rawtypes")
		public void load(Node node) throws Exception {
			List entry_nodes = node.selectNodes("entry");
			for (int i = 0; i < entry_nodes.size(); i++) {
				EnumEntry enumEntry = new EnumEntry();
				enumEntry.load((Node) entry_nodes.get(i));
				list.add(enumEntry);
			}
		}

	}

	public class Enum {
		String name;
		String default_value;
		EnumEntries enumEntries = new EnumEntries();

		public String get_default_value(String parameter_default_value) {
			if (parameter_default_value.length() != 0)
				return parameter_default_value;
			else
				return default_value;
		}

		Enum() {
		}

		public void load(Node node) throws Exception {
			name = getAttr(node, "name");
			default_value = getAttr(node, "default_value");
			enumEntries.load(node);
		}

		public List<String> getValues(String parameter_default_value) {
			String use_default_value = get_default_value(parameter_default_value);
			List<String> values = new ArrayList<String>();
			if (use_default_value.length() != 0)
				values.add(use_default_value);
			for (int i = 0; i < enumEntries.list.size(); i++) {
				EnumEntry enumEntry = enumEntries.list.get(i);
				if (!enumEntry.value.equals(use_default_value)) {
					values.add(enumEntry.value);
				}
			}
			return values;
		}

		public String xapsshell_setvalues(String parameter_default_value) {
			String use_default_value = get_default_value(parameter_default_value);
			String str = "";
			if (use_default_value.length() != 0)
				str = "\"" + use_default_value + "\"";
			for (int i = 0; i < enumEntries.list.size(); i++) {
				EnumEntry enumEntry = enumEntries.list.get(i);
				if (!enumEntry.value.equals(use_default_value)) {
					if (str.length() != 0)
						str += " ";
					str += "\"" + enumEntry.value + "\"";
				}
			}
			return str;
		}
	}

	public class Enums {
		List<Enum> list = new ArrayList<Enum>();

		Enums() {
		}

		@SuppressWarnings("rawtypes")
		public void load(Node node) throws Exception {
			List enum_nodes = node.selectNodes("enum");
			for (int i = 0; i < enum_nodes.size(); i++) {
				Enum enum_ = new Enum();
				enum_.load((Node) enum_nodes.get(i));
				list.add(enum_);
			}
		}

		public Enum find(String name) {
			for (int i = 0; i < list.size(); i++) {
				Enum enum_ = list.get(i);
				if (enum_.name.equals(name))
					return enum_;
			}
			return null;
		}
	}

	public static int myCompare(String a, String b) {
		try {
			Integer av = Integer.parseInt(a);
			Integer bv = Integer.parseInt(b);
			return av.compareTo(bv);
		} catch (NumberFormatException nfe) {
			return a.compareTo(b);
		}
	}

	public static int myCompare(String[] a, String[] b) {
		int i = 0;
		while (true) {
			if ((a.length <= i) && (b.length <= i))
				return 0;
			if (a.length <= i)
				return -1;
			if (b.length <= i)
				return +1;
			int c = myCompare(a[i], b[i]);
			if (c != 0)
				return c;
			i++;
		}
	}

	public static int dottedNameCompare(String a, String b) {
		String a1[] = a.split("\\.");
		String b1[] = b.split("\\.");
		return myCompare(a1, b1);
	}

	@SuppressWarnings("rawtypes")
	public class Parameter implements Comparable {
		String name;
		String protocol;
		String deviceflags;
		String addflags;
		String type;
		Enum enum_;
		String maxlength;
		String default_value;
		String helptext;

		Parameter() {
		}

		public int compareTo(Object other) {
			String otherName = ((Parameter) other).name;
			return dottedNameCompare(name, otherName);
		}

		public boolean equals(Object other) {
			return (compareTo(other) == 0);
		}

		public boolean protocol_match(String use_protocol) {
			if (protocol.contains("ALL"))
				return true;
			if (protocol.contains(use_protocol))
				return true;
			return false;
		}

		public void load(Node node, Enums enums) throws Exception {
			name = getAttr(node, "name");
			Node attributes = node.selectSingleNode("attributes");
			protocol = getAttr(attributes, "protocol");
			deviceflags = getAttr(attributes, "deviceflags");
			addflags = getAttr(attributes, "addflags");
			type = getAttr(attributes, "type");
			maxlength = getAttr(attributes, "maxlength");
			default_value = getAttr(attributes, "default_value");
			helptext = getNodeText(node, "helptext");
			if (type.contains("enum")) {
				String[] parts = type.split("\\(|\\)");
				String enum_name = parts[1];
				enum_ = enums.find(enum_name);
			}
		}

		public boolean isSystemParam() {
			if (name.contains(("System.")))
				return true;
			else
				return false;
		}
	};

	public class Parameters {
		List<Parameter> list = new ArrayList<Parameter>();

		Parameters() {
		}

		@SuppressWarnings("rawtypes")
		public void load(Node node, Enums enums) throws Exception {
			List parameter_nodes = node.selectNodes("parameter");
			for (int i = 0; i < parameter_nodes.size(); i++) {
				Parameter parameter = new Parameter();
				parameter.load((Node) parameter_nodes.get(i), enums);
				list.add(parameter);
			}
		}

		public Parameter find(String name) {
			for (int i = 0; i < list.size(); i++) {
				Parameter parameter = list.get(i);
				if (parameter.name.equals(name))
					return parameter;
			}
			return null;
		}
	}

	Info info = new Info();
	Enums enums = new Enums();
	Parameters parameters = new Parameters();

	UnittypeXML() {
	}

	@SuppressWarnings("rawtypes")
	public void load(String file_input) throws Exception {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setXIncludeAware(true);
		factory.setNamespaceAware(false);
		factory.setValidating(false);

		SAXParser parser = factory.newSAXParser();

		SAXReader reader = new SAXReader(parser.getXMLReader());
		Document document = reader.read(file_input);

		info = new Info();
		info.load((Node) document.selectSingleNode("//unittype/info"));

		enums = new Enums();
		enums.load((Node) document.selectSingleNode("//unittype/parameters/enums"));

		parameters = new Parameters();
		List parameters_nodes = document.selectNodes("//unittype/parameters");
		for (int i = 0; i < parameters_nodes.size(); i++) {
			Node parameter_node = (Node) parameters_nodes.get(i);
			parameters.load(parameter_node, enums);
		}
	}

	public static boolean protocolValid(String protocol) throws Exception {
		if (protocol.equals("TFTP"))
			return true;
		if (protocol.equals("OPP"))
			return true;
		if (protocol.equals("TR-069"))
			return true;
		if (protocol.equals("ALL"))
			return true;
		return false;
	}
}
