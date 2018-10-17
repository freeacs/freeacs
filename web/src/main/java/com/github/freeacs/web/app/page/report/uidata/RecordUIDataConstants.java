package com.github.freeacs.web.app.page.report.uidata;

import com.github.freeacs.web.app.util.DateUtils;
import com.github.freeacs.web.app.util.DecimalUtils;

/**
 * Should instead of being in its own class be placed in RecordUIDataMethods, that in turn should be
 * renamed to RecordUIDataUtils.
 *
 * @author Jarl Andre Hubenthal
 */
final class RecordUIDataConstants {
  /** The Constant DATE_FORMAT. */
  public static final DateUtils.Format DATE_FORMAT = DateUtils.Format.WITH_SECONDS;

  /** The Constant TWO_DECIMALS_FORMAT. */
  public static final DecimalUtils.Format TWO_DECIMALS_FORMAT = DecimalUtils.Format.TWO_DECIMALS;

  /** The Constant NO_DECIMALS_FORMAT. */
  public static final DecimalUtils.Format NO_DECIMALS_FORMAT = DecimalUtils.Format.NO_DECIMALS;
}
