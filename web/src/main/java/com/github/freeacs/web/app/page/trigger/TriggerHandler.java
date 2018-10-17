package com.github.freeacs.web.app.page.trigger;

import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.File;
import com.github.freeacs.dbi.FileType;
import com.github.freeacs.dbi.SyslogEvent;
import com.github.freeacs.dbi.SyslogEvents;
import com.github.freeacs.dbi.Trigger;
import com.github.freeacs.dbi.Triggers;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.web.app.input.DropDownSingleSelect;
import com.github.freeacs.web.app.input.Input;
import com.github.freeacs.web.app.input.InputSelectionFactory;
import com.github.freeacs.web.app.table.TableElement;
import com.github.freeacs.web.app.table.TableElementMaker;
import com.github.freeacs.web.app.util.ACSLoader;
import com.github.freeacs.web.app.util.SessionCache;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.sql.DataSource;

public class TriggerHandler {
  private ACS acs;
  private Unittype unittype;
  private String sessionId;
  private TriggerData inputData;

  public TriggerHandler(
      String sessionId,
      TriggerData inputData,
      DataSource xapsDataSource,
      DataSource syslogDataSource)
      throws SQLException {
    this.acs = ACSLoader.getXAPS(sessionId, xapsDataSource, syslogDataSource);
    this.sessionId = sessionId;
    this.inputData = inputData;
    setUnittype(inputData);
  }

  public Unittype getUnittype() {
    return unittype;
  }

  public void deleteTrigger(TriggerData inputData) throws SQLException {
    setUnittype(inputData);
    Triggers triggers = unittype.getTriggers();
    Trigger trigger = triggers.getById(inputData.getTriggerId().getInteger());
    triggers.deleteTrigger(trigger, acs);
  }

  public boolean createTrigger(TriggerData inputData) {
    setUnittype(inputData);
    return saveTrigger(new Trigger(0, 0));
  }

  public boolean editTrigger(TriggerData inputData) {
    setUnittype(inputData);
    Trigger trigger = unittype.getTriggers().getById(inputData.getTriggerId().getInteger());
    return saveTrigger(trigger);
  }

  public void setUnittype(TriggerData inputData) {
    if (inputData.getUnittype().getString() != null && acs != null) {
      unittype = acs.getUnittype(inputData.getUnittype().getString());
      resetUnitTypeInSessionCacheIfNecessary();
    }
  }

  private boolean saveTrigger(Trigger trigger) {
    boolean isTriggerSaved = true;
    fillTrigger(trigger, inputData);
    try {
      unittype.getTriggers().addOrChangeTrigger(trigger, acs);
    } catch (SQLException exception) {
      isTriggerSaved = false;
      exception.printStackTrace();
    }
    return isTriggerSaved;
  }

  private void fillTrigger(Trigger trigger, TriggerData inputData) {
    if (inputData != null) {
      trigger.setUnittype(this.unittype);
      trigger.setActive(inputData.getActive().hasValue("true"));
      trigger.setTriggerType(nullsafeInteger(inputData.getTriggerType()));
      trigger.setName(nullsafeString(inputData.getName()));
      trigger.setDescription(nullsafeString(inputData.getDescription()));
      trigger.setEvalPeriodMinutes(nullsafeInteger(inputData.getEvalPeriodMinutes()));
      trigger.setNotifyType(nullsafeInteger(inputData.getNotifyType()));
      Integer syslogEventId = nullsafeInteger(inputData.getSyslogEventId());
      if (syslogEventId != null) {
        trigger.setSyslogEvent(SyslogEvents.getById(syslogEventId));
      }
      trigger.setToList(nullsafeString(inputData.getToList()));
      trigger.setNoEvents(nullsafeInteger(inputData.getNumberTotal()));
      trigger.setNoEventPrUnit(nullsafeInteger(inputData.getNumberPerUnit()));
      trigger.setNoUnits(nullsafeInteger(inputData.getNumberOfUnits()));
      trigger.setNotifyIntervalHours(nullsafeInteger(inputData.getNotifyIntervalHours()));
      //			trigger.setGroup(xaps.getGroup(nullsafeInteger(inputData.getGroupId())));
      trigger.setParent(
          unittype.getTriggers().getById(nullsafeInteger(inputData.getParentTrigger())));
      trigger.setScript(unittype.getFiles().getById(nullsafeInteger(inputData.getScriptFileId())));
    }
  }

  public Trigger getTrigger(int triggerId) {
    return unittype.getTriggers().getById(inputData.getTriggerId().getInteger());
  }

  public List<Trigger> getTriggers() {
    return Arrays.asList(unittype.getTriggers().getTriggers());
  }

  public DropDownSingleSelect<SyslogEvent> getSyslogEventDropdown(SyslogEvent selectedSyslogEvent) {
    if (getUnittype() == null) {
      return null;
    }
    List<SyslogEvent> syslogEventList =
        Arrays.asList(getUnittype().getSyslogEvents().getSyslogEvents());
    return InputSelectionFactory.getDropDownSingleSelect(
        inputData.getSyslogEventId(), selectedSyslogEvent, syslogEventList);
  }

  public DropDownSingleSelect<File> getScriptFileDropdown(File selectedFile) {
    if (getUnittype() == null) {
      return null;
    }
    List<File> fileList = Arrays.asList(getUnittype().getFiles().getFiles(FileType.SHELL_SCRIPT));
    return InputSelectionFactory.getDropDownSingleSelect(
        inputData.getScriptFileId(), selectedFile, fileList);
  }

