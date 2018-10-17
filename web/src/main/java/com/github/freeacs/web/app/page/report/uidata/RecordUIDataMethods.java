package com.github.freeacs.web.app.page.report.uidata;

/**
 * Contains static utility methods for use by the package.
 *
 * <p>Shared variables, like formatters, is placed in RecordUIDataConstants.
 *
 * @author Jarl Andre Hubenthal
 */
final class RecordUIDataMethods {
  /** The Constant BYTES_TO_KILOBYTES. */
  private static final int BYTES_TO_KILOBYTES = 1024;

  /** The Constant KILOBYTES_DIVIDEND. */
  private static final int KILOBYTES_DIVIDEND = 1024;

  /** The Constant NON_BREAKING. */
  private static final String NON_BREAKING = "&nbsp;";

  /**
   * Gets the kilo byte presentation.
   *
   * @param bytes the bytes
   * @return the kilo byte presentation
   */
  static String getKiloBytePresentation(Long bytes) {
    if (bytes == null) {
      return "&nbsp;";
    }
    if (bytes > BYTES_TO_KILOBYTES) {
      return RecordUIDataConstants.TWO_DECIMALS_FORMAT.format((double) bytes / KILOBYTES_DIVIDEND)
          + "&nbsp;KB";
    }
    return bytes + "&nbsp;B";
  }

  /**
   * Gets the mega byte presentation.
   *
   * @param bytes the bytes
   * @return the mega byte presentation
   */
  static String getMegaBytePresentation(Long bytes) {
    if (bytes == null) {
      return "&nbsp;";
    }
    if (bytes > BYTES_TO_KILOBYTES) {
      return RecordUIDataConstants.TWO_DECIMALS_FORMAT.format((double) bytes / KILOBYTES_DIVIDEND)
          + "&nbsp;MB";
    }
    return bytes + "&nbsp;KB";
  }

  /**
   * Append string if not non breaking.
   *
   * @param value the value
   * @param toAppend the to append
   * @return the string
   */
  static String appendStringIfNotNonBreaking(String value, String toAppend) {
    if (value != null && !NON_BREAKING.equals(value) && toAppend != null) {
      return value += NON_BREAKING + toAppend;
    }
    return value;
  }

  /**
   * Gets the to string or non breaking space.
   *
   * @param bytes the bytes
   * @return the to string or non breaking space
   */
  static String getToStringOrNonBreakingSpace(Long bytes) {
    if (bytes != null) {
      return bytes.toString();
    }
    return NON_BREAKING;
  }

  /**
   * Gets the percent.
   *
   * @param current the current
   * @param pool the pool
   * @return the percent
   */
  static double getPercent(Long current, Long pool) {
    if (current == null || pool == null) {
      return 0;
    }
    return (double) current / pool * 100d;
  }
}
