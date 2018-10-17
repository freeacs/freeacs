package com.github.freeacs.shell.tools;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColumnDesc {
  private boolean toUpperCase;
  private boolean toLowerCase;
  private boolean unitid;
  private int fromIndex;
  private int toIndex;
  private Pattern columnPattern;
  private Pattern descPattern = Pattern.compile("(\\d+)(u|l|unitid)?-(\\d+)(.*)?");

  public ColumnDesc(String columnDesc) {
    Matcher matcher = descPattern.matcher(columnDesc);
    if (matcher.matches()) {
      fromIndex = Integer.parseInt(matcher.group(1));
      String tmp = matcher.group(2);
      if (tmp != null) {
        if ("u".equals(tmp)) {
          toUpperCase = true;
        }
        if ("l".equals(tmp)) {
          toLowerCase = true;
        }
        if ("unitid".equals(tmp)) {
          unitid = true;
        }
      }
      toIndex = Integer.parseInt(matcher.group(3));
      columnPattern = Pattern.compile(matcher.group(4));
    }
  }

  public boolean isToUpperCase() {
    return toUpperCase;
  }

  public boolean isToLowerCase() {
    return toLowerCase;
  }

  public boolean isUnitid() {
    return unitid;
  }

  public int getFromIndex() {
    return fromIndex;
  }

  public int getToIndex() {
    return toIndex;
  }

  public Pattern getColumnPattern() {
    return columnPattern;
  }
}
