package com.github.freeacs.shell.output;

import java.util.ArrayList;
import java.util.List;

public class Line {
  private List<String> values = new ArrayList<>();

  public Line() {}

  public Line(String... values) {
    for (String value : values) {
      addValue(value);
    }
  }

  private String processValue(String value) {
    if (value != null) {
      value = value.replaceAll("\"", "");
      if ("".equals(value)
          || value.indexOf(' ') > -1
          || value.indexOf('\t') > -1
          || value.indexOf('|') > -1) {
        value = "\"" + value + "\"";
      }
      value = value.replaceAll("\r", "");
      value = value.replaceAll("\n", " ");
    } else {
      value = "NULL";
    }
    return value;
  }

  public void insertValue(int index, String value) {
    values.add(index, processValue(value));
  }

  public void addValue(String value) {
    values.add(processValue(value));
  }

  public void addValue(Integer value) {
    if (value != null) {
      addValue(String.valueOf(value));
    } else {
      addValue((String) null);
    }
  }

  public void addValueRaw(String value) {
    values.add(value);
  }

  public List<String> getValues() {
    return values;
  }
}
