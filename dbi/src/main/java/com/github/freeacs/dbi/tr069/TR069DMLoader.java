package com.github.freeacs.dbi.tr069;

import com.github.freeacs.dbi.tr069.TR069DMParameter.StringType;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.*;
import java.util.Map.Entry;

public class TR069DMLoader {

	public static void main(String[] args) {
		try {
			TR069DMParameterMap map = load();
			printParameterHelpForUseInWeb(map);

		} catch (Throwable t) {
			System.err.println("Unexpected error: " + t);
		}
	}

	public static void printParameterHelpForUseInWeb(TR069DMParameterMap map) {

		Set<String> printedEntries = new HashSet<String>();
		for (Entry<String, TR069DMParameter> entry : map.getMap().entrySet()) {
			String parameterName = entry.getKey();
			if (entry.getKey().startsWith("Device"))
				parameterName = "InternetGateway" + entry.getKey();
			else if (entry.getKey().startsWith("VoiceService"))
				parameterName = "InternetGatewayDevice.Services." + entry.getKey();
			parameterName = parameterName.replace("{i}", "1");
			if (printedEntries.contains(parameterName))
				continue;
			printedEntries.add(parameterName);

			System.out.println("<Parameter>");
			System.out.println("\t<Name>" + parameterName + "</Name>");
			System.out.println("\t<Heading>" + entry.getKey().substring(entry.getKey().lastIndexOf(".") + 1) + "</Heading>");
			System.out.print("\t<Help>");
			//			System.out.print(entry.getValue().getDescription().replace("{i}", "1").replace("’", "'").replace("<", "&lt;").replace(">", "&gt;").replace("”","\"").replace("“", "\""));
			System.out.println("\t</Help>");
			System.out.println("</Parameter>");
		}
	}

