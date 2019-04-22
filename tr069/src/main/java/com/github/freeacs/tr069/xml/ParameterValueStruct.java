package com.github.freeacs.tr069.xml;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParameterValueStruct {
  private String name;
  private String value;
  private String type;

  public ParameterValueStruct(String name, String value) {
    this.name = name;
    this.value = value;
  }
}
