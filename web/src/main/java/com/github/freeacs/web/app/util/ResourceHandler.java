package com.github.freeacs.web.app.util;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * A resource handler for retrieving the property values of the language/text properties.
 *
 * @author Jarl Andre Hubenthal
 */
public abstract class ResourceHandler {
  /** The locale properties. */
  private static ResourceBundle localeProperties;

  /**
   * Gets the properties.
   *
   * @return the properties
   */
  public static ResourceBundle getProperties() {
    if (localeProperties == null) {
      localeProperties =
          new ReferencingResourceBundle(
              WebProperties.getInstance().getProperties(), getLocale(), true);
    }
    return localeProperties;
  }

  /**
   * Gets the string.
   *
   * @param key the key
   * @return the string
   */
  public static String getString(String key) {
    if (localeProperties == null) {
      getProperties();
    }
    return localeProperties.getString(key);
  }

  /**
   * Gets the locale.
   *
   * @return the locale
   */
  private static Locale getLocale() {
    String locale = WebProperties.getInstance().getLocale();
    if (locale != null) {
      return new Locale(locale);
    }
    return Locale.getDefault();
  }
}
