package com.github.freeacs.web.app.util;

/** The Class TimeFormatter. */
public abstract class TimeFormatter {
  /** Ms converted to hours:minutes:seconds string (hours may be >= 24) */
  public static String convertMs2HourMinSecString(long ms) {
    String format = String.format("%%0%dd", 2);
    ms = ms / 1000;
    String seconds = String.format(format, ms % 60);
    String minutes = String.format(format, (ms % 3600) / 60);
    String hours = String.format(format, ms / 3600);
    return hours + ":" + minutes + ":" + seconds;
  }

  /**
   * Ms converted to approximate time string. The idea is to make a short string following these
   * rules: if (ms > 2 days) return N days else if (ms > 2 hours) return N hours else if (ms > 2
   * minutes) return N minutes else return N seconds
   *
   * <p>N will be half-up rounded
   *
   * @param ms
   * @return
   */
  public static String convertMs2ApproxTimeString(long ms) {
    long seconds = Math.round((float) ms / 1000f);
    long minutes = Math.round((float) ms / 60000f);
    long hours = Math.round((float) ms / (60f * 60000f));
    long days = Math.round((float) ms / (24f * 60f * 60000f));
    if (days > 2) {
      return days + " days";
    } else if (hours > 2) {
      return hours + " hours";
    } else if (minutes > 2) {
      return minutes + " minutes";
    } else {
      return seconds + " seconds";
    }
  }
}
