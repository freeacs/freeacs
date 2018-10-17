package com.github.freeacs.dbi;

import com.github.freeacs.dbi.InsertOrUpdateStatement.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Triggers {
  private static Logger logger = LoggerFactory.getLogger(Triggers.class);
  private Map<String, Trigger> nameMap;
  private Map<Integer, Trigger> idMap;
  private Unittype unittype;

  public Triggers(Map<Integer, Trigger> idMap, Map<String, Trigger> nameMap, Unittype unittype) {
    this.idMap = idMap;
    this.nameMap = nameMap;
    this.unittype = unittype;
  }

  public Unittype getUnittype() {
    return unittype;
  }

  public Trigger getById(Integer id) {
    return idMap.get(id);
  }

  public Trigger getByName(String name) {
    return nameMap.get(name);
  }

  public Trigger[] getTriggers() {
    return nameMap.values().toArray(new Trigger[] {});
  }

  @Override
  public String toString() {
    return "Contains " + nameMap.size() + " triggers";
  }

  /**
   * Add or change history in the trigger_release table.
   *
   * @param history
   * @param acs
   * @throws SQLException
   */
  public void addOrChangeHistory(TriggerRelease history, ACS acs) throws SQLException {
    PreparedStatement ps = null;
    Connection c = acs.getDataSource().getConnection();
    try {
      if (history.getId() == null) {
        DynamicStatement ds = new DynamicStatement();
        ds.addSqlAndArguments("trigger_id, ", history.getTrigger().getId());
        ds.addSqlAndIntegerArgs("no_events, ", history.getNoEvents());
        ds.addSqlAndIntegerArgs("no_events_pr_unit, ", history.getNoEventsPrUnit());
        ds.addSqlAndIntegerArgs("no_units, ", history.getNoUnits());
        ds.addSqlAndArguments("first_event_timestamp, ", history.getFirstEventTms());
        ds.addSqlAndArguments("release_timestamp", history.getReleaseTms());
        // Do not specify sent_timestamp, this is only done in UPDATE (from Monitor-server)
        ds.setSql(
            "INSERT INTO trigger_release ("
                + ds.getSql()
                + ") VALUES ("
                + ds.getQuestionMarks()
                + ")");
        ps = ds.makePreparedStatement(c, "id");
        ps.setQueryTimeout(60);
        ps.executeUpdate();
        ResultSet gk = ps.getGeneratedKeys();
        if (gk.next()) {
          history.setId(gk.getInt(1));
        }
      } else {
        // Can only update sent_timestamp - the other fields should not be touched
        DynamicStatement ds = new DynamicStatement();
        ds.addSqlAndArguments(
            "UPDATE trigger_release SET sent_timestamp = ? WHERE id = ?",
            history.getSentTms(),
            history.getId());
        ps = ds.makePreparedStatement(c);
        ps.setQueryTimeout(60);
        ps.executeUpdate();
      }
    } finally {
      if (ps != null) {
        ps.close();
      }
      c.close();
    }
  }

  public TriggerRelease readLatestTriggerRelease(Trigger trigger, Date from, Date to, ACS acs)
      throws SQLException {
    List<TriggerRelease> history = readTriggerReleases(trigger, from, to, acs, 1);
    if (!history.isEmpty()) {
      return history.get(0);
    }
    return null;
  }

  public List<TriggerRelease> readTriggerReleases(
      Trigger trigger, Date from, Date to, ACS acs, Integer limit) throws SQLException {
    PreparedStatement ps = null;
    ResultSet rs = null;
    Connection c = acs.getDataSource().getConnection();
    try {
      List<TriggerRelease> thList = new ArrayList<>();
      DynamicStatement ds = new DynamicStatement();
      ds.addSql(
          "SELECT tr.id, tr.trigger_id, tr.no_events, tr.no_events_pr_unit, tr.no_units, tr.first_event_timestamp, tr.release_timestamp, tr.sent_timestamp ");
      ds.addSqlAndArguments(
          "FROM trigger_release tr, trigger_ t WHERE tr.release_timestamp >= ? AND tr.release_timestamp < ? ",
          from,
          to);
      if (trigger != null) {
        ds.addSqlAndArguments("AND tr.trigger_id = ? ", trigger.getId());
      }
      ds.addSqlAndArguments("AND tr.trigger_id = t.id AND t.unit_type_id = ? ", unittype.getId());
      ds.addSql("ORDER BY tr.release_timestamp DESC");
      if (limit != null) {
        ds.addSql(" LIMIT " + limit);
      }
      ps = ds.makePreparedStatement(c);
      ps.setQueryTimeout(60);
      rs = ps.executeQuery();
      while (rs.next()) {
        Integer id = rs.getInt("id");
        Integer triggerId = rs.getInt("trigger_id");
        Integer noEvents = rs.getInt("no_events");
        Integer noEventsPrUnit = rs.getInt("no_events_pr_unit");
        Integer noUnits = rs.getInt("no_units");
        Date firstEventTms = rs.getTimestamp("first_event_timestamp");
        Date releaseTms = rs.getTimestamp("release_timestamp");
        Date sentTms = rs.getTimestamp("sent_timestamp");
        TriggerRelease th =
            new TriggerRelease(
                getById(triggerId),
                noEvents,
                noEventsPrUnit,
                noUnits,
                firstEventTms,
                releaseTms,
                sentTms);
        th.setId(id);
        thList.add(th);
      }
      return thList;
    } finally {
      if (rs != null) {
        rs.close();
      }
      if (ps != null) {
        ps.close();
      }
      c.close();
    }
  }

  /**
   * Add TriggerEvent to trigger_event table. This execution is not logged (like
   * addOrChangeTrigger()) because this is a method supposed to be more efficient and cost less for
   * the system. It will be run by the Syslog- and TR-069 server. Not necessary with any permission
   * checks.
   *
   * @param event
   * @param acs
   * @throws SQLException
   */
  public void addEvent(TriggerEvent event, ACS acs) throws SQLException {
    PreparedStatement ps = null;
    Connection c = acs.getDataSource().getConnection();
    try {
      DynamicStatement ds = new DynamicStatement();
      ds.addSqlAndArguments("trigger_id, ", event.getTrigger().getId());
      ds.addSqlAndArguments("timestamp_, ", event.getTms());
      ds.addSqlAndArguments("unit_id", event.getUnitId());
      ds.setSql(
          "INSERT INTO trigger_event (" + ds.getSql() + ") VALUES (" + ds.getQuestionMarks() + ")");
      ps = ds.makePreparedStatement(c);
      ps.setQueryTimeout(60);
      ps.executeUpdate();
    } catch (SQLException sqle) {
      // Ignore SQLExceptions, the likely source of such an exception is
      // if shell/web has deleted the trigger involved within the past 1000 ms,
      // and the cache in the syslog server (running this method) has not
      // been updated.
    } finally {
      if (ps != null) {
        ps.close();
      }
      c.close();
    }
  }

  public int deleteEvents(Date upUntil, ACS acs) throws SQLException {
    return deleteEvents(null, upUntil, acs);
  }

  /**
   * Delete old triggerEvents. Run by Core server, to make a smaller table to work on.
   *
   * @param triggerId
   * @param upUntil
   * @param acs
   * @return
   * @throws SQLException
   */
  public int deleteEvents(Integer triggerId, Date upUntil, ACS acs) throws SQLException {
    PreparedStatement ps = null;
    Connection c = acs.getDataSource().getConnection();
    try {
      DynamicStatement ds = new DynamicStatement();
      if (triggerId != null) {
        ds.addSqlAndArguments(
            "DELETE FROM trigger_event WHERE trigger_id = ? and timestamp_ < ?",
            triggerId,
            upUntil);
      } else {
        ds.addSqlAndArguments("DELETE FROM trigger_event WHERE timestamp_ < ?", upUntil);
      }
      ps = ds.makePreparedStatement(c);
      ps.setQueryTimeout(60);
      return ps.executeUpdate();
    } finally {
      if (ps != null) {
        ps.close();
      }
      c.close();
    }
  }

  /**
   * Count number of events for each unitid for a trigger within a specified time frame. This result
   * can also return the total number of event for a trigger within a specified time frame.
   *
   * @param triggerId
   * @param from
   * @param to
   * @param acs
   * @return
   * @throws SQLException
   */
  public Map<String, Integer> countEventsPrUnit(Integer triggerId, Date from, Date to, ACS acs)
      throws SQLException {
    ResultSet rs;
    PreparedStatement ps = null;
    Connection c = acs.getDataSource().getConnection();
    Map<String, Integer> unitMap = new HashMap<>();
    try {
      DynamicStatement ds = new DynamicStatement();
      ds.addSqlAndArguments(
          "SELECT unit_id, COUNT(*) FROM trigger_event WHERE trigger_id = ? AND timestamp_ >= ? AND timestamp_ < ? GROUP BY unit_id",
          triggerId,
          from,
          to);
      ps = ds.makePreparedStatement(c);
      ps.setQueryTimeout(60);
      rs = ps.executeQuery();
      int totalCounter = 0;
      while (rs.next()) {
        String unitId = rs.getString("unit_id");
        Integer count = rs.getInt(2);
        totalCounter += count;
        unitMap.put(unitId, count);
      }
      unitMap.put("TEC-TotalEventsCounter", totalCounter);
      return unitMap;
    } finally {
      if (ps != null) {
        ps.close();
      }
      c.close();
    }
  }

  public Date getFirstEventTms(Integer triggerId, Date from, Date to, ACS acs) throws SQLException {
    ResultSet rs = null;
    PreparedStatement ps = null;
    Connection c = acs.getDataSource().getConnection();
    try {
      DynamicStatement ds = new DynamicStatement();
      ds.addSqlAndArguments(
          "SELECT timestamp_ FROM trigger_event WHERE trigger_id = ? AND timestamp_ >= ? AND timestamp_ < ? ORDER BY timestamp_ ASC LIMIT 1",
          triggerId,
          from,
          to);
      ps = ds.makePreparedStatement(c);
      ps.setQueryTimeout(60);
      rs = ps.executeQuery();
      if (rs.next()) {
        return rs.getTimestamp("timestamp_");
      } else {
        return null;
      }
    } finally {
      if (ps != null) {
        ps.close();
      }
      c.close();
    }
  }

  public void addOrChangeTrigger(Trigger trigger, ACS acs) throws SQLException {
    if (!acs.getUser().isUnittypeAdmin(unittype.getId())) {
      throw new IllegalArgumentException("Not allowed action for this user");
    }
    trigger.validate();
    addOrChangeTriggerImpl(trigger, acs);
    nameMap.put(trigger.getName(), trigger);
    idMap.put(trigger.getId(), trigger);
    if (trigger.getOldName() != null) {
      nameMap.remove(trigger.getOldName());
      trigger.setOldName(null);
    }
  }

  private int deleteTriggerImpl(Trigger trigger, ACS acs) throws SQLException {
    PreparedStatement ps = null;
    boolean wasAutoCommit;
    Connection c = acs.getDataSource().getConnection();
    wasAutoCommit = c.getAutoCommit();
    c.setAutoCommit(false);
    try {
      DynamicStatement ds = new DynamicStatement();
      ds.addSqlAndArguments("DELETE FROM trigger_event WHERE trigger_id = ?", trigger.getId());
      ps = ds.makePreparedStatement(c);
      ps.setQueryTimeout(60);
      ps.executeUpdate();
      ds = new DynamicStatement();
      ds.addSqlAndArguments("DELETE FROM trigger_release WHERE trigger_id = ?", trigger.getId());
      ps = ds.makePreparedStatement(c);
      ps.setQueryTimeout(60);
      ps.executeUpdate();
      ds = new DynamicStatement();
      ds.addSqlAndArguments("DELETE FROM trigger_ WHERE id = ?", trigger.getId());
      ps = ds.makePreparedStatement(c);
      ps.setQueryTimeout(60);
      int rowsDeleted = ps.executeUpdate();
      c.commit();
      c.setAutoCommit(true);
      if (acs.getDbi() != null) {
        acs.getDbi().publishDelete(trigger, trigger.getUnittype());
      }
      logger.info("Deleted trigger " + trigger.getName());
      return rowsDeleted;
    } finally {
      if (ps != null) {
        ps.close();
      }
      c.setAutoCommit(wasAutoCommit);
      c.close();
    }
  }

  /**
   * The first time this method is run, the flag is set. The second time this method is run, the
   * parameter is removed from the name- and id-Map.
   *
   * @throws java.sql.SQLException
   */
  public void deleteTrigger(Trigger trigger, ACS acs) throws SQLException {
    if (!trigger.getChildren().isEmpty()) {
      throw new IllegalArgumentException(
          "This trigger is a composite trigger with \"child\" triggers. Remove child triggers before deleting this trigger");
    }
    if (!acs.getUser().isUnittypeAdmin(unittype.getId())) {
      throw new IllegalArgumentException("Not allowed action for this user");
    }
    int rowsDeleted = deleteTriggerImpl(trigger, acs);
    nameMap.remove(trigger.getName());
    idMap.remove(trigger.getId());
    if (trigger.getParent() != null) {
      trigger.getParent().removeChild(trigger);
    }
  }

  private void addOrChangeTriggerImpl(Trigger trigger, ACS acs) throws SQLException {
    PreparedStatement ps = null;
    Connection c = acs.getDataSource().getConnection();
    try {
      InsertOrUpdateStatement ious =
          new InsertOrUpdateStatement("trigger_", new Field("id", trigger.getId()));
      ious.addField(new Field("name", trigger.getName()));
      ious.addField(new Field("description", trigger.getDescription()));
      ious.addField(new Field("trigger_type", trigger.getTriggerType()));
      ious.addField(new Field("notify_type", trigger.getNotifyType()));
      ious.addField(new Field("active", trigger.isActive() ? 1 : 0));
      ious.addField(new Field("unit_type_id", trigger.getUnittype().getId()));
      //			if (trigger.getGroup() != null)
      //				ious.addField(new Field("group_id", trigger.getGroup().getId()));
      ious.addField(new Field("eval_period_minutes", trigger.getEvalPeriodMinutes()));
      ious.addField(new Field("notify_interval_hours", trigger.getNotifyIntervalHours()));
      if (trigger.getScript() != null) {
        ious.addField(new Field("filestore_id", trigger.getScript().getId()));
      } else {
        ious.addField(new Field("filestore_id", (Integer) null));
      }
      if (trigger.getParent() != null) {
        ious.addField(new Field("parent_trigger_id", trigger.getParent().getId()));
      } else {
        ious.addField(new Field("parent_trigger_id", (Integer) null));
      }
      ious.addField(new Field("to_list", trigger.getToList()));
      if (trigger.getSyslogEvent() != null) {
        ious.addField(new Field("syslog_event_id", trigger.getSyslogEvent().getId()));
      } else {
        ious.addField(new Field("syslog_event_id", (Integer) null));
      }
      ious.addField(new Field("no_events", trigger.getNoEvents()));
      ious.addField(new Field("no_events_pr_unit", trigger.getNoEventsPrUnit()));
      ious.addField(new Field("no_units", trigger.getNoUnits()));
      ps = ious.makePreparedStatement(c);
      ps.setQueryTimeout(60);
      ps.executeUpdate();
      if (ious.isInsert()) {
        ResultSet gk = ps.getGeneratedKeys();
        if (gk.next()) {
          trigger.setId(gk.getInt(1));
        }
        logger.info("Inserted trigger " + trigger.getName());
        if (acs.getDbi() != null) {
          acs.getDbi().publishAdd(trigger, trigger.getUnittype());
        }
      } else {
        if (trigger.isSyslogEventChanged()) { // delete all trigger_events for this trigger
          deleteEvents(trigger.getId(), new Date(), acs);
          trigger.setSyslogEventChangeCompleted();
        }
        logger.info("Updated trigger " + trigger.getName());
        if (acs.getDbi() != null) {
          acs.getDbi().publishChange(trigger, trigger.getUnittype());
        }
      }
    } finally {
      if (ps != null) {
        ps.close();
      }
      c.close();
    }
  }

  protected Map<String, Trigger> getNameMap() {
    return nameMap;
  }

  protected Map<Integer, Trigger> getIdMap() {
    return idMap;
  }

  public List<Trigger> getTopLevelTriggers() {
    Trigger[] allTriggers = getTriggers();
    List<Trigger> topLevelTriggers = new ArrayList<>();
    for (Trigger t : allTriggers) {
      if (t.getParent() == null) {
        topLevelTriggers.add(t);
      }
    }
    return topLevelTriggers;
  }
}
