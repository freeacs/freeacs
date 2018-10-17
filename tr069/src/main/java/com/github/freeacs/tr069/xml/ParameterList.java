package com.github.freeacs.tr069.xml;

import java.util.ArrayList;
import java.util.List;

public class ParameterList {
  private List<ParameterValueStruct> params;
  private List<ParameterInfoStruct> info;
  private List<ParameterAttributeStruct> attributes;

  public ParameterList() {
    this.params = new ArrayList<>();
    this.info = new ArrayList<>();
    this.attributes = new ArrayList<>();
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

  public List<ParameterValueStruct> getParameterValueList() {
    return this.params;
  }

  public List<ParameterInfoStruct> getParameterInfoList() {
    return this.info;
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
    if (!changed) {
      this.params.add(new ParameterValueStruct(key, value, type));
    }
  }
}
