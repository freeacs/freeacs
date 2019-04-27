package com.github.freeacs.tr069.base;

import com.github.freeacs.common.util.TimeWindow;
import com.github.freeacs.dbi.util.SystemConstants;
import com.github.freeacs.dbi.util.SystemParameters;
import lombok.extern.slf4j.Slf4j;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Random;

@Slf4j
public class ServiceWindow {
  /** For random distribution of PII within the ServiceWindow. */
  private static Random random = new Random(System.currentTimeMillis());

  private TimeWindow timeWindow;
  private long currentTms;
  private ACSParameters ACSParameters;
  private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  public ServiceWindow(SessionDataI sessionData, boolean disruptive) {
    this.currentTms = System.currentTimeMillis();
    this.ACSParameters = sessionData.getAcsParameters();
    if (disruptive) {
      timeWindow =
          new TimeWindow(ACSParameters.getValue(SystemParameters.SERVICE_WINDOW_DISRUPTIVE));
    } else {
      timeWindow = new TimeWindow(ACSParameters.getValue(SystemParameters.SERVICE_WINDOW_REGULAR));
    }
  }

  private boolean isEnabled() {
    String enable = ACSParameters.getValue(SystemParameters.SERVICE_WINDOW_ENABLE);
    return enable == null || (!"0".equals(enable) && !"false".equalsIgnoreCase(enable));
  }

  /** Return the number of provisionings per week. Default value is once per day. */
  private float findFrequency() {
    String freq = ACSParameters.getValue(SystemParameters.SERVICE_WINDOW_FREQUENCY);
    float freqFloat = timeWindow.getWeeklyLength() / timeWindow.getDailyLength();
    if (freq != null) {
      try {
        freqFloat = Float.parseFloat(freq);
      } catch (Throwable t) {
      }
    }
    return freqFloat;
  }

  private float findSpread() {
    float freqFloat = (float) SystemConstants.DEFAULT_SERVICEWINDOW_SPREAD_INT / 100f;
    String freq = ACSParameters.getValue(SystemParameters.SERVICE_WINDOW_SPREAD);
    if (freq != null) {
      try {
        freqFloat = Float.valueOf(freq) / 100f;
      } catch (Throwable t) {
      }
    }
    return freqFloat;
  }

  public boolean isWithin() {
    return isWithin(currentTms);
  }

  /**
   * Return false if this time stamp is outside the service-window.
   *
   * @param tms
   * @return
   */
  private boolean isWithin(long tms) {
    return !isEnabled() || timeWindow.isWithinTimeWindow(tms);
  }

  /**
   * The repeatable job will maintain a fixed interval between each run, but it must still obey the
   * service window. Thus each time a repeatable job hits outside the service window, that job
   * execution is skipped. Example:
   *
   * <p>SW: mo-su:0800-0900 Job: repeat every 1200 sec
   *
   * <p>CPE contacts server at 0801 (first time after job has been created). The job executes at
   * 0801, 0821, 0841 on Monday. Then it continues to execute at 0801, 0821 and 0841 on Tuesday. All
   * the in-between job executions are skipped.
   *
   * <p>This principle makes it possible to create an interval of the job and a service window, such
   * that the job will skip several (or all) possible service windows. This is unfortunate, but it
   * seems more intuitive to keep the intervals fixed in real-time, than fixed through
   * service-window-time.
   *
   * @return
   */
  long calculateNextRepeatableTms(Long lastRunTms, long fixedInterval) {
    long nextRunTms = currentTms;
    if (fixedInterval == 0) {
      log.debug("Interval is 0, NRT is now!");
      return nextRunTms;
    }
    long fixedIntervalMs = fixedInterval * 1000L;
    if (lastRunTms == null) { // this subtraction will be canceled below
      log.debug("LRT = null");
      nextRunTms -= fixedIntervalMs;
    } else {
      log.debug("LRT = " + convert(lastRunTms));
      nextRunTms = lastRunTms;
    }
    if (currentTms - nextRunTms > 0) {
      long noIntervals = (currentTms - nextRunTms) / fixedIntervalMs;
      nextRunTms += (noIntervals - 1) * fixedIntervalMs;
    }

    do {
      nextRunTms += fixedIntervalMs;
      log.debug("Suggested NRT = " + convert(nextRunTms));
      long timeToNRT = nextRunTms - currentTms;
      if (timeToNRT
          < -PIIDecision.MINIMUM_PII
              * 1000L) { // do not test old tms, but let tms close to current time pass
        log.debug("Suggested NRT was rejected as too old (timeToNRT = " + timeToNRT / 1000 + " sec)");
        continue;
      }
      if (timeWindow.isWithinTimeWindow(nextRunTms)) {
        log.debug("Suggested NRT was accepted");
        break;
      } else {
        log.debug("Suggested NRT was rejected as outside SW");
      }
    } while (true);
    return nextRunTms;
  }

