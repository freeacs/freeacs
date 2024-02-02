package com.github.freeacs.web.app.menu;

import com.github.freeacs.web.Page;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Serves as a model for page menu items.
 *
 * @author Jarl Andre Hubenthal
 */
@Getter
public class MenuItem {
  // The link url
  /** The url. */
  private String url;
  // The actual link text
  /** The display. */
  private String display;
  // MenuItem children
  /** The sub menu items. */
  private List<MenuItem> subMenuItems = new ArrayList<>();
  // Adds class="selected" if true
  /** The selected. */
  private boolean selected;

  /** The attributes. */
  private final List<MenuItemAttribute> attributes = new ArrayList<>();

  /**
   * Instantiates a new menu item.
   *
   * @param display the display
   * @param url the url
   * @param children the children
   */
  public MenuItem(String display, String url, List<MenuItem> children) {
    this.url = url;
    this.display = display;
    this.subMenuItems = children;
  }

  /**
   * Instantiates a new menu item.
   *
   * @param display the display
   * @param page the page
   */
  public MenuItem(String display, Page page) {
    this.url = page.getUrl();
    this.display = display;
  }

  /**
   * Instantiates a new menu item.
   *
   * @param display the display
   * @param url the url
   */
  public MenuItem(String display, String url) {
    this.url = url;
    this.display = display;
  }

  /**
   * Adds the command.
   *
   * @param cmd the cmd
   * @return the menu item
   */
  public MenuItem addCommand(String cmd) {
    this.url += "&amp;cmd=" + cmd;
    return this;
  }

  /**
   * Adds the parameter.
   *
   * @param key the key
   * @param value the value
   * @return the menu item
   */
  public MenuItem addParameter(String key, String value) {
    this.url += "&amp;" + key + "=" + value;
    return this;
  }

  /**
   * Adds the sub menu item.
   *
   * @param item the item
   */
  public void addSubMenuItem(MenuItem item) {
    this.subMenuItems.add(item);
  }

  /**
   * Adds the sub menu items.
   *
   * @param items the items
   * @return the menu item
   */
  public MenuItem addSubMenuItems(MenuItem... items) {
    java.util.Collections.addAll(this.subMenuItems, items);
    return this;
  }

  /**
   * Sets the disable on click with java script.
   *
   * @return the menu item
   */
  public MenuItem setDisableOnClickWithJavaScript() {
    addAttribute("onclick", "return false;");
    return this;
  }

  /**
   * Sets the url.
   *
   * @param url the url
   * @return the menu item
   */
  public MenuItem setUrl(String url) {
    this.url = url;
    return this;
  }

  /**
   * Sets the display.
   *
   * @param display the display
   * @return the menu item
   */
  public MenuItem setDisplay(String display) {
    this.display = display;
    return this;
  }

  /**
   * Sets the selected.
   *
   * @param selected the selected
   * @return the menu item
   */
  public MenuItem setSelected(boolean selected) {
    this.selected = selected;
    return this;
  }

  /**
   * Adds the attribute.
   *
   * @param key   the key
   * @param value the value
   */
  public void addAttribute(String key, String value) {
    for (MenuItemAttribute attr : attributes) {
      if (attr.getKey().equals(key)) {
        throw new RuntimeException(
            "Wrong usage of MenuItem.addAttribute. Trying to add [" + key + "] twice.");
      }
    }
    this.attributes.add(new MenuItemAttribute(key, value));
  }
}
