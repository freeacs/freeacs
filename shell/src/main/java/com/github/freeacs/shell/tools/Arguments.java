package com.github.freeacs.shell.tools;

import java.util.HashMap;
import java.util.Map;

public class Arguments {
  public static final String FLAG_EXISTS = "FLAG_EXISTS";

  private Map<String, String> argMap = new HashMap<>();

  public Arguments(String[] args) {
    String flag = null;
    for (String arg : args) {
      if (arg.startsWith("-")) {
        if (flag != null) {
          argMap.put(flag, FLAG_EXISTS);
        }
        flag = arg.substring(1);
      } else if (flag == null) {
        throw new IllegalArgumentException("No - prefixing flag option");
      } else {
        argMap.put(flag, arg);
        flag = null;
      }
    }
    if (flag != null) {
      argMap.put(flag, FLAG_EXISTS);
    }
  }

  public String getArgument(String flag) {
    String value = argMap.get(flag);
    if (value == null && argMap.containsKey(flag)) {
      return FLAG_EXISTS;
    } else {
      return value;
    }
  }
}
