package com.github.freeacs.dbi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Trigger {
  public static int TRIGGER_TYPE_BASIC;
  public static int TRIGGER_TYPE_COMPOSITE = 1;

  public static int NOTIFY_TYPE_ALARM;
  public static int NOTIFY_TYPE_REPORT = 1;
  public static int NOTIFY_TYPE_SILENT = 2;

  public static int EVAL_PERIOD_MIN = 15;
  public static int EVAL_PERIOD_MAX = 120;

  public static int NOTIFY_INTERVAL_MIN = 1;
  public static int NOTIFY_INTERVAL_MAX = 168;

  public static Map<Integer, String> triggerTypeMap = new HashMap<>();
  public static Map<Integer, String> notifyTypeMap = new HashMap<>();

  static {
    triggerTypeMap.put(TRIGGER_TYPE_BASIC, "BASIC");
    triggerTypeMap.put(TRIGGER_TYPE_COMPOSITE, "COMPOSITE");
    notifyTypeMap.put(NOTIFY_TYPE_ALARM, "ALARM");
    notifyTypeMap.put(NOTIFY_TYPE_REPORT, "REPORT");
    notifyTypeMap.put(NOTIFY_TYPE_SILENT, "SILENT");
  }

  public static int getNotifyType(String notifyTypeStr) {
    for (Map.Entry<Integer, String> entry : notifyTypeMap.entrySet()) {
      if (entry.getValue().equals(notifyTypeStr)) {
        return entry.getKey();
      }
    }
    throw new IllegalArgumentException(
        "The notify type string " + notifyTypeStr + " is not allowed");
  }

  public static int getTriggerType(String triggerTypeStr) {
    for (Map.Entry<Integer, String> entry : triggerTypeMap.entrySet()) {
      if (entry.getValue().equals(triggerTypeStr)) {
        return entry.getKey();
      }
    }
    throw new IllegalArgumentException(
        "The notify type string " + triggerTypeStr + " is not allowed");
  }

  private Integer id;
  private String name;
  private String description;
  /** Can be TRIGGER_TYPE_BASIC or TRIGGER_TYPE_COMPOSITE. */
  private int triggerType;
  /** Can be ACTION_ALARM, ACTION_REPORT, ACTION_SCRIPT or ACTION_SILENT. */
  private int notifyType;

  private boolean active;
  private Unittype unittype;
  /** Period to verify trigger conditions. Valid range: 15-120 */
  private int evalPeriodMinutes;
  /** Minimum time period between an ALARM or a REPORT. */
  private Integer notifyIntervalHours;

  private File script;
  /** Parent must be a composite trigger and not any of your child triggers. */
  private Trigger parent;
  /** Comma separated email list - will be deleted in future. */
  private String toList;

  private SyslogEvent syslogEvent;
  /**
   * Cannot be NULL for BASIC trigger type, otherwise NULL is possible No of trigger events
   * required. Valid range: >0 (or NULL)
   */
  private Integer noEvents;

  private Integer noEventsPrUnit;
  /**
   * No of trigger event pr units required. Valid range: >0 (or NULL) No of units required. Valid
   * range: >0 (or NULL)
   */
  private Integer noUnits;
  /** Helper field to handle change/delete of syslog event. */
  private SyslogEvent oldSyslogEvent;
  /** Helper field to handle change of trigger name. */
  private String oldName;
  /** Derived field, not read directly from database. */
  private List<Trigger> children;

  /**
   * Future developments:
   *
   * <p>TriggerExpression: Allow an expression to be used to match on the syslog message for the
   * specified syslog event. Will allow more fine-grained control over which messages will become a
   * trigger event. One syslog event can be used in multiple trigger events - useful if you don't
   * want to make a lot of syslog events.
   *
   * <p>OppositeRelease: Instead of releasing trigger if a number of events have happened, then
   * release if too few events have happened. This could prove valuable if a service stops and
   * messages no longer are sent from the device/server.
   *
   * <p>AdvancedNumberOfFields: These fields could be made dynamic in the sense that they could take
   * advantage of group-count (if group is specified) and unittype-count and use these in simple
   * calculations. The numberOf-fields would then become fields which need to be evaluated by the
   * Core-server (example: "45*GC")
   */
  @SuppressWarnings("unused")
  // may be implemented in future
  private boolean oppositeRelease;
  /**
   * If set to true, the release will happen if too few events have been registered
   *
   * <p>Makes a Trigger-placeholder. Meant to be used in conjunction with the various set-methods,
   * instead of running the large constructor. Run the validate-method when completed the
   * set-methods to ensure that the object is valid. The reason for requiring these two arguments,
   * is that several other fields depends on these for validation.
   */
  public Trigger(int triggerType, int notifyType) {
    setTriggerType(triggerType);
    setNotifyType(notifyType);
  }

  public Trigger(
      String name,
      String description,
      int triggerType,
      int notifyType,
      boolean active,
      Unittype unittype,
      int evalPeriodMinutes,
      Integer notifyIntervalHours,
      File script,
      Trigger parent,
      String toList,
      SyslogEvent syslogEvent,
      Integer noEventsPrUnit,
      Integer noEvents,
      Integer noUnits) {
    setName(name);
    setDescription(description);
    setTriggerType(triggerType);
    setNotifyType(notifyType);
    setActive(active);
    setUnittype(unittype);
    setEvalPeriodMinutes(evalPeriodMinutes);
    setNotifyIntervalHours(notifyIntervalHours);
    setScript(script);
    setParent(parent);
    setToList(toList);
    setSyslogEvent(syslogEvent);
    setNoEventPrUnit(noEventsPrUnit);
    setNoEvents(noEvents);
    setNoUnits(noUnits);
  }

  /**
   * Optional method to run to validate that the Trigger object is ok (if you used the
   * short-constructor) - it will throw IllegalArgumentExceptions if something's missing.
   */
  public void validate() {
    setName(name);
    setDescription(description);
    setTriggerType(triggerType);
    setNotifyType(notifyType);
    setActive(active);
    setUnittype(unittype);
    setEvalPeriodMinutes(evalPeriodMinutes);
    setNotifyIntervalHours(notifyIntervalHours);
    setScript(script);
    setParent(parent);
    setToList(toList);
    setSyslogEvent(syslogEvent);
    setNoEventPrUnit(noEventsPrUnit);
    setNoEvents(noEvents);
    setNoUnits(noUnits);
  }

  public Integer getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public Unittype getUnittype() {
    return unittype;
  }

  @Override
  public String toString() {
    return "[" + id + "] [" + name + "]";
  }

  protected void setId(Integer id) {
    this.id = id;
  }

  protected String getOldName() {
    return oldName;
  }

  public void setName(String name) {
    if (name == null || "".equals(name.trim())) {
      throw new IllegalArgumentException("Trigger name cannot be null or an empty string");
    }
    if (!name.equals(this.name)) {
      this.oldName = this.name;
    }
    this.name = name;
  }

  protected void setOldName(String oldName) {
    this.oldName = oldName;
  }

  public String getDescription() {
    return description;
  }

  public SyslogEvent getSyslogEvent() {
    return syslogEvent;
  }

  public int getNotifyType() {
    return notifyType;
  }

  public String getNotifyTypeAsStr() {
    return notifyTypeMap.get(notifyType);
  }

  public String getToList() {
    return toList;
  }

  public int getEvalPeriodMinutes() {
    return evalPeriodMinutes;
  }

  public Integer getNoEvents() {
    return noEvents;
  }

  public Integer getNoEventsPrUnit() {
    return noEventsPrUnit;
  }

  public Integer getNoUnits() {
    return noUnits;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setSyslogEvent(SyslogEvent syslogEvent) {
    if (syslogEvent == null) {
      if (this.triggerType == TRIGGER_TYPE_BASIC) {
        throw new IllegalArgumentException(
            "Trigger syslogEvent cannot be NULL if trigger type is BASIC");
      }
      this.syslogEvent = null;
    } else {
      if (this.triggerType == TRIGGER_TYPE_COMPOSITE) {
        throw new IllegalArgumentException(
            "Trigger syslogEvent must be NULL if trigger type is COMPOSITE");
      }
      if (this.syslogEvent != null
          && this.syslogEvent.getId().intValue() != syslogEvent.getId().intValue()) {
        oldSyslogEvent = this.syslogEvent;
      } // we do this to remember that we have changed syslog event
      this.syslogEvent = syslogEvent;
    }
  }

  public boolean isSyslogEventChanged() {
    return oldSyslogEvent != null
        && this.syslogEvent.getId().intValue() != oldSyslogEvent.getId().intValue();
  }

  public void setSyslogEventChangeCompleted() {
    oldSyslogEvent = this.syslogEvent;
  }

  public void setNotifyType(int notifyType) {
    if (notifyType < NOTIFY_TYPE_ALARM && notifyType > NOTIFY_TYPE_SILENT) {
      throw new IllegalArgumentException("Trigger notifyType cannot be " + notifyType);
    }
    this.notifyType = notifyType;
  }

  public void setToList(String toList) {
    if (toList != null
        && (this.notifyType == NOTIFY_TYPE_ALARM || this.notifyType == NOTIFY_TYPE_REPORT)) {
      String[] emailAddresses = toList.split(",");
      for (String emailAddress : emailAddresses) {
        // Shortest valid mail address: a@b.co
        if (emailAddress.indexOf('@') < 1
            || emailAddress.lastIndexOf('.') < emailAddress.indexOf('@') + 2
            || emailAddress.length() < 6) {
          throw new IllegalArgumentException(
              "Trigger email address " + emailAddress + " is not valid");
        }
      }
    }
    this.toList = toList;
  }

  public void setEvalPeriodMinutes(int evalPeriodMinutes) {
    if (evalPeriodMinutes < EVAL_PERIOD_MIN || evalPeriodMinutes > EVAL_PERIOD_MAX) {
      throw new IllegalArgumentException(
          "Trigger evalPeriodMinutes cannot be lower than "
              + EVAL_PERIOD_MIN
              + " or higher than "
              + EVAL_PERIOD_MAX);
    }
    // if notifyInterval is 0 it means the value is not set yet - the check will then be performed
    // on setNotifyIntervalHours()
    // if notifyInterval is 2 or higher it's ok for all allowed evalPeriodMinutes
    if (notifyIntervalHours != null && notifyIntervalHours == 1 && evalPeriodMinutes > 60) {
      throw new IllegalArgumentException(
          "Trigger evalPeriodMinutes cannot be greater than 60(minutes), since notifyInterval is 1(hour).");
    }
    this.evalPeriodMinutes = evalPeriodMinutes;
  }

  public void setNoEvents(Integer noEvents) {
    if (noEvents == null) {
      if (this.triggerType == TRIGGER_TYPE_BASIC) {
        throw new IllegalArgumentException(
            "Trigger noEventsPrUnit cannot be NULL if trigger type is BASIC");
      }
      this.noEvents = null;
    } else {
      if (this.triggerType == TRIGGER_TYPE_COMPOSITE) {
        throw new IllegalArgumentException(
            "Trigger noEventsPrUnit must be NULL if trigger type is COMPOSITE");
      }
      if (noEvents < 1) {
        throw new IllegalArgumentException("Trigger noEvents cannot be lower than 1");
      }
      this.noEvents = noEvents;
    }
  }

  public void setNoEventPrUnit(Integer noEventsPrUnit) {
    if (noEventsPrUnit == null) {
      if (this.triggerType == TRIGGER_TYPE_BASIC) {
        throw new IllegalArgumentException(
            "Trigger noEventsPrUnit cannot be NULL if trigger type is BASIC");
      }
      this.noEventsPrUnit = null;
    } else {
      if (this.triggerType == TRIGGER_TYPE_COMPOSITE) {
        throw new IllegalArgumentException(
            "Trigger noEventsPrUnit must be NULL if trigger type is COMPOSITE");
      }
      if (noEventsPrUnit < 1) {
        throw new IllegalArgumentException("Trigger noEventsPrUnit cannot be lower than 1");
      }
      this.noEventsPrUnit = noEventsPrUnit;
    }
  }

  public void setNoUnits(Integer noUnits) {
    if (noUnits == null) {
      if (this.triggerType == TRIGGER_TYPE_BASIC) {
        throw new IllegalArgumentException(
            "Trigger noEventsPrUnit cannot be NULL if trigger type is BASIC");
      }
      this.noUnits = null;
    } else {
      if (this.triggerType == TRIGGER_TYPE_COMPOSITE) {
        throw new IllegalArgumentException(
            "Trigger noEventsPrUnit must be NULL if trigger type is COMPOSITE");
      }
      if (noUnits < 1) {
        throw new IllegalArgumentException("Trigger noUnits cannot be lower than 1");
      }
      this.noUnits = noUnits;
    }
  }

  public void setUnittype(Unittype unittype) {
    if (unittype == null) {
      throw new IllegalArgumentException("Trigger Unit Type cannot be null");
    }
    this.unittype = unittype;
  }

  public Integer getNotifyIntervalHours() {
    return notifyIntervalHours;
  }

  public void setNotifyIntervalHours(Integer notifyIntervalHours) {
    if (this.notifyType == NOTIFY_TYPE_ALARM || this.notifyType == NOTIFY_TYPE_REPORT) {
      if (notifyIntervalHours == null) {
        throw new IllegalArgumentException(
            "Trigger notifyIntervalHours must be set if notifyType is ALARM or REPORT");
      } else {
        if (notifyIntervalHours < NOTIFY_INTERVAL_MIN
            || notifyIntervalHours > NOTIFY_INTERVAL_MAX) {
          throw new IllegalArgumentException(
              "Trigger notifyIntervalHours cannot be lower than "
                  + NOTIFY_INTERVAL_MIN
                  + " or higher than "
                  + NOTIFY_INTERVAL_MAX
                  + "(=week)");
        }
        if (notifyIntervalHours == 1 && evalPeriodMinutes > NOTIFY_INTERVAL_MIN * 60) {
          throw new IllegalArgumentException(
              "Trigger notifyIntervalHours cannot be 1(hour), since evalPeriodMinutes is greater than 60(minutes)");
        }
      }
    }
    this.notifyIntervalHours = notifyIntervalHours;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public Trigger getParent() {
    return parent;
  }

  public void setParent(Trigger newParent) {
    if (this.parent != null) {
      parent.removeChild(this);
    }
    if (newParent == null) {
      this.parent = newParent;
      return;
    }
    if (newParent.getTriggerType() == TRIGGER_TYPE_BASIC) {
      throw new IllegalArgumentException("Parent trigger must be a COMPOSITE trigger");
    }
    Trigger tmpParent = newParent;
    while (tmpParent != null) {
      if (tmpParent.getId().equals(getId())) {
        throw new IllegalArgumentException(
            "Parent reference loop occurred for "
                + tmpParent.getName()
                + " ("
                + tmpParent.getId()
                + ")");
      }
      tmpParent = tmpParent.getParent();
    }
    this.parent = newParent;
    this.parent.addChild(this);
  }

  public int getTriggerType() {
    return triggerType;
  }

  public String getTriggerTypeStr() {
    return triggerTypeMap.get(triggerType);
  }

  public void setTriggerType(int triggerType) {
    this.triggerType = triggerType;
  }

  public File getScript() {
    return script;
  }

  public void setScript(File script) {
    if (script != null && script.getType() != FileType.SHELL_SCRIPT) {
      throw new IllegalArgumentException(
          "The script-file must be of SHELL_SCRIPT type, that is, a Fusion Shell Script file");
    }
    this.script = script;
  }

  protected void addChild(Trigger child) {
    if (children == null) {
      children = new ArrayList<>();
    }
    if (!this.children.contains(child)) {
      this.children.add(child);
    }
  }

  public List<Trigger> getAllChildren() {
    return getAllChildrenRec(this);
  }

  private List<Trigger> getAllChildrenRec(Trigger g) {
    List<Trigger> groups = new ArrayList<>();
    for (Trigger childrenGroup : g.getChildren()) {
      groups.add(childrenGroup);
      groups.addAll(getAllChildrenRec(childrenGroup));
    }
    return groups;
  }

  public List<Trigger> getChildren() {
    if (children == null) {
      children = new ArrayList<>();
    }
    return children;
  }

  protected void removeChild(Trigger child) {
    if (children != null) {
      children.remove(child);
    }
  }

  public Trigger getTopParent() {
    Trigger tmp = this;
    while (tmp.getParent() != null) {
      tmp = tmp.getParent();
    }
    return tmp;
  }

  /**
   * Used by Core-TriggerDaemon to determine when to store TriggerUnits file Used by
   * Core-ScriptDaemon to determine when to delete TriggerUnits file.
   *
   * @return
   */
  public boolean hasAnyParentScript() {
    Trigger tmp = getParent();
    while (tmp != null) {
      if (tmp.getScript() != null) {
        return true;
      } else {
        tmp = tmp.getParent();
      }
    }
    return false;
  }
}
