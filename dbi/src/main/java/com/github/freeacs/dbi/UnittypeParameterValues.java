package com.github.freeacs.dbi;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
public class UnittypeParameterValues {
  public static String ENUM = "enum";
  public static String REGEXP = "regexp";

  private String type;
  private Pattern pattern;
  private List<String> values;

  public void setPattern(String pattern) {
    if (pattern != null) {
      this.type = REGEXP;
      this.pattern = Pattern.compile(pattern);
      values = null;
    } else {
      this.pattern = null;
    }
  }

  public List<String> getValues() {
    if (values == null) {
      values = new ArrayList<>();
    }
    return values;
  }

  public void setValues(List<String> values) {
    this.type = ENUM;
    this.values = values;
    this.pattern = null;
  }

  public boolean match(String str) {
    if (type.equals(REGEXP)) {
      Matcher mathcer = pattern.matcher(str);
      return mathcer.matches();
    } else if (type.equals(ENUM)) {
      return values.contains(str);
    }
    return false;
  }
}
