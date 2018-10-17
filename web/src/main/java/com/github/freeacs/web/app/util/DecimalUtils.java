package com.github.freeacs.web.app.util;

import java.text.DecimalFormat;

/** The Class DecimalUtils. */
public class DecimalUtils {
  /** The Enum Format. */
  public enum Format {
    /** The ON e_ decimal. */
    ONE_DECIMAL("0.0"),

    /** The TW o_ decimals. */
    TWO_DECIMALS("0.00"),

    /** The N o_ decimals. */
    NO_DECIMALS("0");

    /** The format. */
    String format;

    /**
     * Instantiates a new format.
     *
     * @param f the f
     */
    Format(String f) {
      format = f;
    }

    /**
     * Format.
     *
     * @param d the d
     * @return the string
     */
    public String format(Double d) {
      return new DecimalFormat(format).format(d);
    }
  }

  /**
   * Format with two decimals.
   *
   * @param d the d
   * @return the string
   */
  public static String formatWithTwoDecimals(Double d) {
    return Format.TWO_DECIMALS.format(d);
  }
}
