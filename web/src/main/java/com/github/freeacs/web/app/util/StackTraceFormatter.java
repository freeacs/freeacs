package com.github.freeacs.web.app.util;

import java.io.StringWriter;

/**
 * For printing stack traces in a human readable format.
 *
 * @author Jarl Andre Hubenthal
 */
public abstract class StackTraceFormatter {
  /**
   * Produces an HTML string of the stacktrace.
   *
   * @param e a throwable
   * @return a html string
   */
  public static String getStackTraceAsHTML(Throwable e) {
    StringWriter string = new StringWriter();
    string.write("<br />");
    for (int i = 0; i < e.getStackTrace().length; i++) {
      String stack = e.getStackTrace()[i] + "<br />";
      stack = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + stack;
      string.write(stack);
    }
    return string.toString();
  }
}
