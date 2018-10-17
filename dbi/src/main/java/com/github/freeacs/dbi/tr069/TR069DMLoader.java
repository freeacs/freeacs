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
  private static void updateParameters(
      Map<String, TR069DMParameter> map, String objectName, NodeList nList) throws Exception {
    for (int i = 0; i < nList.getLength(); i++) {
      Node n = nList.item(i);
      TR069DMParameter parameter = new TR069DMParameter();

      // Find attributes
      NamedNodeMap nnm = n.getAttributes();
      for (int j = 0; j < nnm.getLength(); j++) {
        Node attribute = nnm.item(j);
        if ("name".equals(attribute.getNodeName())) {
          parameter.setName(objectName + attribute.getNodeValue());
          map.put(parameter.getName(), parameter);
        }
        if ("access".equals(attribute.getNodeName())) {
          parameter.setReadOnly("readOnly".equals(attribute.getNodeValue()));
        }
        if ("dmr:version".equals(attribute.getNodeName())) {
          parameter.setDataModelVersion(attribute.getNodeValue());
        }
        if ("activeNotify".equals(attribute.getNodeName())) {
          parameter.setNotification(attribute.getNodeValue());
        }
        if ("status".equals(attribute.getNodeName())) {
          parameter.setDataModelStatus(attribute.getNodeValue());
        }
        if ("forcedInform".equals(attribute.getNodeName())) {
          parameter.setForcedInform("true".equals(attribute.getNodeValue()));
        }
      }

      // Find children
      NodeList children = n.getChildNodes();
      for (int j = 0; j < children.getLength(); j++) {
        if (!children.item(j).hasChildNodes()) {
          continue;
        }
        Element syntaxElement = (Element) children.item(j);
        if ("description".equals(syntaxElement.getNodeName())) {
          parameter.setDescription(syntaxElement.getTextContent());
        }
        if ("syntax".equals(syntaxElement.getNodeName())) {
          String command = syntaxElement.getAttribute("command");
          if ("true".equals(command)) {
            parameter.setCommand(true);
          }
          NodeList syntaxChildren = syntaxElement.getChildNodes();
          for (int k = 0; k < syntaxChildren.getLength(); k++) {
            Node syntaxChildNode = syntaxChildren.item(k);
            String name = syntaxChildNode.getNodeName();
            if ("#text".equals(name) || "default".equals(name)) {
              continue;
            }
            if ("dataType".equals(name)) {
              String dataType = ((Element) syntaxChildNode).getAttribute("ref");
              parameter.setDatatype(TR069DMType.valueOf(dataType.toUpperCase()));
              continue;
            }
            if ("list".equals(name)) {
              parameter.setList(true);
            } else {
              parameter.setDatatype(TR069DMType.valueOf(name.toUpperCase()));
              NodeList datatypeChildren = syntaxChildNode.getChildNodes();
              for (int l = 0; l < datatypeChildren.getLength(); l++) {
                // May find size, range, enumeration, pattern
                Node datatypeChild = datatypeChildren.item(l);
                if ("range".equals(datatypeChild.getNodeName())) {
                  Element rangeElement = (Element) datatypeChild;
                  if (rangeElement.getAttribute("minInclusive") != null
                      && !"".equals(rangeElement.getAttribute("minInclusive"))) {
                    parameter
                        .getRange()
                        .setMin(Long.valueOf(rangeElement.getAttribute("minInclusive")));
                  }
                  if (rangeElement.getAttribute("maxInclusive") != null
                      && !"".equals(rangeElement.getAttribute("maxInclusive"))) {
                    parameter
                        .getRange()
                        .setMax(Long.valueOf(rangeElement.getAttribute("maxInclusive")));
                  }
                }
                if ("size".equals(datatypeChild.getNodeName())) {
                  Element sizeElement = (Element) datatypeChild;
                  if (sizeElement.getAttribute("minLength") != null
                      && !"".equals(sizeElement.getAttribute("minLength"))) {
                    parameter
                        .getRange()
                        .setMin(Long.valueOf(sizeElement.getAttribute("minLength")));
                  }
                  if (sizeElement.getAttribute("maxLength") != null
                      && !"".equals(sizeElement.getAttribute("maxLength"))) {
                    parameter
                        .getRange()
                        .setMax(Long.valueOf(sizeElement.getAttribute("maxLength")));
                  }
                }
                if ("pattern".equals(datatypeChild.getNodeName())) {
                  Element patternElement = (Element) datatypeChild;
                  List<StringType> enums = parameter.getEnumeration();
                  StringType st = null;
                  if (!enums.isEmpty()) {
                    st = enums.get(enums.size() - 1);
                  }
                  if (st != null) {
                    if (st.getPattern() == null) {
                      st.setPattern(patternElement.getAttribute("value"));
                    } else {
                      st = new StringType(null, patternElement.getAttribute("value"));
                      enums.add(st);
                    }
                  } else {
                    st = new StringType(null, patternElement.getAttribute("value"));
                    enums.add(st);
                  }
                }
                if ("enumeration".equals(datatypeChild.getNodeName())) {
                  Element enumeration = (Element) datatypeChild;
                  List<StringType> enums = parameter.getEnumeration();
                  StringType st = null;
                  if (!enums.isEmpty()) {
                    st = enums.get(enums.size() - 1);
                  }
                  if (st != null) {
                    if (st.getValue() == null) {
                      st.setValue(enumeration.getAttribute("value"));
                    } else {
                      st = new StringType(enumeration.getAttribute("value"), null);
                      enums.add(st);
                    }
                  } else {
                    st = new StringType(enumeration.getAttribute("value"), null);
                    enums.add(st);
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  private static void updateDatamodelFromInputStream(
      Map<String, TR069DMParameter> map, InputStream inputStream) throws Exception {
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
    String[] filenames =
        new String[] {"tr-098-1-4-0-full.xml", "tr-181-1-2-0-full.xml", "tr-104-1-1-0-full.xml"};
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
