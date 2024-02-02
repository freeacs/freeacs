package com.github.freeacs.web.app.menu;

import lombok.Getter;

/**
 * A single menu item attribute. Represents a name='value' for a list item (li).
 *
 * @author Jarl Andre Hubenthal
 */
@Getter
public class MenuItemAttribute {
  /** The key. */
  private final String key;

  /** The value. */
  private final String value;

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
}
