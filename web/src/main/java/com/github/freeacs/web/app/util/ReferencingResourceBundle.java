package com.github.freeacs.web.app.util;

import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A resouce bundle that checks for self references.
 *
 * @author Jarl Andre Hubenthal
 */
public class ReferencingResourceBundle extends ResourceBundle {
  /** The resources. */
  private static ResourceBundle resources;

  /** The pattern. */
  private Pattern pattern;

  /** The silent. */
  private boolean silent;

  /**
   * Instantiates a new referencing resource bundle.
   *
   * @param basename the basename
   * @param locale the locale
   * @param silent the silent
   */
  public ReferencingResourceBundle(String basename, Locale locale, boolean silent) {
    resources = ResourceBundle.getBundle(basename, locale);
    this.pattern = Pattern.compile("\\{.*?\\}");
    this.silent = silent;
  }

  @Override
  public Enumeration<String> getKeys() {
    return resources.getKeys();
  }

  @Override
  protected Object handleGetObject(String key) {
    String value = resources.getString(key);
    if (value != null) {
      Matcher matcher = this.pattern.matcher(value);
      while (matcher.find()) {
        String match = matcher.group();
        String s = match.substring(1, match.length() - 1);
        try {
          String referencedValue = (String) handleGetObject(s);
          if (referencedValue != null) {
            value = value.replace(match, referencedValue);
          }
        } catch (MissingResourceException e) {
          if (!this.silent) {
            throw e;
          }
        }
      }
    }
    return value;
  }
}
