package com.github.freeacs.shell.util;

import java.util.HashMap;
import java.util.Map;

public class ValidateEnum implements ValidateInput {
  private Map<String, Object> enumMap = new HashMap<>();

  public ValidateEnum(String... strings) {
    for (String str : strings) {
      enumMap.put(str, null);
    }
  }

  public ValidateEnum(Map<String, Object> enumMap) {
    this.enumMap = enumMap;
  }

  public boolean validate(String input) {
    return enumMap.containsKey(input);
  }
}
