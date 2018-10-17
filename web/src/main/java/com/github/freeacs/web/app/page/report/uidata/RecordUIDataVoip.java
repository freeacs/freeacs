package com.github.freeacs.web.app.page.report.uidata;

import com.github.freeacs.dbi.report.RecordVoip;
import com.github.freeacs.web.app.page.AbstractWebPage;
import com.github.freeacs.web.app.util.TimeFormatter;
import freemarker.template.TemplateModelException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * This is a wrapper class for RecordVoip. Calculates numbers and percents for the voip status.
 *
 * <p>Contains a static converter method <code>convertRecords(List<RecordVoip> records)</code> for
 * converting a larger set of RecordVoip objects.
 *
 * @author Jarl Andre Hubenthal
 */
public class RecordUIDataVoip extends RecordVoip {
  /** The mos avg. */
  private Double mosAvg;

  /** The jitter avg. */
  private Double jitterAvg;

  /** The jitter max. */
  private Double jitterMax;

  /** The percent loss avg. */
  private Double percentLossAvg;

  /** The call length total in seconds. */
  private Long callLengthTotalInSeconds;

  /** The total score. */
  private Double totalScore;

  /** The row background style. */
  private String rowBackgroundStyle = "";

  /** Instantiates a new record ui data voip. */
  public RecordUIDataVoip() {}

  /**
   * Instantiates a new record ui data voip.
   *
   * @param record the record
   */
  public RecordUIDataVoip(RecordVoip record) {
    super(
        record.getTms(),
        record.getPeriodType(),
        record.getUnittypeName(),
        record.getProfileName(),
        record.getSoftwareVersion(),
        record.getLine());
    add(record);
    if (getMosAvg().get() != null) {
      mosAvg = (double) getMosAvg().get() / getMosAvg().getDividend();
    }
    if (getJitterAvg().get() != null) {
      jitterAvg = (double) getJitterAvg().get() / getJitterAvg().getDividend();
    }
    if (getJitterMax().get() != null) {
      jitterMax = (double) getJitterMax().get() / getJitterMax().getDividend();
    }
    if (getPercentLossAvg().get() != null) {
      percentLossAvg = (double) getPercentLossAvg().get() / getPercentLossAvg().getDividend();
    }
    if (getCallLengthTotal().get() != null) {
      callLengthTotalInSeconds = getCallLengthTotal().get();
    }
    if (getVoIPQuality() != null && getVoIPQuality().get() != null) {
      totalScore = (double) getVoIPQuality().get() / getVoIPQuality().getDividend();
    }
    try {
      if (totalScore != null) {
        rowBackgroundStyle =
            new AbstractWebPage.RowBackgroundColorMethod()
                .exec(Arrays.asList(totalScore.toString()));
      }
    } catch (TemplateModelException e) {
    }
  }

  /**
   * Gets the call start as string.
   *
   * @return the call start as string
   */
  public String getCallStartAsString() {
    Calendar cal = Calendar.getInstance();
    cal.setTime(getTms());
    cal.add(Calendar.MILLISECOND, -(this.callLengthTotalInSeconds.intValue() * 1000));
    Date d = cal.getTime();
    return getTmsAsStringInternal(d);
  }

  /**
   * Convert records.
   *
   * @param records the records
   * @return the list
   */
  public static List<RecordUIDataVoip> convertRecords(List<RecordVoip> records) {
    List<RecordUIDataVoip> list = new ArrayList<>();
    for (RecordVoip record : records) {
      list.add(new RecordUIDataVoip(record));
    }
    return list;
  }

  /**
   * Checks if is telephone call.
   *
   * @return true, if is telephone call
   */
  public boolean isTelephoneCall() {
    return getMosAvg().get() != null;
  }

  /**
   * Gets the tms as string.
   *
   * @return the tms as string
   */
  public String getTmsAsString() {
    return getTmsAsStringInternal(getTms());
  }

  /**
   * Gets the tms as string non breaking.
   *
   * @return the tms as string non breaking
   */
  public String getTmsAsStringNonBreaking() {
    return getTmsAsString().replace(" ", "&nbsp;");
  }

  /**
   * Gets the tms as string internal.
   *
   * @param d the d
   * @return the tms as string internal
   */
  private String getTmsAsStringInternal(Date d) {
    return RecordUIDataConstants.DATE_FORMAT.format(d);
  }

  /**
   * Gets the mos avg as string.
   *
   * @return the mos avg as string
   */
  public String getMosAvgAsString() {
    return RecordUIDataConstants.TWO_DECIMALS_FORMAT.format(mosAvg);
  }

  /**
   * Gets the mos avg as long.
   *
   * @return the mos avg as long
   */
  public double getMosAvgAsLong() {
    return mosAvg;
  }

  /**
   * Gets the jitter avg as long.
   *
   * @return the jitter avg as long
   */
  public double getJitterAvgAsLong() {
    return jitterAvg;
  }

  /**
   * Gets the jitter max as long.
   *
   * @return the jitter max as long
   */
  public double getJitterMaxAsLong() {
    return jitterMax;
  }

  /**
   * Gets the percent loss avg as string.
   *
   * @return the percent loss avg as string
   */
  public String getPercentLossAvgAsString() {
    return RecordUIDataConstants.NO_DECIMALS_FORMAT.format(percentLossAvg);
  }

  /**
   * Gets the call length total as long.
   *
   * @return the call length total as long
   */
  public long getCallLengthTotalAsLong() {
    return callLengthTotalInSeconds;
  }

  /**
   * Gets the call length total as string.
   *
   * @return the call length total as string
   * @throws TemplateModelException the template model exception
   */
  public String getCallLengthTotalAsString() throws TemplateModelException {
    return TimeFormatter.convertMs2HourMinSecString(callLengthTotalInSeconds * 1000);
  }

  /**
   * Gets the total score as string.
   *
   * @return the total score as string
   */
  public String getTotalScoreAsString() {
    return RecordUIDataConstants.NO_DECIMALS_FORMAT.format(totalScore);
  }

  /**
   * Gets the row background style.
   *
   * @return the row background style
   */
  public String getRowBackgroundStyle() {
    return rowBackgroundStyle;
  }

  public Double getTotalScore() {
    return totalScore;
  }
}
