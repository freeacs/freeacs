package com.github.freeacs.dbi;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UnittypeParameterFlag {
  private String flag;

  public UnittypeParameterFlag(String flag) {
    setFlag(flag);
  }

  protected UnittypeParameterFlag(String flag, boolean noValidation) {
    this.flag = flag;
  }

  public String getFlag() {
    return flag;
  }

  public void setFlag(String fl) {
    String flag = fl;
    if (flag == null || flag.trim().isEmpty()) {
      throw new IllegalArgumentException(
          "The unittype parameter flag format cannot be blank/null.");
    }
    flag = flag.toUpperCase();
    Pattern p = Pattern.compile("[ABCDIRWSX]{1,7}");
    Matcher m = p.matcher(flag);
    if (!m.matches()) {
      throw new IllegalArgumentException(
          "Wrong format. The unittype parameter flag can be one ore more out of these characters:  ABCDRWSX (flag:"
              + flag
              + ")");
    }
    String modifiedFlag = ""; // strip away duplicate flags
    for (int i = 0; i < flag.length(); i++) {
      if ("I".equals(flag.substring(i, i + 1))) {
        continue;
      } // silently drops all Inspection flags - to be backward compatible
      if (!modifiedFlag.contains(flag.substring(i, i + 1))) {
        modifiedFlag += flag.substring(i, i + 1);
      }
    }
    flag = modifiedFlag;
    if (flag.indexOf('R') > -1 || flag.indexOf('X') > -1) {
      this.flag = flag;
    } else {
      throw new IllegalArgumentException(
          "The flag must contain either R, RW or X (flag:" + flag + ")");
    }
    if (isSystem() && (flag.indexOf('W') > -1 || isReadOnly())) {
      throw new IllegalArgumentException(
          "The flag must contain either R, RW or X (flag:" + flag + ")");
    }
    if (isAlwaysRead() && !isReadOnly()) {
      throw new IllegalArgumentException(
          "The A flag cannot be set together with the W or X flag (flag:" + flag + ")");
    }
    if (isDisplayable() && isConfidential()) {
      throw new IllegalArgumentException(
          "The D flag cannot be set together with the C flag (flag:" + flag + ")");
    }
    if (isBootRequired() && !isReadWrite()) {
      throw new IllegalArgumentException(
          "The B flag can only be specified together with RW flag (flag:" + flag + ")");
    }
  }

  public boolean isConfidential() {
    return flag.indexOf('C') > -1;
  }

  public boolean isAlwaysRead() {
    return flag.indexOf('A') > -1;
  }

  public boolean isReadOnly() {
    return flag.indexOf('R') > -1 && flag.indexOf('W') == -1;
  }

  public boolean isReadWrite() {
    return flag.indexOf('R') > -1 && flag.indexOf('W') > -1;
  }

  public boolean isSystem() {
    return flag.indexOf('X') > -1;
  }

  public boolean isBootRequired() {
    return flag.indexOf('B') > -1;
  }

  public boolean isSearchable() {
    return flag.indexOf('S') > -1;
  }

  public boolean isDisplayable() {
    return flag.indexOf('D') > -1;
  }

  @Override
  public String toString() {
    return flag;
  }
}
