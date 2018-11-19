package com.github.freeacs.web;

import java.text.SimpleDateFormat;

public class H2CustomFunctions {
  public static String convertDatetimeToString(java.util.Date dttm, String format) {
    SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd");
    return dateFormat.format(dttm);
  }
}
