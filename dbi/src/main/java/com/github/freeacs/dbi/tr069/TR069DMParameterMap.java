package com.github.freeacs.dbi.tr069;

import java.util.Map;

public class TR069DMParameterMap {
  private static TR069DMParameterMap tr069ParameterMap;

  public static TR069DMParameterMap getTR069ParameterMap() throws Exception {
    if (tr069ParameterMap == null) {
      tr069ParameterMap = TR069DMLoader.load();
    }
    return tr069ParameterMap;
  }

  private Map<String, TR069DMParameter> map;

  public TR069DMParameterMap(Map<String, TR069DMParameter> map) {
    this.map = map;
  }

  private TR069DMParameter getParameterImpl(String name) {
    return map.get(name);
  }

  public TR069DMParameter getParameter(String name) {
    name = name.replaceAll("\\.\\d+\\.", ".{i}."); // data-model uses indexes {i}

    TR069DMParameter p = getParameterImpl(name);
    if (p == null) {
      p = getParameterImpl(name.replaceAll("^Device", "InternetGatewayDevice"));
    }
    if (p == null) {
      p = getParameterImpl(name.replaceAll("^InternetGatewayDevice", "Device"));
    }
    if (p == null) {
      p = getParameterImpl(name.replaceAll("^InternetGatewayDevice.Services.", ""));
    }
    return p;
  }

  public Map<String, TR069DMParameter> getMap() {
    return map;
  }
}
