package com.github.freeacs.tr069.xml;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MethodList {
  private List<String> methods = new ArrayList<>();

  public void addMethod(String method) {
    this.methods.add(method);
  }

  public boolean contains(String method) {
    return methods.contains(method);
  }
}
