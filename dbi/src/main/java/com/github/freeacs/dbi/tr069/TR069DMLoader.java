package com.github.freeacs.dbi.tr069;

import com.github.freeacs.dbi.tr069.TR069DMParameter.StringType;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class TR069DMLoader {
  private static void updateParameters(Map<String, TR069DMParameter> map, String objectName, NodeList nList) {
    for (int i = 0; i < nList.getLength(); i++) {
      Node n = nList.item(i);
      TR069DMParameter parameter = new TR069DMParameter();
      processNodeAttributes(n, parameter, objectName, map);
      processNodeChildren(n, parameter);
    }
  }

  private static void processNodeAttributes(Node n, TR069DMParameter parameter, String objectName, Map<String, TR069DMParameter> map) {
    NamedNodeMap nnm = n.getAttributes();
    for (int j = 0; j < nnm.getLength(); j++) {
      Node attribute = nnm.item(j);
      switch (attribute.getNodeName()) {
        case "name":
          parameter.setName(objectName + attribute.getNodeValue());
          map.put(parameter.getName(), parameter);
          break;
        case "access":
          parameter.setReadOnly("readOnly".equals(attribute.getNodeValue()));
          break;
        case "dmr:version":
          parameter.setDataModelVersion(attribute.getNodeValue());
          break;
        case "activeNotify":
          parameter.setNotification(attribute.getNodeValue());
          break;
        case "status":
          parameter.setDataModelStatus(attribute.getNodeValue());
          break;
        case "forcedInform":
          parameter.setForcedInform("true".equals(attribute.getNodeValue()));
          break;
      }
    }
  }

  private static void processNodeChildren(Node n, TR069DMParameter parameter) {
    NodeList children = n.getChildNodes();
    for (int j = 0; j < children.getLength(); j++) {
      if (!children.item(j).hasChildNodes()) {
        continue;
      }
      Element childElement = (Element) children.item(j);
      switch (childElement.getNodeName()) {
        case "description":
          parameter.setDescription(childElement.getTextContent());
          break;
        case "syntax":
          processSyntax(childElement, parameter);
          break;
      }
    }
  }

  private static void processSyntax(Element syntaxElement, TR069DMParameter parameter) {
    String command = syntaxElement.getAttribute("command");
    if ("true".equals(command)) {
      parameter.setCommand(true);
    }
    NodeList syntaxChildren = syntaxElement.getChildNodes();
    for (int k = 0; k < syntaxChildren.getLength(); k++) {
      Node syntaxChildNode = syntaxChildren.item(k);
      if (!"#text".equals(syntaxChildNode.getNodeName()) && !"default".equals(syntaxChildNode.getNodeName())) {
        processSyntaxChild(syntaxChildNode, parameter);
      }
    }
  }

  private static void processSyntaxChild(Node syntaxChildNode, TR069DMParameter parameter) {
    switch (syntaxChildNode.getNodeName()) {
      case "dataType":
        String dataType = ((Element) syntaxChildNode).getAttribute("ref");
        parameter.setDatatype(TR069DMType.valueOf(dataType.toUpperCase()));
        break;
      case "list":
        parameter.setList(true);
        break;
      default:
        parameter.setDatatype(TR069DMType.valueOf(syntaxChildNode.getNodeName().toUpperCase()));
        processDatatypeChildren(syntaxChildNode, parameter);
        break;
    }
  }

  private static void processDatatypeChildren(Node syntaxChildNode, TR069DMParameter parameter) {
    NodeList datatypeChildren = syntaxChildNode.getChildNodes();
    for (int l = 0; l < datatypeChildren.getLength(); l++) {
      Node datatypeChild = datatypeChildren.item(l);
      switch (datatypeChild.getNodeName()) {
        case "range":
        case "size":
          processRangeOrSize((Element) datatypeChild, parameter);
          break;
        case "pattern":
          processPattern((Element) datatypeChild, parameter);
          break;
        case "enumeration":
          processEnumeration((Element) datatypeChild, parameter);
          break;
      }
    }
  }

  private static void processRangeOrSize(Element element, TR069DMParameter parameter) {
    String min = element.getAttribute("minInclusive");
    if (min.isEmpty()) min = element.getAttribute("minLength");
    if (!min.isEmpty()) parameter.getRange().setMin(Long.valueOf(min));

    String max = element.getAttribute("maxInclusive");
    if (max.isEmpty()) max = element.getAttribute("maxLength");
    if (!max.isEmpty()) parameter.getRange().setMax(Long.valueOf(max));
  }

  private static void processPattern(Element patternElement, TR069DMParameter parameter) {
    List<StringType> enums = parameter.getEnumeration();
    StringType st = new StringType(null, patternElement.getAttribute("value"));
    enums.add(st);
  }

  private static void processEnumeration(Element enumeration, TR069DMParameter parameter) {
    List<StringType> enums = parameter.getEnumeration();
    StringType st = new StringType(enumeration.getAttribute("value"), null);
    enums.add(st);
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
    InputStream stream;
    ClassLoader cl = TR069DMLoader.class.getClassLoader();
    stream = cl.getResourceAsStream(searchName);
    while (stream == null) {
      cl = cl.getParent();
      if (cl == null) {
        break;
      }
      stream = cl.getResourceAsStream(searchName);
    }
    return stream;
  }

  public static TR069DMParameterMap load() throws Exception {
    Map<String, TR069DMParameter> map = new TreeMap<>();
    String[] filenames = {"tr-098-1-4-0-full.xml", "tr-181-1-2-0-full.xml", "tr-104-1-1-0-full.xml"};
    for (String filename : filenames) {
      InputStream is = getInputStream(filename);
      if (is == null) {
        is = getInputStream("src/" + filename);
      }
      if (is != null) {
        updateDatamodelFromInputStream(map, is);
      }
    }

    return new TR069DMParameterMap(map);
  }
}
