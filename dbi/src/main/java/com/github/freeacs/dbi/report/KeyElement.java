package com.github.freeacs.dbi.report;

import lombok.Data;

@Data
public class KeyElement {
  private String name;
  private String value;

  public KeyElement(String name, String value) {
    this.name = name;
    this.value = value;
  }
}
