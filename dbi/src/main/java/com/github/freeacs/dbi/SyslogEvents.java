package com.github.freeacs.dbi;

import com.github.freeacs.dbi.InsertOrUpdateStatement.Field;
import com.github.freeacs.dbi.SyslogEvent.StorePolicy;
import com.github.freeacs.dbi.util.ACSVersionCheck;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SyslogEvents {
  private static Logger logger = LoggerFactory.getLogger(SyslogEvents.class);
  private static Map<Integer, SyslogEvent> idMap = new TreeMap<>();

  static {
    SyslogEvent defaultEvent = new SyslogEvent();
    defaultEvent.validateInput(false);
    defaultEvent.setEventId(0);
    defaultEvent.setName("Default");
    defaultEvent.setDescription("Default event");
    defaultEvent.setDeleteLimit(0);
    idMap.put(0, defaultEvent);
  }

  private Map<Integer, SyslogEvent> eventIdMap;
  private Unittype unittype;

  public SyslogEvents(Map<Integer, SyslogEvent> eventIdMap, Unittype unittype) {
    this.eventIdMap = eventIdMap;
    for (SyslogEvent event : eventIdMap.values()) {
      idMap.put(event.getId(), event);
    }
    this.unittype = unittype;
  }

  protected static void updateIdMap(SyslogEvent syslogEvent) {
    idMap.put(syslogEvent.getId(), syslogEvent);
  }

  public static SyslogEvent getById(Integer id) {
    return idMap.get(id);
  }

  public SyslogEvent getByEventId(Integer id) {
    return eventIdMap.get(id);
  }

  public SyslogEvent[] getSyslogEvents() {
    SyslogEvent[] syslogEvents = new SyslogEvent[eventIdMap.size()];
    eventIdMap.values().toArray(syslogEvents);
    return syslogEvents;
  }

  @Override
  public String toString() {
    return "Contains " + idMap.size() + " syslog events";
  }

  private void addOrChangeSyslogEventImpl(SyslogEvent syslogEvent, ACS acs) throws SQLException {
    Connection c = acs.getDataSource().getConnection();
    PreparedStatement ps = null;
    try {
      InsertOrUpdateStatement ious =
          new InsertOrUpdateStatement("syslog_event", new Field("id", syslogEvent.getId()));
      ious.addField(new Field("syslog_event_id", syslogEvent.getEventId()));
      ious.addField(new Field("syslog_event_name", syslogEvent.getName()));
      ious.addField(new Field("description", syslogEvent.getDescription()));
      ious.addField(new Field("expression", syslogEvent.getExpression()));
      ious.addField(new Field("delete_limit", syslogEvent.getDeleteLimit()));
      if (ACSVersionCheck.syslogEventReworkSupported) {
        ious.addField(new Field("unit_type_id", syslogEvent.getUnittype().getId()));
        ious.addField(new Field("store_policy", syslogEvent.getStorePolicy().toString()));
        ious.addField(
            new Field(
                "filestore_id",
                syslogEvent.getScript() == null ? null : syslogEvent.getScript().getId()));
        ious.addField(
            new Field(
                "group_id",
                syslogEvent.getGroup() == null ? null : syslogEvent.getGroup().getId()));
      } else {
        ious.addField(new Field("unit_type_name", syslogEvent.getUnittype().getName()));
        if (syslogEvent.getStorePolicy() == StorePolicy.DUPLICATE) {
          ious.addField(
              new Field("task", StorePolicy.DUPLICATE + "" + SyslogEvent.DUPLICATE_TIMEOUT));
        } else {
          ious.addField(new Field("task", syslogEvent.getStorePolicy()));
        }
      }
      ps = ious.makePreparedStatement(c);
      ps.setQueryTimeout(60);
      ps.executeUpdate();
      if (ious.isInsert()) {
        ResultSet gk = ps.getGeneratedKeys();
        if (gk.next()) {
          syslogEvent.setId(gk.getInt(1));
        }
        logger.info("Inserted syslog event " + syslogEvent.getEventId());
        if (acs.getDbi() != null) {
          acs.getDbi().publishAdd(syslogEvent, syslogEvent.getUnittype());
        }
      } else {
        logger.info("Updated syslog event " + syslogEvent.getEventId());
        if (acs.getDbi() != null) {
          acs.getDbi().publishChange(syslogEvent, unittype);
        }
      }
    } finally {
      if (ps != null) {
        ps.close();
      }
      if (c != null) {
        c.close();
      }
    }
  }

  public void addOrChangeSyslogEvent(SyslogEvent syslogEvent, ACS acs) throws SQLException {
    if (!acs.getUser().isUnittypeAdmin(unittype.getId())) {
      throw new IllegalArgumentException("Not allowed action for this user");
    }
    syslogEvent.validate();
    addOrChangeSyslogEventImpl(syslogEvent, acs);
    idMap.put(syslogEvent.getId(), syslogEvent);
    eventIdMap.put(syslogEvent.getEventId(), syslogEvent);
  }

  private void deleteSyslogEventImpl(Unittype unittype, SyslogEvent syslogEvent, ACS acs)
      throws SQLException {
    PreparedStatement ps = null;
    Connection c = acs.getDataSource().getConnection();
    try {
      DynamicStatement ds = new DynamicStatement();
      if (ACSVersionCheck.syslogEventReworkSupported) {
        ds.addSqlAndArguments(
            "DELETE FROM syslog_event WHERE syslog_event_id = ? ", syslogEvent.getEventId());
      }
      if (ACSVersionCheck.syslogEventReworkSupported) {
        ds.addSqlAndArguments("AND unit_type_id = ?", unittype.getId());
      } else {
        ds.addSqlAndArguments("AND unit_type_name = ?", unittype.getName());
      }
      ps = ds.makePreparedStatement(c);
      ps.setQueryTimeout(60);
      ps.executeUpdate();

      logger.info("Deleted syslog event " + syslogEvent.getEventId());
      if (acs.getDbi() != null) {
        acs.getDbi().publishDelete(syslogEvent, unittype);
      }
    } finally {
      if (ps != null) {
        ps.close();
      }
      if (c != null) {
        c.close();
      }
    }
  }

  /**
   * The first time this method is run, the flag is set. The second time this method is run, the
   * parameter is removed from the name- and id-Map.
   *
   * @throws SQLException
   */
  public void deleteSyslogEvent(SyslogEvent syslogEvent, ACS acs) throws SQLException {
    if (!acs.getUser().isUnittypeAdmin(unittype.getId())) {
      throw new IllegalArgumentException("Not allowed action for this user");
    }
    if (syslogEvent.getEventId() < 1000) {
      throw new IllegalArgumentException(
          "Cannot delete syslog events with id 0-999, they are restricted to ACS");
    }
    deleteSyslogEventImpl(syslogEvent, acs);
  }

  protected void deleteSyslogEventImpl(SyslogEvent syslogEvent, ACS acs) throws SQLException {
    deleteSyslogEventImpl(unittype, syslogEvent, acs);
    idMap.remove(syslogEvent.getId());
    eventIdMap.remove(syslogEvent.getEventId());
  }
}
