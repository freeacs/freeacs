package com.github.freeacs.web.app.table;

/** The Enum TableColor. */
public enum TableColor {
  /** The GREEN. */
  GREEN("ccdcb7"),

  /** The GRAY. */
  GRAY("CCCCCC"),

  /** The ORANG e_ light. */
  ORANGE_LIGHT("FFC62D"),

  /** The ORANG e_ dark. */
  ORANGE_DARK("E49400"),

  /** The RED. */
  RED("e58850"),

  /** The WHITE. */
  WHITE("FFFFFF"),

  /** The BLACK. */
  BLACK("000000");

  /** The color hex. */
  private String colorHex;

  /**
   * Instantiates a new table color.
   *
   * @param color the color
   */
  TableColor(String color) {
    this.colorHex = color;
  }

  public String toString() {
    return colorHex;
  }
}
