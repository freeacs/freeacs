package com.github.freeacs.dbi.report;

import java.util.Calendar;
import java.util.Date;

/**
 * Insert a Date object and a periodType. Get a Date object in return, set to the first ms in the
 * period.
 *
 * @author Morten
 */
public class TmsConverter {
  private Calendar calendar;

  public TmsConverter() {
    this(Calendar.getInstance());
  }

  public TmsConverter(Calendar calendar) {
    this.calendar = calendar;
  }

  public Date convert(Date d, PeriodType periodType) {
    calendar.setTime(d);
    if (periodType == PeriodType.MONTH) {
      calendar.set(Calendar.DAY_OF_MONTH, 0);
      calendar.set(Calendar.HOUR_OF_DAY, 0);
      calendar.set(Calendar.MINUTE, 0);
      calendar.set(Calendar.SECOND, 0);
    } else if (periodType == PeriodType.DAY) {
      calendar.set(Calendar.HOUR_OF_DAY, 0);
      calendar.set(Calendar.MINUTE, 0);
      calendar.set(Calendar.SECOND, 0);
    } else if (periodType == PeriodType.HOUR) {
      calendar.set(Calendar.MINUTE, 0);
      calendar.set(Calendar.SECOND, 0);
    } else if (periodType == PeriodType.SECOND) {
      calendar.set(Calendar.SECOND, 0);
    }
    calendar.set(Calendar.MILLISECOND, 0);
    return calendar.getTime();
  }

  public Date rollForward(Date d, PeriodType periodType) {
    calendar.setTime(d);
    if (periodType == PeriodType.MONTH) {
      calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + 1);
    }
    if (periodType == PeriodType.DAY) {
      calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1);
    }
    if (periodType == PeriodType.HOUR) {
      calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) + 1);
    }
    if (periodType == PeriodType.MINUTE) {
      calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE) + 1);
    }
    if (periodType == PeriodType.SECOND) {
      calendar.set(Calendar.SECOND, calendar.get(Calendar.SECOND) + 1);
    }
    return calendar.getTime();
  }

  public int minute(Date d) {
    calendar.setTime(d);
    return calendar.get(Calendar.MINUTE);
  }

  public int hour(Date d) {
    calendar.setTime(d);
    return calendar.get(Calendar.HOUR_OF_DAY);
  }

  public int day(Date d) {
    calendar.setTime(d);
    return calendar.get(Calendar.DAY_OF_MONTH);
  }

  public int month(Date d) {
    calendar.setTime(d);
    return calendar.get(Calendar.MONTH);
  }
}
