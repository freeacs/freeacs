package com.github.freeacs.shell.menu;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {
  public static SimpleDateFormat outputFormatDefault = new SimpleDateFormat("yyyyMMdd-HHmm");
  public static SimpleDateFormat outputFormatExtended = new SimpleDateFormat("yyyyMMdd-HHmmss");

  private static Pattern absoluteTimePattern =
      Pattern.compile("(\\d{4})(\\d{2})(\\d{2})-?(\\d{2})?(\\d{2})?(\\d{2})?");
  private static Pattern offsetTimePattern = Pattern.compile("(\\d+)(m|h|d)");

  public static Date getDateFromOption(String optionValue) {
    Matcher m = absoluteTimePattern.matcher(optionValue);
    if (m.matches()) {
      Calendar calendar = Calendar.getInstance();
      calendar.set(Calendar.YEAR, Integer.parseInt(m.group(1)));
      calendar.set(Calendar.MONTH, Integer.parseInt(m.group(2)) - 1);
      calendar.set(Calendar.DATE, Integer.parseInt(m.group(3)));
      if (m.group(4) != null) {
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(m.group(4)));
      } else {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
      }
      if (m.group(5) != null) {
        calendar.set(Calendar.MINUTE, Integer.parseInt(m.group(5)));
      } else {
        calendar.set(Calendar.MINUTE, 0);
      }
      if (m.group(6) != null) {
        calendar.set(Calendar.SECOND, Integer.parseInt(m.group(6)));
      } else {
        calendar.set(Calendar.SECOND, 0);
      }
      calendar.set(Calendar.MILLISECOND, 0);
      return calendar.getTime();
    }
    m = offsetTimePattern.matcher(optionValue);
    if (m.find()) {
      Calendar calendar = Calendar.getInstance();
      if ("m".equals(m.group(2))) {
        calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE) - Integer.parseInt(m.group(1)));
      }
      if ("h".equals(m.group(2))) {
        calendar.set(
            Calendar.HOUR_OF_DAY,
            calendar.get(Calendar.HOUR_OF_DAY) - Integer.parseInt(m.group(1)));
      }
      if ("d".equals(m.group(2))) {
        calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) - Integer.parseInt(m.group(1)));
      }
      return calendar.getTime();
    }
    return null;
  }

  public static Date autoboxDate(String arg) {
    if ("NULL".equals(arg)) {
      return null;
    }
    return getDateFromOption(arg);
  }

  public static Integer autoboxInteger(String arg) {
    try {
      if ("NULL".equals(arg)) {
        return null;
      } else {
        return Integer.valueOf(arg);
      }
    } catch (NumberFormatException nfe) {
      throw new IllegalArgumentException("The argument " + arg + " was not a number (as expected)");
    }
  }

  public static String autoboxString(String arg) {
    if ("NULL".equals(arg)) {
      return null;
    } else {
      return arg;
    }
  }
}
