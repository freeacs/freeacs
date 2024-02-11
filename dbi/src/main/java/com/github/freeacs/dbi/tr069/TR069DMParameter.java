package com.github.freeacs.dbi.tr069;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Data
public class TR069DMParameter {
  @Setter
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

    public Long getMax() {
      if (max != null) {
        return max;
      }
      return datatype.getMax();
    }

    public String toString() {
      return getMin() + "-" + getMax();
    }
  }

  @Setter
  @Getter
  public static class StringType {
    private String value;
    private String pattern;

    public StringType(String value, String pattern) {
      this.value = value;
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
}
