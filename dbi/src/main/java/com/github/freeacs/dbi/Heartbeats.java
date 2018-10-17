package com.github.freeacs.dbi;

import com.github.freeacs.dbi.InsertOrUpdateStatement.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Heartbeats {
  private static Logger logger = LoggerFactory.getLogger(Heartbeats.class);

  private Map<String, Heartbeat> nameMap;
  private Map<Integer, Heartbeat> idMap;
  private Unittype unittype;

  public Heartbeats(
      Map<Integer, Heartbeat> idMap, Map<String, Heartbeat> nameMap, Unittype unittype) {
    this.idMap = idMap;
    this.nameMap = nameMap;
    this.unittype = unittype;
  }

  public Heartbeat getById(Integer id) {
    return idMap.get(id);
  }

  public Heartbeat getByName(String name) {
    return nameMap.get(name);
  }

  public Heartbeat[] getHeartbeats() {
    return nameMap.values().toArray(new Heartbeat[] {});
  }

  @Override
  public String toString() {
    return "Contains " + idMap.size() + " heartbeats";
  }

  private void addOrChangeHeartbeatImpl(Heartbeat heartbeat, ACS acs) throws SQLException {
    Connection c = acs.getDataSource().getConnection();
    PreparedStatement ps = null;
    try {
      InsertOrUpdateStatement ious =
          new InsertOrUpdateStatement("heartbeat", new Field("id", heartbeat.getId()));
      ious.addField(new Field("name", heartbeat.getName()));
      ious.addField(new Field("unit_type_id", heartbeat.getUnittype().getId()));
      ious.addField(new Field("heartbeat_expression", heartbeat.getExpression()));
      ious.addField(new Field("heartbeat_group_id", heartbeat.getGroup().getId()));
      ious.addField(new Field("heartbeat_timeout_hour", heartbeat.getTimeoutHours()));
      ps = ious.makePreparedStatement(c);
      ps.setQueryTimeout(60);
      ps.executeUpdate();
      if (ious.isInsert()) {
        ResultSet gk = ps.getGeneratedKeys();
        if (gk.next()) {
          heartbeat.setId(gk.getInt(1));
        }
        logger.info("Inserted heartbeat " + heartbeat.getId());
        if (acs.getDbi() != null) {
          acs.getDbi().publishAdd(heartbeat, heartbeat.getUnittype());
        }
      } else {
        logger.info("Updated heartbeat " + heartbeat.getId());
        if (acs.getDbi() != null) {
          acs.getDbi().publishChange(heartbeat, unittype);
        }
      }
    } finally {
      if (ps != null) {
        ps.close();
      }
      c.close();
    }
  }

  public void addOrChangeHeartbeat(Heartbeat heartbeat, ACS acs) throws SQLException {
    if (!acs.getUser().isUnittypeAdmin(unittype.getId())) {
      throw new IllegalArgumentException("Not allowed action for this user");
    }
    heartbeat.validateInput(true);
    heartbeat.validate();
    addOrChangeHeartbeatImpl(heartbeat, acs);
    idMap.put(heartbeat.getId(), heartbeat);
    nameMap.put(heartbeat.getName(), heartbeat);
  }

  private void deleteHeartbeatImpl(Heartbeat heartbeat, ACS acs) throws SQLException {
    PreparedStatement ps = null;
    Connection c = acs.getDataSource().getConnection();
    try {
      DynamicStatement ds = new DynamicStatement();
      ds.addSqlAndArguments("DELETE FROM heartbeat WHERE id = ? ", heartbeat.getId());
      ps = ds.makePreparedStatement(c);
      ps.setQueryTimeout(60);
      ps.executeUpdate();

      logger.info("Deleted heartbeat " + heartbeat.getId());
      if (acs.getDbi() != null) {
        acs.getDbi().publishDelete(heartbeat, unittype);
      }
    } finally {
      if (ps != null) {
        ps.close();
      }
      c.close();
    }
  }

  /**
   * The first time this method is run, the flag is set. The second time this method is run, the
   * parameter is removed from the name- and id-Map.
   *
   * @throws SQLException
   */
  public void deleteHeartbeat(Heartbeat heartbeat, ACS acs) throws SQLException {
    if (!acs.getUser().isUnittypeAdmin(unittype.getId())) {
      throw new IllegalArgumentException("Not allowed action for this user");
    }
    deleteHeartbeatImpl(heartbeat, acs);
    idMap.remove(heartbeat.getId());
    nameMap.remove(heartbeat.getName());
  }
}
