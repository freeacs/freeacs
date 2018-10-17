package com.github.freeacs.web.app.page.trigger;

import com.github.freeacs.dbi.ScriptExecution;
import com.github.freeacs.dbi.Trigger;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * This class is custom-tailored to display data on the TriggerRelease-page.
 *
 * @author Morten
 */
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

  public int getNoEvents() {
    return noEvents;
  }

  public void setNoEvents(int noEvents) {
    this.noEvents = noEvents;
  }

  public int getNoEventsPrUnit() {
    return noEventsPrUnit;
  }

  public void setNoEventsPrUnit(int noEventsPrUnit) {
    this.noEventsPrUnit = noEventsPrUnit;
  }

  public int getNoUnits() {
    return noUnits;
  }

  public void setNoUnits(int noUnits) {
    this.noUnits = noUnits;
  }

  private int noEvents;
  private int noEventsPrUnit;
  private int noUnits;

  public Date getReleasedTms() {
    return releasedTms;
  }

  private String formatDateForDisplay(Date d) {
    if (d != null) {
      Calendar midnight = Calendar.getInstance();
      midnight.set(Calendar.HOUR_OF_DAY, 0);
      midnight.set(Calendar.MINUTE, 0);
      midnight.set(Calendar.SECOND, 0);
      midnight.set(Calendar.MILLISECOND, 0);
      if (d.before(midnight.getTime())) {
        return yesterdayFormat.format(d);
      } else {
        return todayFormat.format(d);
      }
    }
    return "";
  }

  public void setReleasedTms(Date released) {
    this.releasedTms = released;
  }

  public String getDisplayScriptFinished() {
    if (scriptExecution != null) {
      return formatDateForDisplay(scriptExecution.getEndTms());
    } else {
      return "";
    }
  }

  public String getDisplayScriptStarted() {
    if (scriptExecution != null) {
      return formatDateForDisplay(scriptExecution.getStartTms());
    } else {
      return "";
    }
  }

  public String getDisplayScriptRequested() {
    if (scriptExecution != null) {
      return formatDateForDisplay(scriptExecution.getRequestTms());
    } else {
      return "";
    }
  }

  public String getDisplayFirstEvent() {
    return formatDateForDisplay(firstEventTms);
  }

  public String getDisplayReleased() {
    return formatDateForDisplay(releasedTms);
  }

  public String getDisplayNotified() {
    return formatDateForDisplay(notifiedTms);
  }

  public String getSyslogPageQueryString() {
    return syslogPageQueryString;
  }

  public void setSyslogPageQueryString(String syslogPageQueryString) {
    this.syslogPageQueryString = syslogPageQueryString;
  }

  public Date getNotifiedTms() {
    return notifiedTms;
  }

  public void setNotifiedTms(Date notified) {
    this.notifiedTms = notified;
  }

  public Date getFirstEventTms() {
    return firstEventTms;
  }

  public void setFirstEventTms(Date firstEventTms) {
    this.firstEventTms = firstEventTms;
  }

  public Integer getReleaseId() {
    return releaseId;
  }

  public void setReleaseId(Integer releaseId) {
    this.releaseId = releaseId;
  }

  public ScriptExecution getScriptExecution() {
    return scriptExecution;
  }

  public void setScriptExecution(ScriptExecution scriptExecution) {
    this.scriptExecution = scriptExecution;
  }

  public Trigger getTrigger() {
    return trigger;
  }

  public void setTrigger(Trigger trigger) {
    this.trigger = trigger;
  }
}
