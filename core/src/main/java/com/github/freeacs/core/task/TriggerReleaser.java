package com.github.freeacs.core.task;

import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.DBI;
import com.github.freeacs.dbi.File;
import com.github.freeacs.dbi.FileType;
import com.github.freeacs.dbi.Files;
import com.github.freeacs.dbi.ScriptExecutions;
import com.github.freeacs.dbi.SyslogConstants;
import com.github.freeacs.dbi.Trigger;
import com.github.freeacs.dbi.TriggerComparator;
import com.github.freeacs.dbi.TriggerRelease;
import com.github.freeacs.dbi.Triggers;
import com.github.freeacs.dbi.Unittype;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TriggerReleaser extends DBIShare {
  public TriggerReleaser(String taskName, DBI dbi) {
    super(taskName, dbi);
  }

  private static Logger logger = LoggerFactory.getLogger(TriggerReleaser.class);
  private ACS acs;
  private static SimpleDateFormat tmsFormat = new SimpleDateFormat("HHmmss");
  private static long MS_MINUTE = 60 * 1000;
  private static long MS_HOUR = 60 * MS_MINUTE;

  @Override
  public void runImpl() throws Exception {
    acs = getLatestACS();
    processAllTriggers();
  }

  @Override
  public Logger getLogger() {
    return logger;
  }

  private void executeTriggerScript(
      Trigger trigger, Map<String, Integer> unitEventsMap, Integer triggerReleaseId)
      throws SQLException {
    ScriptExecutions executions = new ScriptExecutions(getDataSource());
    executions.requestExecution(
        trigger.getScript(),
        "\"-uut:" + trigger.getUnittype().getName() + "\" -v" + makeTriggerUnitsFilename(trigger),
        "TRIGGER:" + triggerReleaseId);
  }

  private Map<String, Integer> getUnitEventsMapFromChilden(Trigger trigger) throws SQLException {
    Map<String, Integer> unitEventsMap = new HashMap<>();
    for (Trigger child : trigger.getAllChildren()) {
      if (child.getTriggerType() == Trigger.TRIGGER_TYPE_BASIC) {
        File triggerUnitsFile =
            child.getUnittype().getFiles().getByName(makeTriggerUnitsFilename(child));
        String content = new String(triggerUnitsFile.getContent());
        String[] contentLines = content.split("\n");
        for (String line : contentLines) {
          String[] lineArgs = line.split(" ");
          Integer count = unitEventsMap.get(lineArgs[0]);
          if (count != null) {
            unitEventsMap.put(lineArgs[0], count + Integer.valueOf(lineArgs[1]));
          } else {
            unitEventsMap.put(lineArgs[0], Integer.valueOf(lineArgs[1]));
          }
        }
      }
    }
    return unitEventsMap;
  }

  private String makeTriggerUnitsFilename(Trigger trigger) {
    return "TU-" + trigger.getId();
  }

  private void processAllTriggers() throws SQLException, IOException, ParseException {
    logger.info("TriggerReleaser: Trigger processing starts...");
    // The now/process-timestamp is set to the beginning of this minute (which should be 30 seconds
    // ago since the
    // TriggerTask runs 30 seconds into every minute). All syslog-write operations initiated before
    // 30 seconds
    // ago should be completed and the data set for the trigger releases should be complete and
    // unchanged for all
    // future. Thus the alarms/reports from this system should be accurate.
    Calendar c = Calendar.getInstance();
    c.set(Calendar.SECOND, 0);
    c.set(Calendar.MILLISECOND, 0);
    Date now = c.getTime();
    boolean deleteOldEvents = true;
    for (Unittype unittype : acs.getUnittypes().getUnittypes()) {
      Triggers triggers = unittype.getTriggers();
      if (deleteOldEvents) { // execute this only once, will delete for all unittypes...
        Date startOfMaxEvaluationPeriod =
            new Date(now.getTime() - Trigger.EVAL_PERIOD_MAX * MS_MINUTE);
        int rowsDeleted = triggers.deleteEvents(startOfMaxEvaluationPeriod, acs);
        if (rowsDeleted > 0) {
          logger.info(
              "TriggerReleaser: Deleted "
                  + rowsDeleted
                  + " Trigger Events which was older than evaluation max period ("
                  + Trigger.EVAL_PERIOD_MAX
                  + " min) (before "
                  + startOfMaxEvaluationPeriod
                  + ")");
        }
        deleteOldEvents = false;
      }
      processTriggersForUnittype(triggers, now);
    }
  }

  private void processTrigger(Date now, Triggers triggers, Trigger trigger, Date evaluationStart)
      throws SQLException {
    if (trigger.getTriggerType() == Trigger.TRIGGER_TYPE_BASIC) {
      Map<String, Integer> unitEventsMap =
          triggers.countEventsPrUnit(trigger.getId(), evaluationStart, now, acs);
      int ne =
          unitEventsMap.remove(
              "TEC-TotalEventsCounter"); // will always return an int and then remove it from map
      int nu = 0;
      for (Integer noEvents : unitEventsMap.values()) {
        if (noEvents >= trigger.getNoEventsPrUnit()) {
          nu++;
        }
      }
      if (ne >= trigger.getNoEvents() && nu >= trigger.getNoUnits()) { // trigger is released!
        Date firstEventTms = triggers.getFirstEventTms(trigger.getId(), evaluationStart, now, acs);
        logger.info(
            "TriggerReleaser: \t\tTrigger "
                + trigger.getName()
                + " was released since noEvents is "
                + ne
                + " (>= "
                + trigger.getNoEvents()
                + ") and noUnits is "
                + nu
                + " (>= "
                + trigger.getNoUnits()
                + ")");
        logger.info(
            "TriggerReleaser: \t\tTrigger "
                + trigger.getName()
                + " is evaluated in this timeframe: "
                + evaluationStart
                + " - "
                + now
                + ". First event was found at "
                + firstEventTms);
        TriggerRelease th =
            new TriggerRelease(
                trigger,
                ne,
                trigger.getNoEventsPrUnit(),
                nu,
                new Date(firstEventTms.getTime()),
                new Date(now.getTime()),
                null);
        triggers.addOrChangeHistory(th, acs);
        if (trigger.getScript() != null) {
          storeTriggerUnits(trigger, unitEventsMap, now);
          executeTriggerScript(trigger, unitEventsMap, th.getId());
        } else if (trigger.hasAnyParentScript()) {
          storeTriggerUnits(trigger, unitEventsMap, now);
        }
        int rowsDeleted = triggers.deleteEvents(trigger.getId(), now, acs);
        if (rowsDeleted > 0) {
          logger.debug(
              "TriggerReleaser: \t\tDeleted "
                  + rowsDeleted
                  + " Trigger Events which was older than processing timestamp (before "
                  + now
                  + "), to avoid trigger release based on the same Trigger Events");
        }
        DBI dbi = acs.getDbi();
        if (!sentWithinNotifyInterval(triggers, trigger, now)
            && (trigger.getNotifyType() == Trigger.NOTIFY_TYPE_ALARM
                || trigger.getNotifyType() == Trigger.NOTIFY_TYPE_REPORT)) {
          dbi.publishTriggerReleased(trigger, SyslogConstants.FACILITY_MONITOR);
        } else {
          logger.debug(
              "TriggerReleaser: \t\tFound a Trigger History which was notified, abort processing - no need to send ALARM more often than notify-interval");
        }
      } else {
        logger.debug(
            "TriggerReleaser: \t\tTrigger was not released since noEvents is "
                + ne
                + " (required: "
                + trigger.getNoEvents()
                + ") and noUnits is "
                + nu
                + " (required: "
                + trigger.getNoUnits()
                + ")");
      }
    } else { // Trigger.TRIGGER_TYPE_COMPOSITE
      boolean release = false;
      Date firstEventTms = now;
      for (Trigger child : trigger.getChildren()) {
        if (!child.isActive()) {
          continue;
        }
        Date evaluationPeriodStart =
            new Date(now.getTime() - trigger.getEvalPeriodMinutes() * MS_MINUTE);
        TriggerRelease th =
            triggers.readLatestTriggerRelease(trigger, evaluationPeriodStart, now, acs);
        if (th == null) {
          release = false;
          break;
        } else {
          release = true;
        }
        if (th.getFirstEventTms().before(firstEventTms)) {
          firstEventTms = th.getFirstEventTms();
        }
      }
      if (release) {
        logger.info(
            "TriggerReleaser: \t\tTrigger "
                + trigger.getName()
                + " was released since all child trigger are released");
        TriggerRelease th = new TriggerRelease(trigger, firstEventTms, now, null);
        triggers.addOrChangeHistory(th, acs);
        if (trigger.getScript() != null) {
          executeTriggerScript(trigger, getUnitEventsMapFromChilden(trigger), th.getId());
        }
        DBI dbi = acs.getDbi();
        if (!sentWithinNotifyInterval(triggers, trigger, now)
            && (trigger.getNotifyType() == Trigger.NOTIFY_TYPE_ALARM
                || trigger.getNotifyType() == Trigger.NOTIFY_TYPE_REPORT)) {
          dbi.publishTriggerReleased(trigger, SyslogConstants.FACILITY_MONITOR);
        }
      } else {
        logger.debug(
            "TriggerReleaser: \t\tTrigger was not released since not all child triggers were released");
      }
    }
  }

  private void processTriggersForUnittype(Triggers triggers, Date now) throws SQLException {
    Trigger[] triggerArr = triggers.getTriggers();
    // This sorting will make sure that all BASIC triggers are treated first, then higher level
    // COMPOSITE until
    // the highest level COMPOSITE will be treated last.
    Arrays.sort(triggerArr, new TriggerComparator(false));
    if (triggerArr.length > 0) {
      logger.info(
          "TriggerReleaser: Processing triggers in unittype "
              + triggerArr[0].getUnittype().getName());
    }
    for (Trigger trigger : triggerArr) {
      if (!trigger.isActive()) {
        logger.debug(
            "TriggerReleaser: \tTrigger "
                + trigger.getName()
                + " ("
                + trigger.getId()
                + ") is in-active, dismiss processing");
        continue;
      }
      logger.debug(
          "TriggerReleaser: \tTrigger "
              + trigger.getName()
              + " ("
              + trigger.getId()
              + "), type is "
              + trigger.getTriggerTypeStr()
              + ", notifyType is "
              + trigger.getNotifyTypeAsStr());
      Date evaluationStart = new Date(now.getTime() - trigger.getEvalPeriodMinutes() * MS_MINUTE);
      if (trigger.getTriggerType() == Trigger.TRIGGER_TYPE_BASIC) {
        // No trigger events to delete for a COMPOSITE trigger
        int rowsDeleted = triggers.deleteEvents(trigger.getId(), evaluationStart, acs);
        if (rowsDeleted > 0) {
          logger.debug(
              "TriggerReleaser: \t\tDeleted "
                  + rowsDeleted
                  + " Trigger Events which was older than evaluation period ("
                  + trigger.getEvalPeriodMinutes()
                  + " min) (before "
                  + evaluationStart
                  + ")");
        }
      }
      Date evaluationPeriodStart =
          new Date(now.getTime() - trigger.getEvalPeriodMinutes() * MS_MINUTE);
      TriggerRelease th =
          triggers.readLatestTriggerRelease(trigger, evaluationPeriodStart, now, acs);
      if (th != null) {
        logger.debug(
            "TriggerReleaser: \t\tFound a recent Trigger release within evaluation period, no trigger processing so quickly after last release");
        continue;
      } else {
        processTrigger(now, triggers, trigger, evaluationStart);
      }
    }
  }

  private boolean sentWithinNotifyInterval(Triggers triggers, Trigger trigger, Date now)
      throws SQLException {
    Date notifyIntervalStart = new Date(now.getTime() - trigger.getNotifyIntervalHours() * MS_HOUR);
    List<TriggerRelease> historyList =
        triggers.readTriggerReleases(trigger, notifyIntervalStart, now, acs, null);
    boolean sent = false;
    for (TriggerRelease history : historyList) {
      if (history.getSentTms() != null) {
        sent = true;
        break;
      }
    }
    return sent;
  }

  private void storeTriggerUnits(Trigger trigger, Map<String, Integer> unitEventsMap, Date now)
      throws SQLException {
    Files files = trigger.getUnittype().getFiles();
    String filename = makeTriggerUnitsFilename(trigger);
    String desc =
        "Units causing release of trigger " + trigger.getName() + " at " + tmsFormat.format(now);
    StringBuilder sb = new StringBuilder();
    for (Entry<String, Integer> entry : unitEventsMap.entrySet()) {
      sb.append(entry.getKey()).append(" ").append(entry.getValue()).append("\n");
    }
    byte[] triggerUnitsFileByteArr = sb.toString().getBytes();
    File f = files.getByName(filename);
    if (f == null) {
      f =
          new File(
              trigger.getUnittype(),
              filename,
              FileType.UNITS,
              desc,
              String.valueOf(trigger.getId()),
              now,
              null,
              acs.getUser());
    }
    f.setDescription(desc);
    f.setBytes(triggerUnitsFileByteArr);
    files.addOrChangeFile(f, acs);
  }
}