  public List<TableElement> getTriggerTableElements() {
    try {
      return new TableElementMaker().getTriggers(unittype);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public DropDownSingleSelect<Trigger> getTriggerParentDropdown(Trigger trigger) {
    if (getUnittype() == null) {
      return null;
    }
    List<Trigger> triggerList = createFilteredList(trigger);
    Trigger parentTrigger = trigger == null ? null : trigger.getParent();
    return InputSelectionFactory.getDropDownSingleSelect(
        inputData.getParentTrigger(), parentTrigger, triggerList);
  }

  private List<Trigger> createFilteredList(Trigger selectedTrigger) {
    ArrayList<Trigger> allTriggersList =
        new ArrayList<>(Arrays.asList(getUnittype().getTriggers().getTriggers()));
    allTriggersList.removeIf(trigger -> trigger.getTriggerType() == Trigger.TRIGGER_TYPE_BASIC);
    if (selectedTrigger == null) {
      return allTriggersList;
    }
    allTriggersList.remove(selectedTrigger);
    allTriggersList.removeAll(selectedTrigger.getAllChildren());
    return allTriggersList;
  }

  public DropDownSingleSelect<NotifyType> getNotifyTypeDropdown(Integer selectedActionType) {
    List<NotifyType> triggerNotifyTypes = new ArrayList<>();
    triggerNotifyTypes.add(new NotifyType(0, "ALARM"));
    triggerNotifyTypes.add(new NotifyType(1, "REPORT"));
    triggerNotifyTypes.add(new NotifyType(2, "SILENT"));
    NotifyType selectedNotify =
        selectedActionType == null
            ? triggerNotifyTypes.get(0)
            : triggerNotifyTypes.get(selectedActionType);
    return InputSelectionFactory.getDropDownSingleSelect(
        inputData.getNotifyType(), selectedNotify, triggerNotifyTypes);
  }

  public DropDownSingleSelect<TriggerType> getTriggertypeDropdown(Integer selectedTriggertypeId) {
    List<TriggerType> triggerTypes = new ArrayList<>();
    triggerTypes.add(new TriggerType(0, "BASIC"));
    triggerTypes.add(new TriggerType(1, "COMPOSITE"));
    TriggerType selectedTriggerType =
        selectedTriggertypeId == null
            ? triggerTypes.get(0)
            : triggerTypes.get(selectedTriggertypeId);
    return InputSelectionFactory.getDropDownSingleSelect(
        inputData.getTriggerType(), selectedTriggerType, triggerTypes);
  }

  public DropDownSingleSelect<String> getNewEvalPeriodDropdown(Integer selectedEvalPeriod) {
    List<String> periods = new ArrayList<>();
    for (int i = 15; i <= 120; i += 15) {
      periods.add(String.valueOf(i));
    }
    int selectedIndex =
        selectedEvalPeriod == null ? 0 : periods.indexOf(String.valueOf(selectedEvalPeriod));
    return InputSelectionFactory.getDropDownSingleSelect(
        inputData.getEvalPeriodMinutes(), periods.get(selectedIndex), periods);
  }

  public DropDownSingleSelect<NotifyIntervalHour> getNotifyIntervalHoursDropdown(
      Integer selectedNotifyIntervalHourId) {
    List<NotifyIntervalHour> intervals = new ArrayList<>();
    intervals.add(new NotifyIntervalHour(1, "one hour"));
    intervals.add(new NotifyIntervalHour(2, "two hours"));
    intervals.add(new NotifyIntervalHour(4, "four hours"));
    intervals.add(new NotifyIntervalHour(6, "six hours"));
    intervals.add(new NotifyIntervalHour(12, "twelve hours"));
    intervals.add(new NotifyIntervalHour(24, "one day"));
    intervals.add(new NotifyIntervalHour(48, "two days"));
    intervals.add(new NotifyIntervalHour(72, "three days"));
    intervals.add(new NotifyIntervalHour(96, "four days"));
    intervals.add(new NotifyIntervalHour(120, "five days"));
    intervals.add(new NotifyIntervalHour(144, "six days"));
    intervals.add(new NotifyIntervalHour(168, "one week"));
    NotifyIntervalHour selectedHour =
        getSelectedNotifyIntervalHour(intervals, selectedNotifyIntervalHourId);
    return InputSelectionFactory.getDropDownSingleSelect(
        inputData.getNotifyIntervalHours(), selectedHour, intervals);
  }

  private NotifyIntervalHour getSelectedNotifyIntervalHour(
      List<NotifyIntervalHour> intervals, Integer selectedNotifyIntervalHourId) {
    if (selectedNotifyIntervalHourId != null) {
      for (NotifyIntervalHour element : intervals) {
        if (element.getId().intValue() == selectedNotifyIntervalHourId) {
          return element;
        }
      }
    }
    return intervals.get(0);
  }

  private void resetUnitTypeInSessionCacheIfNecessary() {
    if (unittype == null) {
      SessionCache.getSessionData(sessionId).setUnittypeName(null);
    }
  }

  private Integer nullsafeInteger(Input input) {
    return input == null ? null : input.getInteger();
  }

  private String nullsafeString(Input input) {
    return input == null ? null : input.getString();
  }
}
