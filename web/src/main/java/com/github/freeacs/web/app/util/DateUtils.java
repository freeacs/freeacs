package com.github.freeacs.web.app.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * To enable synchronization (possibly, but not implemented) for {@link SimpleDateFormat}.
 *
 * @author Jarl Andre Hubenthal
 */
public abstract class DateUtils {
  /** The Enum Format. */
  public enum Format {
    /** The DAT e_ only. */
    DATE_ONLY(ResourceHandler.getString("DATE_FORMAT_NOTIME")),

    /** The DEFAULT. */
    DEFAULT(ResourceHandler.getString("DATE_FORMAT")),

    /** The WIT h_ seconds. */
    WITH_SECONDS(ResourceHandler.getString("DATE_FORMAT_WITH_SECONDS"));

    /** The format. */
    String format;

    /**
     * Instantiates a new format.
     *
     * @param s the s
     */
    Format(String s) {
      format = s;
    }

    /**
     * Format.
     *
     * @param d the d
     * @return the string
     */
    public String format(Date d) {
      return formatDate(this, d);
    }

    /**
     * Parses the.
     *
     * @param d the d
     * @return the date
     * @throws ParseException the parse exception
     */
    public Date parse(String d) throws ParseException {
      return parseDate(this, d);
    }
  }

  /**
   * Format date default.
   *
   * @param d the d
   * @return the string
   */
  public static String formatDateDefault(Date d) {
    return formatDate(Format.DEFAULT, d);
  }

  /**
   * Parses the date default.
   *
   * @param d the d
   * @return the date
   * @throws ParseException the parse exception
   */
  public static Date parseDateDefault(String d) throws ParseException {
    return parseDate(Format.DEFAULT, d);
  }

  /**
   * Format date.
   *
   * @param dateOnly the date only
   * @param d the d
   * @return the string
   */
  public static String formatDate(Format dateOnly, Date d) {
    SimpleDateFormat format = new SimpleDateFormat(dateOnly.format);
    return format.format(d);
  }

  /**
   * Parses the date.
   *
   * @param defaultD the default d
   * @param d the d
   * @return the date
   * @throws ParseException the parse exception
   */
  public static Date parseDate(Format defaultD, String d) throws ParseException {
    SimpleDateFormat format = new SimpleDateFormat(defaultD.format);
    return format.parse(d);
  }

  public static String getUpTime(long min) {
    int days = (int) (min / 60 / 24);
    int hours = (int) (min / 60) - days * 24;
    int minutes = (int) min - (hours + days * 24) * 60;

    String upTime = "";
    if (days != 0) {
      upTime += days + "d ";
    }
    if (hours != 0) {
      upTime += hours + "h ";
    }
    if (minutes != 0) {
      upTime += min + "m ";
    }

    return upTime;
  }
}
