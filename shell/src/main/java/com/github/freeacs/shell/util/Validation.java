package com.github.freeacs.shell.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class Validation {
  public static void numberOfArgs(String[] args, int argsExpected) {
    if (args.length < argsExpected) {
      if (args.length <= 1) {
        throw new IllegalArgumentException(
            "No arguments supplied, expected at least " + (argsExpected - 1));
      }
      if (args.length > 1) {
        String supplied = "";
        for (int i = 1; i < args.length; i++) {
          if (args[i].contains(" ")) {
            supplied += "\"" + args[i] + "\" ";
          } else {
            supplied += args[i] + " ";
          }
        }
        throw new IllegalArgumentException(
            "Too few argument (expected  at least "
                + (argsExpected - 1)
                + ") (arguments supplied: "
                + supplied
                + ")");
      }
    }
  }

  /**
   * @param args
   * @param str
   * @return
   * @deprecated - use matches() instead
   */
  public static boolean filter(String[] args, String str) {
    String patternStr = null;
    boolean equal = true;
    if (args.length > 1) {
      patternStr = args[1];
      if (patternStr.startsWith("!")) {
        patternStr = patternStr.substring(1);
        equal = false;
      }
    }
    if (patternStr != null) {
      Pattern pattern;
      try {
        pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
      } catch (PatternSyntaxException pse) {
        throw new IllegalArgumentException(
            "The filter argument is not allowed: "
                + pse.getMessage()
                + "(reason may be not expanded variable)");
      }
      Matcher matcher = pattern.matcher(str);
      if (matcher.find()) {
        return !equal;
      } else {
        return equal;
      }
    }
    return false;
  }

  /**
   * The method is a slightly modified version of String.matches(), since it also handles a ! at the
   * start of the regex to negate the search. Also handles a non-existing regex and non-existing
   * strToMatch
   *
   * @param regex - may also be null, then all strings matches (return true)
   * @param strToMatch - may also be null (return true)
   * @return
   */
  public static boolean matches(String regex, String strToMatch) {
    if (regex == null) {
      return true;
    }
    if (strToMatch == null) {
      return false;
    }
    boolean equal = true; // standard compare
    if (regex.startsWith("!")) {
      regex = regex.substring(1);
      equal = false; // negative compare
    }
    String[] regexChars = new String[] {"*", "+", "?", "^", "$"};
    boolean containsRexexChar = false;
    for (String regexChar : regexChars) {
      if (regex.contains(regexChar)) {
        containsRexexChar = true;
      }
    }
    if (!containsRexexChar) {
      regex = ".*" + regex + ".*";
    }
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(strToMatch);
    boolean match = matcher.find();
    if (equal) {
      return match;
    } else {
      return !match;
    }
  }

  /**
   * The method will match against all the string. If none matches, this method returns false.
   * Otherwise return true.
   *
   * @param regexp
   * @param strsToMatch
   * @return
   */
  public static boolean matches(String regexp, String... strsToMatch) {
    for (String strToMatch : strsToMatch) {
      if (matches(regexp, strToMatch)) {
        return true;
      }
    }
    return false;
  }
}