  /**
   * Calculates the PeriodicInformInterval (in seconds) in standard way following these rules or
   * guidelines: If Enabled it will be calculated according these rules a) PII must be within
   * ServiceWindow b) PII must be set according to the Frequency -> interval c) PII must be spread
   * according to Spread d) PII can be minimum 31 seconds If Not Enabled, it will be calculated
   * according to b) and d) only
   *
   * @return
   */
  long calculateStdPII() {
    if (isEnabled()) {
      long nextPIITms = 0;
      long nextInterval = calculateNextInterval();

      /*
       * If we're inside the ServiceWindow right now, calculate how much
       * is left of the ServiceWindow (leftOfTW). If nextInterval is
       * more into the future than can fit within leftOfTW, then subtract
       * all of leftOfTW from nextInterval. Otherwise next PeriodicInformInterval
       * will fit within this current Time-window (ServiceWindow) and we can just
       * add nextInterval to current time stamp.
       */
      if (timeWindow.isWithinTimeWindow(currentTms)) {
        long leftOfTW =
            timeWindow.getPreviousStartTms(currentTms) + timeWindow.getDailyLength() - currentTms;
        if (nextInterval > leftOfTW) {
          nextInterval -= leftOfTW;
        } else {
          nextPIITms = currentTms + nextInterval;
        } // We have our final time stamp..
      }

      /*
       * Check if nextInterval fits within the next TimeWindow. Continue
       * until nextInterval fits, and then calculate next Periodic Inform
       * Interval timestamp.
       */
      if (nextPIITms == 0) {
        nextPIITms = timeWindow.getNextStartTms(currentTms);
        while (timeWindow.getDailyLength() < nextInterval) {
          nextInterval -= timeWindow.getDailyLength();
          nextPIITms = timeWindow.getNextStartTms(nextPIITms);
        }
        nextPIITms += nextInterval;
      }

      // Convert from timestamp (ms) back to Periodic Inform Interval (seconds
      // til next provisioning).
      long nextPII = (nextPIITms - currentTms) / 1000;
      log.debug("Standard PeriodicInformInterval calculated to "
              + nextPII
              + "("
              + convert(nextPIITms)
              + ") (TimeWindow is : "
              + timeWindow
              + ")");
      if (nextPII < PIIDecision.MINIMUM_PII) {
        log.debug("Standard PeriodicInformInterval was calculated too low, changed to "
                + PIIDecision.MINIMUM_PII);
        nextPII = PIIDecision.MINIMUM_PII;
      }
      // Return as seconds
      return nextPII;
    } else {
      // Make sure frequency is set to once pr day
      String freq = ACSParameters.getValue(SystemParameters.SERVICE_WINDOW_FREQUENCY);
      float freqFloat = 7; // default - once pr day
      if (freq != null) {
        try {
          freqFloat = Float.parseFloat(freq);
        } catch (Throwable t) {
        }
      }
      // Find the nextInterval (calculations are all in seconds)
      long nextPII = (long) ((float) (7 * 24 * 3600) / freqFloat);
      long nextPIITms = (currentTms + nextPII * 1000L) / 1000;
      log.debug("Standard PeriodicInformInterval (SW disabled) calculated to "
              + nextPII
              + "("
              + convert(nextPIITms)
              + ") (TimeWindow is : "
              + timeWindow
              + ")");
      // make sure it is above 30 seconds
      if (nextPII < PIIDecision.MINIMUM_PII) {
        log.debug("Standard PeriodicInformInterval (SW disabled) was calculated too low, set to "
                + PIIDecision.MINIMUM_PII);
        nextPII = PIIDecision.MINIMUM_PII;
      }
      return nextPII;
    }
  }

  /**
   * This method will calculate the next interval to a provisioning. However, it is important to
   * understand that this interval relates to the time windows defined be this ServiceWindow class.
   * Here's an example to explain the issue:
   *
   * <p>A ServiceWindow can be defined as "mo-we:0800-1200". In this case the total number of hours
   * available for provisioning during a week is 4h*3days=12h Furthermore, assume that the frequency
   * is set to 2. This means that there should be a provisioning every 6h. This number is
   * represented by defaultInterval in the code. Assuming that there is a provisioning at 8am Monday
   * morning, you must understand that the next should not happen before 10am Tuesday morning, but
   * the number returned from this method is still 6h (or rather translated to milliseconds).
   *
   * <p>Additionally we have something called SpreadFactor. This is a factor from 0-100%
   * (represented as a float going from 0 to 1), which will spread the interval accordingly. We
   * calculate spread
   *
   * <p>Spread is calculated like this:
   *
   * <p>Spread = random(2*DefaultInterval*SpreadFactor)-DefaultInterval*SpreadFactor
   *
   * <p>If SpreadFactor is 50% and DefaultInterval is 6h, the Spread can now range from -3h to 3h.
   * This Spread is added to Default Interval to give the FinalInterval.
   *
   * @return
   */
  private long calculateNextInterval() {
    // The spread of the interval. Default is 0.5 (= 50%).
    float spreadFactor = findSpread();
    // find frequency, default is once a day (expressed as 7 if service window
    // defines all days of the week (= mo-su))
    float frequency = findFrequency();
    // The length (in ms) of a time-window
    // The freq-time-window is the time allowed for provisioning during a whole week
    // divided by the frequency of provisionings pr week. Thus 24*7h weekly prov.
    // time and frequency=7 will give 24h time-window.
    long defaultInterval = (long) ((float) timeWindow.getWeeklyLength() / frequency);
    if (spreadFactor == 0f) { // Special code to treat 0 spread (random function cannot handle 0)
      log.debug("DefaultInterval calculated to "
              + defaultInterval / 1000
              + " seconds - spreadfactor is 0");
    } else {
      int ds = (int) (defaultInterval * spreadFactor);
      defaultInterval = defaultInterval + random.nextInt(ds * 2) - ds;
      log.debug("DefaultInterval calculated to "
              + defaultInterval / 1000
              + " seconds - spreadfactor is "
              + spreadFactor * 100);
    }
    return defaultInterval;
  }

  long getCurrentTms() {
    return currentTms;
  }

  static String convert(Long tms) {
    return sdf.format(new Date(tms));
    //		return String.format("%1$tF %1$tR", tms);
  }

  TimeWindow getTimeWindow() {
    return timeWindow;
  }
}
