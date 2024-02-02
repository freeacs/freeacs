package com.github.freeacs.web.app.page.trigger;

import com.github.freeacs.dbi.ScriptExecution;
import com.github.freeacs.dbi.Trigger;
import lombok.Getter;
import lombok.Setter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * This class is custom-tailored to display data on the TriggerRelease-page.
 *
 * @author Morten
 */
@Getter
@Setter
public class ReleaseTrigger {
  private static SimpleDateFormat todayFormat = new SimpleDateFormat("HH:mm");
  private static SimpleDateFormat yesterdayFormat = new SimpleDateFormat("MMM-dd HH:mm", Locale.US);
  private Date firstEventTms;
  private Date releasedTms;
  private Date notifiedTms;
  private Integer releaseId;
  private String syslogPageQueryString;
  private ScriptExecution scriptExecution;
  private Trigger trigger;

  public void setNoEvents(int noEvents) {
    this.noEvents = noEvents;
  }

  public void setNoEventsPrUnit(int noEventsPrUnit) {
    this.noEventsPrUnit = noEventsPrUnit;
  }

  public void setNoUnits(int noUnits) {
    this.noUnits = noUnits;
  }

  private int noEvents;
  private int noEventsPrUnit;
  private int noUnits;

}
