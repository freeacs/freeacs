package com.github.freeacs.web.app.page.unittype;

import java.util.ArrayList;
import java.util.List;

/** The Enum UnittypeParameterFlags. */
public enum UnittypeParameterFlags {
  /** The ALL. */
  ALL("All"),

  /** The R. */
  R("Read-Only"),

  /** The RW. */
  RW("Read-Write"),

  /** The X. */
  X("System"),

  /** The C. */
  C("Confidential"),

  /** The NO t_ x. */
  NOT_X("Device"),

  /** The D. */
  D("Displayable"),

  /** The I. */
  I("Inspection"),

  /** The S. */
  S("Searchable"),

  /** The A. */
  A("Always-Read"),

  /** The B. */
  B("Boot-Required");

  /**
   * Gets the by value.
   *
   * @param value the value
   * @return the by value
   */
  public static UnittypeParameterFlags getByValue(String value) {
    if (value != null) {
      for (UnittypeParameterFlags utpf : values()) {
        if (utpf.toString().equals(value)) {
          return utpf;
        }
      }
    }
    return null;
  }

  /**
   * To list.
   *
   * @return the list
   */
  public static List<String> toList() {
    List<String> flags = new ArrayList<>();
    for (UnittypeParameterFlags flag : values()) {
      flags.add(flag.toString());
    }
    return flags;
  }

  /** The display. */
  private String display;

  /**
   * Instantiates a new unittype parameter flags.
   *
   * @param flag the flag
   */
  UnittypeParameterFlags(String flag) {
    display = flag;
  }

  public String toString() {
    return display;
  }
}
