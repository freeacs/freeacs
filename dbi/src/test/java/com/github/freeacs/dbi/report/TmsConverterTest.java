package com.github.freeacs.dbi.report;

import static org.mockito.Mockito.*;

import java.util.Calendar;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;

public class TmsConverterTest {

  private Calendar calendar;
  private TmsConverter converter;

  @Before
  public void init() {
    calendar = mock(Calendar.class);
    converter = new TmsConverter(calendar);
  }

  @Test
  public void convertMonth() {
    // Given:
    final Date date = new Date(2018 - 1900, 10, 1);
    final PeriodType periodType = PeriodType.MONTH;

    // When:
    converter.convert(date, periodType);

    // Then:
    verify(calendar).setTime(date);
    verify(calendar, never()).set(Calendar.YEAR, 0);
    verify(calendar, never()).set(Calendar.MONTH, 0);
    verify(calendar).set(Calendar.DAY_OF_MONTH, 0);
    verify(calendar).set(Calendar.HOUR_OF_DAY, 0);
    verify(calendar).set(Calendar.MINUTE, 0);
    verify(calendar).set(Calendar.SECOND, 0);
    verify(calendar).set(Calendar.MILLISECOND, 0);
    verify(calendar).getTime();
  }

  @Test
  public void convertDay() {
    // Given:
    final Date date = new Date(2018 - 1900, 10, 1);
    final PeriodType periodType = PeriodType.DAY;

    // When:
    converter.convert(date, periodType);

    // Then:
    verify(calendar).setTime(date);
    verify(calendar, never()).set(Calendar.YEAR, 0);
    verify(calendar, never()).set(Calendar.MONTH, 0);
    verify(calendar, never()).set(Calendar.DAY_OF_MONTH, 0);
    verify(calendar).set(Calendar.HOUR_OF_DAY, 0);
    verify(calendar).set(Calendar.MINUTE, 0);
    verify(calendar).set(Calendar.SECOND, 0);
    verify(calendar).set(Calendar.MILLISECOND, 0);
    verify(calendar).getTime();
  }

  @Test
  public void convertHour() {
    // Given:
    final Date date = new Date(2018 - 1900, 10, 1);
    final PeriodType periodType = PeriodType.HOUR;

    // When:
    converter.convert(date, periodType);

    // Then:
    verify(calendar).setTime(date);
    verify(calendar, never()).set(Calendar.YEAR, 0);
    verify(calendar, never()).set(Calendar.MONTH, 0);
    verify(calendar, never()).set(Calendar.DAY_OF_MONTH, 0);
    verify(calendar, never()).set(Calendar.HOUR_OF_DAY, 0);
    verify(calendar).set(Calendar.MINUTE, 0);
    verify(calendar).set(Calendar.SECOND, 0);
    verify(calendar).set(Calendar.MILLISECOND, 0);
    verify(calendar).getTime();
  }

  @Test
  public void convertSecond() {
    // Given:
    final Date date = new Date(2018 - 1900, 10, 1);
    final PeriodType periodType = PeriodType.SECOND;

    // When:
    converter.convert(date, periodType);

    // Then:
    verify(calendar).setTime(date);
    verify(calendar, never()).set(Calendar.YEAR, 0);
    verify(calendar, never()).set(Calendar.MONTH, 0);
    verify(calendar, never()).set(Calendar.DAY_OF_MONTH, 0);
    verify(calendar, never()).set(Calendar.HOUR_OF_DAY, 0);
    verify(calendar, never()).set(Calendar.MINUTE, 0);
    verify(calendar).set(Calendar.SECOND, 0);
    verify(calendar).set(Calendar.MILLISECOND, 0);
    verify(calendar).getTime();
  }

  @Test
  public void rollForwardMonth() {
    // Given:
    final Date date = new Date(2018 - 1900, 10, 1);
    final PeriodType periodType = PeriodType.MONTH;

    // When:
    converter.rollForward(date, periodType);

    // Then:
    verify(calendar).setTime(date);
    verify(calendar).set(Calendar.MONTH, calendar.get(Calendar.MONTH) + 1);
    verify(calendar).getTime();
  }

  @Test
  public void rollForwardDay() {
    // Given:
    final Date date = new Date(2018 - 1900, 10, 1);
    final PeriodType periodType = PeriodType.DAY;

    // When:
    converter.rollForward(date, periodType);

    // Then:
    verify(calendar).setTime(date);
    verify(calendar).set(Calendar.MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1);
    verify(calendar).getTime();
  }

  @Test
  public void rollForwardHour() {
    // Given:
    final Date date = new Date(2018 - 1900, 10, 1);
    final PeriodType periodType = PeriodType.HOUR;

    // When:
    converter.rollForward(date, periodType);

    // Then:
    verify(calendar).setTime(date);
    verify(calendar).set(Calendar.MONTH, calendar.get(Calendar.HOUR_OF_DAY) + 1);
    verify(calendar).getTime();
  }

  @Test
  public void rollForwardMinute() {
    // Given:
    final Date date = new Date(2018 - 1900, 10, 1);
    final PeriodType periodType = PeriodType.MINUTE;

    // When:
    converter.rollForward(date, periodType);

    // Then:
    verify(calendar).setTime(date);
    verify(calendar).set(Calendar.MONTH, calendar.get(Calendar.MINUTE) + 1);
    verify(calendar).getTime();
  }

  @Test
  public void rollForwardSecond() {
    // Given:
    final Date date = new Date(2018 - 1900, 10, 1);
    final PeriodType periodType = PeriodType.SECOND;

    // When:
    converter.rollForward(date, periodType);

    // Then:
    verify(calendar).setTime(date);
    verify(calendar).set(Calendar.MONTH, calendar.get(Calendar.SECOND) + 1);
    verify(calendar).getTime();
  }

  @Test
  public void minute() {
    // Given:
    final Date date = new Date(2018 - 1900, 10, 1);

    // When:
    converter.minute(date);

    // Then:
    verify(calendar).setTime(date);
    verify(calendar).get(Calendar.MINUTE);
  }

  @Test
  public void hour() {
    // Given:
    final Date date = new Date(2018 - 1900, 10, 1);

    // When:
    converter.hour(date);

    // Then:
    verify(calendar).setTime(date);
    verify(calendar).get(Calendar.HOUR_OF_DAY);
  }

  @Test
  public void day() {
    // Given:
    final Date date = new Date(2018 - 1900, 10, 1);

    // When:
    converter.day(date);

    // Then:
    verify(calendar).setTime(date);
    verify(calendar).get(Calendar.DAY_OF_MONTH);
  }

  @Test
  public void month() {
    // Given:
    final Date date = new Date(2018 - 1900, 10, 1);

    // When:
    converter.month(date);

    // Then:
    verify(calendar).setTime(date);
    verify(calendar).get(Calendar.MONTH);
  }
}
