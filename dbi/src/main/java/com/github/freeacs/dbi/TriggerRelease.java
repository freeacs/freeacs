package com.github.freeacs.dbi;

import java.util.Date;

/**
 * Created by IntelliJ IDEA. User: Morten Date: 11.01.12 Time: 10:02 To change this template use
 * File | Settings | File Templates.
 */
public class TriggerRelease {
  private Integer id;
  private Trigger trigger;
  private Integer noEvents;
  private Integer noEventsPrUnit;
  private Integer noUnits;
  private Date firstEventTms;
  private Date releaseTms;
  private Date sentTms;

  public TriggerRelease(
      Trigger trigger,
      Integer ne,
      Integer nepu,
      Integer nu,
      Date firstEventTms,
      Date releaseTms,
      Date sentTms) {
    setTrigger(trigger);
    setNoEvents(ne);
    setNoEventsPrUnit(nepu);
    setNoUnits(nu);
    setFirstEventTms(firstEventTms);
    setReleaseTms(releaseTms);
    setSentTms(sentTms);
  }

  public TriggerRelease(Trigger trigger, Date firstEventTms, Date releaseTms, Date sentTms) {
    setTrigger(trigger);
    setFirstEventTms(firstEventTms);
    setReleaseTms(releaseTms);
    setSentTms(sentTms);
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Trigger getTrigger() {
    return trigger;
  }

  public void setTrigger(Trigger trigger) {
    if (trigger == null) {
      throw new IllegalArgumentException("TriggerRelease Trigger cannot be null");
    }
    this.trigger = trigger;
  }

  public Integer getNoEvents() {
    return noEvents;
  }

  public void setNoEvents(int noEvents) {
    if (noEvents < 0) {
      throw new IllegalArgumentException("TriggerRelease noEvent cannot be < 0");
    }
    this.noEvents = noEvents;
  }

  public Integer getNoEventsPrUnit() {
    return noEventsPrUnit;
  }

  public void setNoEventsPrUnit(int noEventsPrUnit) {
    if (noEventsPrUnit < 0) {
      throw new IllegalArgumentException("TriggerRelease noEventsPrUnit cannot be < 0");
    }
    this.noEventsPrUnit = noEventsPrUnit;
  }

  public Integer getNoUnits() {
    return noUnits;
  }

  public void setNoUnits(int noUnits) {
    if (noUnits < 0) {
      throw new IllegalArgumentException("TriggerRelease noUnits cannot be < 0");
    }
    this.noUnits = noUnits;
  }

  public Date getReleaseTms() {
    return releaseTms;
  }

  public void setReleaseTms(Date releaseTms) {
    if (releaseTms == null) {
      throw new IllegalArgumentException("TriggerRelease ReleaseTms cannot be null");
    }
    this.releaseTms = releaseTms;
  }

  public Date getSentTms() {
    return sentTms;
  }

  public void setSentTms(Date sentTms) {
    this.sentTms = sentTms;
  }

  public Date getFirstEventTms() {
    return firstEventTms;
  }

  public void setFirstEventTms(Date firstEventTms) {
    if (firstEventTms == null) {
      throw new IllegalArgumentException("TriggerRelease FirstEventTms cannot be null");
    }
    this.firstEventTms = firstEventTms;
  }
}
