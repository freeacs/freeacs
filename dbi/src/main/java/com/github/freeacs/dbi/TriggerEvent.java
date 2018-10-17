package com.github.freeacs.dbi;

import java.util.Date;

/**
 * Created by IntelliJ IDEA. User: Morten Date: 10.01.12 Time: 21:20 To change this template use
 * File | Settings | File Templates.
 */
public class TriggerEvent {
  private Trigger trigger;
  private Date tms;
  private String unitId;

  public TriggerEvent(Trigger trigger, Date tms, String unitId) {
    setTrigger(trigger);
    setTms(tms);
    setUnitId(unitId);
  }

  public Trigger getTrigger() {
    return trigger;
  }

  public void setTrigger(Trigger trigger) {
    if (trigger == null) {
      throw new IllegalArgumentException("TriggerEvent trigger cannot be null");
    }
    this.trigger = trigger;
  }

  public Date getTms() {
    return tms;
  }

  public void setTms(Date tms) {
    if (tms == null) {
      throw new IllegalArgumentException("TriggerEvent tms cannot be null");
    }
    this.tms = tms;
  }

  public String getUnitId() {
    return unitId;
  }

  public void setUnitId(String unitId) {
    if (unitId == null) {
      throw new IllegalArgumentException("TriggerEvent Unitid cannot be null");
    }
    this.unitId = unitId;
  }
}
