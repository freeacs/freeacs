package com.github.freeacs.web.app.page.scriptexecution;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Contains methods related to special String operation related to xAPS Web.
 *
 * @author Morten Simonsen, Jarl Andre Hubenthal
 */
public class StringUtil {
  /** The pattern. */
  private static Pattern pattern = Pattern.compile("(\"([^\"]*)\")|([^ \"\t]+)");

  /**
   * Replaces the old split function, since the old function did not handle strings which had quotes
   * in the middle of a string. This was a problem when using the special root-char :. For example
   * this string would not be properly split: call script.xss :"Testperf" TestperfP;
   *
   * @param s the s
   * @return The String array
   */
  public static String[] split(String s) {
    Matcher m = pattern.matcher(s);
    int pos = 0;
    List<String> commands = new ArrayList<>();
    while (m.find(pos)) {
      String group1 = m.group(2);
      String group2 = m.group(3);
      if (group1 != null) {
        commands.add(group1);
      } else if (group2 != null) {
        commands.add(group2);
      }
      pos = m.end();
    }
    String[] strArray = new String[commands.size()];
    commands.toArray(strArray);
    return strArray;
  }
}
