package com.github.freeacs.dbi.tr069;

import java.util.ArrayList;
import java.util.List;

public class TR069DMParameter {
  public class Range {
    private Long min;
    private Long max;

    public Range() {}

    public Range(Long min, Long max) {
      this.min = min;
      this.max = max;
    }

    public Long getMin() {
      if (min != null) {
        return min;
      }
      return datatype.getMin();
    }

    public void setMin(Long min) {
      this.min = min;
    }

    public Long getMax() {
      if (max != null) {
        return max;
      }
      return datatype.getMax();
    }

    public void setMax(Long max) {
      this.max = max;
    }

    public String toString() {
      return getMin() + "-" + getMax();
    }
  }

  public static class StringType {
    private String value;
    private String pattern;

    public StringType(String value, String pattern) {
      this.value = value;
      this.pattern = pattern;
    }

    public String getValue() {
      return value;
    }

    public void setValue(String value) {
      this.value = value;
    }

    public String getPattern() {
      return pattern;
    }

    public void setPattern(String pattern) {
      this.pattern = pattern;
    }
  }

  /** Covered. */
  private String name;
  /** Covered. */
  private String notification;
  /** Covered. */
  private boolean forcedInform;
  /** Covered. */
  private boolean readOnly;

  private boolean list;
  /** Default is string // covered. */
  private TR069DMType datatype = TR069DMType.STRING;

  /**
   * Types: boolean datetime string, base64, hexBinary (string types) Facets: size, enumeration,
   * pattern, int, long, unsignedInt, unsignedLong (numerical types) Facets: range covers both size
   * and range // covered
   */
  private Range range = new Range();
  /** Covered. */
  private List<StringType> enumeration = new ArrayList<>();
  /** Covered. */
  private String description;
  /** Covered. */
  private String dataModelVersion;
  /** Covered. */
  private String dataModelStatus;
  /** Covered. */
  private boolean command;

  public String getDataModelStatus() {
    return dataModelStatus;
  }

  public void setDataModelStatus(String dataModelStatus) {
    this.dataModelStatus = dataModelStatus;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isForcedInform() {
    return forcedInform;
  }

  public void setForcedInform(boolean forcedInform) {
    this.forcedInform = forcedInform;
  }

  public boolean isReadOnly() {
    return readOnly;
  }

  public void setReadOnly(boolean readOnly) {
    this.readOnly = readOnly;
  }

  public TR069DMType getDatatype() {
    return datatype;
  }

  public void setDatatype(TR069DMType datatype) {
    this.datatype = datatype;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getDataModelVersion() {
    return dataModelVersion;
  }

  public void setDataModelVersion(String dataModelVersion) {
    this.dataModelVersion = dataModelVersion;
  }

  public String toString() {
    StringBuilder output = new StringBuilder();
    output.append(String.format("%-12s", datatype.toString()));
    output.append(String.format("%-10s", isReadOnly() ? "ReadOnly" : "ReadWrite"));
    output.append(String.format("%-25s", range.toString()));
    String enumStr = "";
    if (enumeration == null || enumeration.isEmpty()) {
      enumStr += "NO-ENUM/PATTERN";
    } else if (enumeration.size() == 1) {
      StringType st = enumeration.get(0);
      if (st.getPattern() != null) {
        enumStr += "PAT:" + st.getPattern();
      }
      if (st.getValue() != null) {
        enumStr += "VAL:" + st.getValue();
      }
    } else {
      StringType st = enumeration.get(0);
      if (st.getPattern() != null) {
        enumStr += enumeration.size() + " PATTERNS";
      }
      if (st.getValue() != null) {
        enumStr += enumeration.size() + " VALUES";
      }
    }
    output.append(String.format("%-25s", enumStr));
    output.append(String.format("%-7s", isList() ? "LIST" : ""));
    output.append(String.format("%-100s", name));
    return output.toString();
    //		return "name:" + name + ", ro:" + readOnly + ", dmv:" + dataModelVersion + ", no:" +
    // notification + ", dms:" + dataModelStatus + ", fi:" + forcedInform + ", co:" + command;
  }

  public String getNotification() {
    return notification;
  }

  public void setNotification(String notification) {
    this.notification = notification;
  }

  public boolean isCommand() {
    return command;
  }

  public void setCommand(boolean command) {
    this.command = command;
  }

  public Range getRange() {
    return range;
  }

  public void setRange(Range range) {
    this.range = range;
  }

  public List<StringType> getEnumeration() {
    return enumeration;
  }

  public void setEnumeration(List<StringType> enumeration) {
    this.enumeration = enumeration;
  }

  public boolean isList() {
    return list;
  }

  public void setList(boolean list) {
    this.list = list;
  }

  public boolean hasSpecificRange() {
    return range.getMin() != null || range.getMax() != null;
  }
}