	private static void updateParameters(Map<String, TR069DMParameter> map, String objectName, NodeList nList) throws Exception {

		//		System.out.println("Found " + nList.getLength() + " parameter nodes");
		for (int i = 0; i < nList.getLength(); i++) {
			Node n = nList.item(i);
			TR069DMParameter parameter = new TR069DMParameter();

			// Find attributes
			NamedNodeMap nnm = n.getAttributes();
			for (int j = 0; j < nnm.getLength(); j++) {
				Node attribute = nnm.item(j);
				if (attribute.getNodeName().equals("name")) {
					//					if (map.get(objectName + attribute.getNodeValue()) == null) {
					parameter.setName(objectName + attribute.getNodeValue());
					map.put(parameter.getName(), parameter);
					//					} 
					//					else {
					//						System.out.println("Already made: " + objectName + attribute.getNodeValue());
					//						parameter = map.get(objectName + attribute.getNodeValue());
					//					}
				}
				if (attribute.getNodeName().equals("access"))
					parameter.setReadOnly(attribute.getNodeValue().equals("readOnly"));
				if (attribute.getNodeName().equals("dmr:version"))
					parameter.setDataModelVersion(attribute.getNodeValue());
				if (attribute.getNodeName().equals("activeNotify"))
					parameter.setNotification(attribute.getNodeValue());
				if (attribute.getNodeName().equals("status"))
					parameter.setDataModelStatus(attribute.getNodeValue());
				if (attribute.getNodeName().equals("forcedInform"))
					parameter.setForcedInform(attribute.getNodeValue().equals("true"));
			}

			// Find children
			NodeList children = n.getChildNodes();
			for (int j = 0; j < children.getLength(); j++) {
				if (!children.item(j).hasChildNodes())
					continue;
				Element syntaxElement = (Element) children.item(j);
				if (syntaxElement.getNodeName().equals("description"))
					parameter.setDescription(syntaxElement.getTextContent());
				if (syntaxElement.getNodeName().equals("syntax")) {
					String command = syntaxElement.getAttribute("command");
					if (command != null && command.equals("true"))
						parameter.setCommand(true);
					NodeList syntaxChildren = syntaxElement.getChildNodes();
					for (int k = 0; k < syntaxChildren.getLength(); k++) {
						Node syntaxChildNode = syntaxChildren.item(k);
						String name = syntaxChildNode.getNodeName();
						if (name.equals("#text"))
							continue;
						if (name.equals("default"))
							continue;
						if (name.equals("dataType")) {
							String dataType = ((Element) syntaxChildNode).getAttribute("ref");
							parameter.setDatatype(TR069DMType.valueOf(dataType.toUpperCase()));
							continue;
						}
						if (name.equals("list")) {
							parameter.setList(true);
						} else {
							parameter.setDatatype(TR069DMType.valueOf(name.toUpperCase()));
							NodeList datatypeChildren = syntaxChildNode.getChildNodes();
							for (int l = 0; l < datatypeChildren.getLength(); l++) {
								// May find size, range, enumeration, pattern
								Node datatypeChild = datatypeChildren.item(l);
								if (datatypeChild.getNodeName().equals("range")) {
									Element rangeElement = (Element) datatypeChild;
									if (rangeElement.getAttribute("minInclusive") != null && !rangeElement.getAttribute("minInclusive").equals(""))
										parameter.getRange().setMin(new Long(rangeElement.getAttribute("minInclusive")));
									if (rangeElement.getAttribute("maxInclusive") != null && !rangeElement.getAttribute("maxInclusive").equals(""))
										parameter.getRange().setMax(new Long(rangeElement.getAttribute("maxInclusive")));
									//	System.out.println("Found range for " + parameter.getName() + ": [" + rangeElement.getAttribute("minInclusive") + "," + rangeElement.getAttribute("maxInclusive") + "]");										
								}
								if (datatypeChild.getNodeName().equals("size")) {
									Element sizeElement = (Element) datatypeChild;
									if (sizeElement.getAttribute("minLength") != null && !sizeElement.getAttribute("minLength").equals(""))
										parameter.getRange().setMin(new Long(sizeElement.getAttribute("minLength")));
									if (sizeElement.getAttribute("maxLength") != null && !sizeElement.getAttribute("maxLength").equals(""))
										parameter.getRange().setMax(new Long(sizeElement.getAttribute("maxLength")));
									//	System.out.println("Found size for " + parameter.getName() + ": [" + sizeElement.getAttribute("minLength") + "," + sizeElement.getAttribute("maxLength") + "]");
								}
								if (datatypeChild.getNodeName().equals("pattern")) {
									Element patternElement = (Element) datatypeChild;
									List<StringType> enums = parameter.getEnumeration();
									StringType st = null;
									if (enums.size() > 0)
										st = enums.get(enums.size() - 1);
									if (st != null) {
										if (st.getPattern() == null)
											st.setPattern(patternElement.getAttribute("value"));
										else {
											st = new StringType(null, patternElement.getAttribute("value"));
											enums.add(st);
										}
									} else {
										st = new StringType(null, patternElement.getAttribute("value"));
										enums.add(st);
									}
									//	System.out.println("Found pattern for " + parameter.getName() + " enum-size: " + parameter.getEnumeration().size());
								}
								if (datatypeChild.getNodeName().equals("enumeration")) {
									Element enumeration = (Element) datatypeChild;
									List<StringType> enums = parameter.getEnumeration();
									StringType st = null;
									if (enums.size() > 0)
										st = enums.get(enums.size() - 1);
									if (st != null) {
										if (st.getValue() == null)
											st.setValue(enumeration.getAttribute("value"));
										else {
											st = new StringType(enumeration.getAttribute("value"), null);
											enums.add(st);
										}
									} else {
										st = new StringType(enumeration.getAttribute("value"), null);
										enums.add(st);
									}
									//	System.out.println("Found enumeration for " + parameter.getName() + " enum-size: " + parameter.getEnumeration().size());
									//	System.out.println("Found enumeration for " + parameter.getName() + ": " + enumeration.getAttribute("value"));
								}
							}
						}
						//	if (!name.equals("string") && !name.equals("list") && !name.equals("int") && !name.equals("unsignedInt") && !name.equals("base64") && !name.equals("hexBinary"))
						//		System.out.println("Parameter " + parameter.getName() + " has syntaxChild " + syntaxChild.getNodeName());
					}
				}
			}
		}
	}

	private static void updateDatamodelFromInputStream(Map<String, TR069DMParameter> map, InputStream inputStream) throws Exception {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(inputStream);
		doc.getDocumentElement().normalize();

		NodeList objects = doc.getElementsByTagName("object");
		for (int i = 0; i < objects.getLength(); i++) {
			Element object = (Element) objects.item(i);
			String objectName = object.getAttribute("name");
			NodeList parameterNodes = object.getElementsByTagName("parameter");
			updateParameters(map, objectName, parameterNodes);
		}

	}

	private static InputStream getInputStream(String searchName) {
		InputStream stream = null;
		ClassLoader cl = TR069DMLoader.class.getClassLoader();
		stream = cl.getResourceAsStream(searchName);
		while (stream == null) {
			cl = cl.getParent();
			if (cl == null)
				break;
			stream = cl.getResourceAsStream(searchName);
		}
		return stream;
	}

	public static TR069DMParameterMap load() throws Exception {
		Map<String, TR069DMParameter> map = new TreeMap<String, TR069DMParameter>();
		String[] filenames = new String[] { "tr-098-1-4-0-full.xml", "tr-181-1-2-0-full.xml", "tr-104-1-1-0-full.xml" };
		for (String filename : filenames) {
			InputStream is = getInputStream(filename);
			if (is == null)
				is = getInputStream("src/" + filename);
			if (is != null)
				updateDatamodelFromInputStream(map, is);
		}

		return new TR069DMParameterMap(map);
	}
}