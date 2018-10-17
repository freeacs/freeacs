package com.github.freeacs.web.app.page.unittype;

import java.util.ArrayList;
import java.util.List;

/** The Enum UnittypeParameterTypes. */
public enum UnittypeParameterTypes {
  /** The All. */
  All,

  /** The Configured. */
  Configured,

  /** The Unconfigured. */
  Unconfigured;

  /**
   * To list.
   *
   * @return the list
   */
  public static List<String> toList() {
    List<String> flags = new ArrayList<>();
    for (UnittypeParameterTypes flag : values()) {
      flags.add(flag.toString());
    }
    return flags;
  }
}
