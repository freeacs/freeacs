package com.github.freeacs.web.app.input;

import java.util.regex.Pattern;

/**
 * This class is used for escaping text. Commonly used to remove invalid content in text (html or
 * non text)
 *
 * @author Jarl Andre Hubenthal
 */
public class Escaping {
  /** The Enum EscapeType. */
  public enum EscapeType {
    /** The TAG s_ an d_ content. */
    TAGS_AND_CONTENT,

    /** The TAG s_ only. */
    TAGS_ONLY
  }

  /**
   * Removes the html tags.
   *
   * @param strHTML the str html
   * @param type the type
   * @return the string
   */
  public static String removeHTMLTags(String strHTML, EscapeType type) {
    Pattern pattern = null;

    String strTagLess = strHTML;

    if (type == EscapeType.TAGS_AND_CONTENT) {
      String regex = "<[^>]*>";
      pattern = Pattern.compile(regex);
      strTagLess = pattern.matcher(strTagLess).replaceAll("");
    } else if (type == EscapeType.TAGS_ONLY) {
      strTagLess = strTagLess.replaceAll("<", "");
      strTagLess = strTagLess.replaceAll(">", "");
    }

    return strTagLess;
  }

  /**
   * Removes the non alphanumeric characters.
   *
   * @param s the s
   * @return the string
   */
  public static String removeNonAlphanumericCharacters(String s) {
    return s.replaceAll("[^a-zA-Z0-9]", "");
  }
}
