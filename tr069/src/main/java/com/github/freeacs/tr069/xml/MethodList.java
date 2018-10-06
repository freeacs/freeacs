package com.github.freeacs.tr069.xml;

import java.util.ArrayList;
import java.util.List;

public class MethodList {

  private List<String> methods;

  public MethodList() {
    this.methods = new ArrayList<>();
  }

  public List<String> getMethods() {
    return methods;
  }

  public String getMethod(int index) {
    return methods.get(index);
  }

  public void setMethods(List<String> methods) {
    this.methods = methods;
  }

  public void addMethod(String method) {
    this.methods.add(method);
  }

  public boolean contains(String method) {
    return methods.contains(method);
  }
}
