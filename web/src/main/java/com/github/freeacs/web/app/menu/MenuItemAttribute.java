package com.github.freeacs.web.app.menu;

/**
 * A single menu item attribute. Represents a name='value' for a list item (li).
 *
 * @author Jarl Andre Hubenthal
 */
public class MenuItemAttribute {
  /** The key. */
  private String key;

  /** The value. */
  private String value;

  /**
   * Instantiates a new menu item attribute.
   *
   * @param k the k
   * @param v the v
   */
  public MenuItemAttribute(String k, String v) {
    key = k;
    value = v;
  }

  /**
   * Gets the key.
   *
   * @return the key
   */
  public String getKey() {
    return key;
  }

  /**
   * Gets the value.
   *
   * @return the value
   */
  public String getValue() {
    return value;
  }
}
