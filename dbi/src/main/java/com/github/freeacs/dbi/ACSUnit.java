package com.github.freeacs.dbi;

import com.github.freeacs.dbi.util.ACSVersionCheck;
import com.github.freeacs.dbi.util.SyslogClient;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** ACSUnit is a class to help you work with units and unit parameters. */
public class ACSUnit {
  private static Logger logger = LoggerFactory.getLogger(ACSUnit.class);
  private static long updateCounter;
  private static long insertCounter;

  private DataSource dataSource;
  private Syslog syslog;
  private ACS acs;

  public ACSUnit(DataSource dataSource, ACS acs, Syslog syslog) throws SQLException {
    this.dataSource = dataSource;
    this.syslog = syslog;
    if (acs == null) {
      throw new IllegalArgumentException("The ACSUnit constructor requires a non-null ACS object");
    }
    this.acs = acs;
    if (acs.getUnittypes() == null) {
      acs.read();
    }
  }

  /**
   * @param value - expected to be a unique unit parameter value (ex: serialnumber, mac, ip, etc)
   * @param unittype - may be null
   * @param profile - may be null
   * @return a unit object will all unit parameters found
   */
  public Unit getUnitByValue(String value, Unittype unittype, Profile profile) throws SQLException {
    Connection connection = null;
    boolean wasAutoCommit = false;
    try {
      connection = dataSource.getConnection();
      wasAutoCommit = connection.getAutoCommit();
      connection.setAutoCommit(false);
      UnitQueryCrossUnittype uqcu = new UnitQueryCrossUnittype(connection, acs, unittype, profile);
      Unit u = uqcu.getUnitByValue(value);
      if (u != null && ACSVersionCheck.unitParamSessionSupported && u.isSessionMode()) {
        return uqcu.addSessionParameters(u);
      } else {
        return u;
      }
    } finally {
      if (connection != null) {
        connection.setAutoCommit(wasAutoCommit);
        connection.close();
      }
    }
  }

  public Unit getLimitedUnitByValue(String value) throws SQLException {
    Connection connection = null;
    boolean wasAutoCommit = false;
    try {
      connection = dataSource.getConnection();
      wasAutoCommit = connection.getAutoCommit();
      connection.setAutoCommit(false);
      UnitQueryCrossUnittype uqcu =
          new UnitQueryCrossUnittype(connection, acs, null, (Profile) null);
      return uqcu.getLimitedUnitByValue(value);
    } finally {
      if (connection != null) {
        connection.setAutoCommit(wasAutoCommit);
        connection.close();
      }
    }
  }

  /**
   * @param unittype - may be null
   * @param profile - may be null
   * @return a unit object will all unit parameters found
   */
  public Unit getUnitById(String unitId, Unittype unittype, Profile profile) throws SQLException {
    Connection connection = null;
    boolean wasAutoCommit = false;
    try {
      connection = dataSource.getConnection();
      wasAutoCommit = connection.getAutoCommit();
      connection.setAutoCommit(false);
      UnitQueryCrossUnittype uqcu = new UnitQueryCrossUnittype(connection, acs, unittype, profile);
      Unit u = uqcu.getUnitById(unitId);
      if (u != null && ACSVersionCheck.unitParamSessionSupported && u.isSessionMode()) {
        return uqcu.addSessionParameters(u);
      } else {
        return u;
      }
    } finally {
      if (connection != null) {
        connection.setAutoCommit(wasAutoCommit);
        connection.close();
      }
    }
  }

  public Unit getUnitById(String unitId) throws SQLException {
    return getUnitById(unitId, null, null);
  }

  /**
   * This list will be INSERTed into the UNIT-table, and connected to the given profile (and thereby
   * to the correct unittype).
   *
   * <p>The function cannot change the profile or unittype for an already existing unitid. For
   * changing the profile, use the moveUnit()-function, for changing the unittype you would have to
   * delete all units from the current unittype, and add them to the new one. The reason for this is
   * that you should make a new set of unittype-parameters as well when you do that.
   */
  public void addUnits(List<String> unitIds, Profile profile) throws SQLException {
    Connection connection = null;
    PreparedStatement ps = null;
    boolean wasAutoCommit = false;
    try {
      connection = dataSource.getConnection();
      wasAutoCommit = connection.getAutoCommit();
      connection.setAutoCommit(false);
      Unittype unittype = profile.getUnittype();
      for (int i = 0; unitIds != null && i < unitIds.size(); i++) {
        String unitId = unitIds.get(i);
        DynamicStatement ds = new DynamicStatement();
        ds.addSql("INSERT INTO unit (unit_id, unit_type_id, profile_id) VALUES (?,?,?)");
        ds.addArguments(unitId, unittype.getId(), profile.getId());
        try {
          ps = ds.makePreparedStatement(connection);
          ps.setQueryTimeout(60);
          ps.executeUpdate();
          SyslogClient.info(unitId, "Added unit", syslog);
          logger.info("Added unit " + unitId);
        } catch (SQLException ex) {
          ds = new DynamicStatement();
          ds.addSql("UPDATE unit SET profile_id = ? WHERE unit_id = ? AND unit_type_id = ?");
          ds.addArguments(profile.getId(), unitId, unittype.getId());
          ps = ds.makePreparedStatement(connection);
          ps.setQueryTimeout(60);
          int rowsUpdated = ps.executeUpdate();
          if (rowsUpdated == 0) {
            throw ex;
          }
          if (rowsUpdated > 0) {
            SyslogClient.info(unitId, "Moved unit to profile " + profile.getName(), syslog);
            logger.info("Moved unit " + unitId + " to profile " + profile.getName());
          }
        }
        if (i > 0 && i % 100 == 0) {
          connection.commit();
        }
      }
      connection.commit();
    } catch (SQLException sqle) {
      if (connection != null) {
        connection.rollback();
      }
      throw sqle;
    } finally {
      if (ps != null) {
        ps.close();
      }
      if (connection != null) {
        connection.setAutoCommit(wasAutoCommit);
        connection.close();
      }
    }
  }

  /**
   * This method is made for moving a number of units from one profile to another. If you try to
   * move a unitId which does not exist, then the method aborts with an sqlexception.
   */
  public void moveUnits(List<String> unitIds, Profile profile) throws SQLException {
    Connection connection = null;
    PreparedStatement ps = null;
    boolean wasAutoCommit = false;
    try {
      connection = dataSource.getConnection();
      wasAutoCommit = connection.getAutoCommit();
      connection.setAutoCommit(false);
      Integer unittypeId = profile.getUnittype().getId();
      Integer profileId = profile.getId();
      for (int i = 0; unitIds != null && i < unitIds.size(); i++) {
        DynamicStatement ds = new DynamicStatement();
        ds.addSqlAndArguments(
            "UPDATE unit SET profile_id = ? WHERE unit_id = ? AND unit_type_id = ?",
            profileId,
            unitIds.get(i),
            unittypeId);
        ps = ds.makePreparedStatement(connection);
        ps.setQueryTimeout(60);
        ps.executeUpdate();

        logger.info("Moved unit " + unitIds.get(i) + " to profile " + profile.getName());
      }
      connection.commit();
    } catch (SQLException sqle) {
      if (connection != null) {
        connection.rollback();
      }
      throw sqle;
    } finally {
      if (ps != null) {
        ps.close();
      }
      if (connection != null) {
        connection.setAutoCommit(wasAutoCommit);
        connection.close();
      }
    }
  }

  private int executeSql(
      String sql, Connection c, UnittypeParameter unittypeParameter, String value, String unitId)
      throws SQLException {
    PreparedStatement pp = c.prepareStatement(sql);
    pp.setString(1, value);
    pp.setString(2, unitId);
    pp.setInt(3, unittypeParameter.getId());
    pp.setQueryTimeout(60);
    int rowsupdated = pp.executeUpdate();
    pp.close();
    return rowsupdated;
  }

  public List<String> getUnitIdsFromSessionUnitParameters() throws SQLException {
    Connection connection = null;
    PreparedStatement ps = null;
    ResultSet rs;
    boolean wasAutoCommit = false;
    try {
      connection = dataSource.getConnection();
      wasAutoCommit = connection.getAutoCommit();
      connection.setAutoCommit(false);
      DynamicStatement ds = new DynamicStatement();
      ds.addSql("SELECT unit_id FROM unit_param_session");
      ps = ds.makePreparedStatement(connection);
      ps.setQueryTimeout(60);
      rs = ps.executeQuery();
      List<String> unitIds = new ArrayList<>();
      while (rs.next()) {
        unitIds.add(rs.getString("unit_id"));
      }
      return unitIds;
    } finally {
      if (ps != null) {
        ps.close();
      }
      if (connection != null) {
        connection.setAutoCommit(wasAutoCommit);
        connection.close();
      }
    }
  }

  private void addOrChangeUnitParameters(
      List<UnitParameter> unitParameters, Profile prof, boolean session) throws SQLException {
    Connection connection = null;
    PreparedStatement pp = null;
    String sql;
    boolean updateFirst = true;
    String tableName = "unit_param";
    if (session) {
      tableName += "_session";
    }
    boolean wasAutoCommit = false;
    try {
      if (updateCounter < insertCounter) {
        updateFirst = false;
      }
      connection = dataSource.getConnection();
      wasAutoCommit = connection.getAutoCommit();
      connection.setAutoCommit(false);
      for (int i = 0; unitParameters != null && i < unitParameters.size(); i++) {
        UnitParameter unitParameter = unitParameters.get(i);
        String unitId = unitParameter.getUnitId();
        Parameter parameter = unitParameter.getParameter();
        if (parameter.getValue() != null && parameter.getValue().length() > 512) {
          parameter.setValue(parameter.getValue().substring(0, 509) + "...");
        }
        String value = parameter.getValue(); // will be "" if value was null
        String utpName = parameter.getUnittypeParameter().getName();
        String action = "Updated";
        if (updateFirst) {
          sql =
              "UPDATE " + tableName + " SET value = ? WHERE unit_id = ? AND unit_type_param_id = ?";
          int rowsupdated =
              executeSql(sql, connection, parameter.getUnittypeParameter(), value, unitId);
          if (rowsupdated == 0) {
            insertCounter++;
            action = "Added";
            sql =
                "INSERT INTO "
                    + tableName
                    + " (value, unit_id, unit_type_param_id) VALUES (?, ?, ?)";
            executeSql(sql, connection, parameter.getUnittypeParameter(), value, unitId);
          } else {
            updateCounter++;
          }
        } else {
          sql =
              "INSERT INTO " + tableName + " (value, unit_id, unit_type_param_id) VALUES (?, ?, ?)";
          try {
            executeSql(sql, connection, parameter.getUnittypeParameter(), value, unitId);
            insertCounter++;
            action = "Added";
          } catch (SQLException insertEx) {
            updateCounter++;
            sql =
                "UPDATE "
                    + tableName
                    + " SET value = ? WHERE unit_id = ? AND unit_type_param_id = ?";
            int rowsupdated =
                executeSql(sql, connection, parameter.getUnittypeParameter(), value, unitId);
            if (rowsupdated == 0) {
              throw insertEx;
            }
          }
        }
        if (updateCounter > 25 || insertCounter > 25) {
          if (updateCounter > insertCounter) {
            updateCounter = 1;
            insertCounter = 0;
          } else {
            updateCounter = 0;
            insertCounter = 1;
          }
        }
        String msg;
        if (tableName.contains("session")) {
          msg = action + " temporary unit parameter " + utpName;
        } else {
          msg = action + " unit parameter " + utpName;
        }
        if (parameter.getUnittypeParameter().getFlag().isConfidential()) {
          msg += " with confidental value (*****)";
        } else {
          msg += " with value " + parameter.getValue();
        }
        SyslogClient.info(unitId, msg, syslog);
        logger.info(msg);
        if (i > 0 && i % 100 == 0) {
          connection.commit();
        }
      }
      connection.commit();
    } catch (SQLException sqle) {
      connection.rollback();
      throw sqle;
    } finally {
      if (pp != null) {
        pp.close();
      }
      if (connection != null) {
        connection.setAutoCommit(wasAutoCommit);
        connection.close();
      }
    }
  }

  public void addOrChangeQueuedUnitParameters(Unit unit) throws SQLException {
    List<UnitParameter> queuedParameters = unit.flushWriteQueue();
    Iterator<UnitParameter> iterator = queuedParameters.iterator();
    while (iterator.hasNext()) {
      UnitParameter queuedUp = iterator.next();
      UnitParameter storedUp =
          unit.getUnitParameters().get(queuedUp.getParameter().getUnittypeParameter().getName());
      if (storedUp != null
          && storedUp.getValue() != null
          && storedUp.getValue().equals(queuedUp.getValue())) {
        iterator
            .remove(); // don't write the queued Unit Parameter if it has the same value as already
        // stored
      }
    }
    addOrChangeUnitParameters(queuedParameters, unit.getProfile());
  }

  public void addOrChangeUnitParameters(List<UnitParameter> unitParameters, Profile prof)
      throws SQLException {
    addOrChangeUnitParameters(unitParameters, prof, false);
  }

  public void addOrChangeUnitParameter(Unit unit, String unittypeParameterName, String value)
      throws SQLException {
    Unittype unittype = unit.getUnittype();
    Parameter parameter =
        new Parameter(unittype.getUnittypeParameters().getByName(unittypeParameterName), value);
    UnitParameter up = new UnitParameter(parameter, unit.getId(), unit.getProfile());
    List<UnitParameter> ups = new ArrayList<>();
    ups.add(up);
    addOrChangeUnitParameters(ups, unit.getProfile());
  }

  public void addOrChangeSessionUnitParameters(List<UnitParameter> unitParameters, Profile prof)
      throws SQLException {
    if (ACSVersionCheck.unitParamSessionSupported) {
      addOrChangeUnitParameters(unitParameters, prof, true);
    }
  }

  /** Deletes the unit and all the unitparameters in that unit. */
  public int deleteUnit(Unit unit) throws SQLException {
    Connection connection = null;
    PreparedStatement ps = null;
    boolean wasAutoCommit = false;
    try {
      connection = dataSource.getConnection();
      wasAutoCommit = connection.getAutoCommit();
      connection.setAutoCommit(false);
      DynamicStatement ds = new DynamicStatement();
      ds.addSqlAndArguments("DELETE FROM unit_param WHERE unit_id = ?", unit.getId());
      ps = ds.makePreparedStatement(connection);
      ps.setQueryTimeout(60);
      int paramsDeleted = ps.executeUpdate();
      ps.close();

      ds = new DynamicStatement();
      ds.addSqlAndArguments("DELETE FROM unit_job WHERE unit_id = ?", unit.getId());
      ps = ds.makePreparedStatement(connection);
      ps.setQueryTimeout(60);
      int unitJobsDeleted = ps.executeUpdate();
      ps.close();

      ds = new DynamicStatement();
      ds.addSqlAndArguments("DELETE FROM unit WHERE unit_id = ?", unit.getId());
      ps = ds.makePreparedStatement(connection);
      ps.setQueryTimeout(60);
      int rowsDeleted = ps.executeUpdate();
      ps.close();

      connection.commit();
      if (paramsDeleted > 0) {
        logger.info("Deleted " + paramsDeleted + " unit parameters for unit " + unit.getId());
      }
      if (unitJobsDeleted > 0) {
        logger.info("Deleted " + unitJobsDeleted + " unit jobs for unit " + unit.getId());
      }
      if (rowsDeleted == 0) {
        logger.warn("No unit deleted, possibly because it did not exist.");
      } else {
        SyslogClient.info(unit.getId(), "Deleted unit", syslog);
        logger.info("Deleted unit " + unit.getId());
      }
      return rowsDeleted;
    } catch (SQLException sqle) {
      if (connection != null) {
        connection.rollback();
      }
      throw sqle;
    } finally {
      if (ps != null) {
        ps.close();
      }
      if (connection != null) {
        connection.setAutoCommit(wasAutoCommit);
        connection.close();
      }
    }
  }

  /**
   * Deletes all unitparameters and units for the units in the given profile. WARNING: These SQL
   * statements may be very slow to execute. Look at the plan here: 1. SQL to get all Units in a
   * profile 2. Iterate over all units in profile and delete parameters for each one. 3. SQL to
   * delete all units in a profile If you have 100000 units in a profile, then you will need to run
   * 100002 SQL statements. That is going to take a long time.
   */
  public void deleteUnits(Profile profile) throws SQLException {
    Statement s = null;
    String sql = null;
    Map<String, Unit> unitMap =
        getUnits(profile.getUnittype(), profile, (Parameter) null, Integer.MAX_VALUE);
    Connection connection = null;
    boolean wasAutoCommit = false;
    try {
      connection = dataSource.getConnection();
      wasAutoCommit = connection.getAutoCommit();
      connection.setAutoCommit(false);
      s = connection.createStatement();
      int counter = 0;
      int upDeleted = 0;
      for (String unitId : unitMap.keySet()) {
        sql = "DELETE FROM unit_param WHERE unit_id = '" + unitId + "'";
        upDeleted += s.executeUpdate(sql);
        if (counter > 0 && counter % 100 == 0) {
          connection.commit();
        }
        counter++;
      }

      logger.info(
          "Deleted unit parameters for all units in for profile "
              + profile.getName()
              + "("
              + upDeleted
              + " parameters deleted)");
      sql = "DELETE FROM unit WHERE profile_id = " + profile.getId();
      int rowsDeleted = s.executeUpdate(sql);
      logger.info(
          "Deleted all units in for profile "
              + profile.getName()
              + "("
              + rowsDeleted
              + " units deleted)");
      connection.commit();
    } catch (SQLException sqle) {
      if (connection != null) {
        connection.rollback();
      }
      throw sqle;
    } finally {
      if (s != null) {
        s.close();
      }
      if (connection != null) {
        connection.setAutoCommit(wasAutoCommit);
        connection.close();
      }
    }
  }

  public void deleteUnitParameters(Unit unit) throws SQLException {
    deleteUnitParameters(unit.flushDeleteQueue());
  }

  /**
   * Deletes all unitparameters in the list. If list set to null then all parameters are deleted.
   */
  public void deleteUnitParameters(List<UnitParameter> unitParameters) throws SQLException {
    Connection connection = null;
    Statement s = null;
    String sql;
    boolean wasAutoCommit = false;
    try {
      connection = dataSource.getConnection();
      wasAutoCommit = connection.getAutoCommit();
      connection.setAutoCommit(false);
      s = connection.createStatement();
      int rowsDeleted = 0;
      for (UnitParameter unitParameter : unitParameters) {
        Integer utpId = unitParameter.getParameter().getUnittypeParameter().getId();
        String unitId = unitParameter.getUnitId();
        sql =
            "DELETE FROM unit_param WHERE unit_id = '"
                + unitId
                + "' AND unit_type_param_id = "
                + utpId;
        s.setQueryTimeout(60);
        rowsDeleted += s.executeUpdate(sql);
        if (rowsDeleted > 0) {
          SyslogClient.info(
              unitId,
              "Deleted unit parameter " + unitParameter.getParameter().getUnittypeParameter(),
              syslog);
          logger.info(
              "Deleted unit parameter " + unitParameter.getParameter().getUnittypeParameter());
        }
      }
      connection.commit();
    } catch (SQLException sqle) {
      if (connection != null) {
        connection.rollback();
      }
      throw sqle;
    } finally {
      if (s != null) {
        s.close();
      }
      if (connection != null) {
        connection.setAutoCommit(wasAutoCommit);
        connection.close();
      }
    }
  }

  public void deleteAllSessionParameters(Unit unit) throws SQLException {
    if (!ACSVersionCheck.unitParamSessionSupported) {
      return;
    }
    Connection connection = null;
    Statement s = null;
    String sql;
    boolean wasAutoCommit = false;
    try {
      connection = dataSource.getConnection();
      wasAutoCommit = connection.getAutoCommit();
      connection.setAutoCommit(false);
      s = connection.createStatement();
      int rowsDeleted = 0;
      sql = "DELETE FROM unit_param_session WHERE unit_id = '" + unit.getId() + "'";
      s.setQueryTimeout(60);
      rowsDeleted += s.executeUpdate(sql);
      if (rowsDeleted > 0) {
        logger.info("Deleted " + rowsDeleted + " unit session parameters");
      }
      connection.commit();
    } finally {
      if (s != null) {
        s.close();
      }
      if (connection != null) {
        connection.setAutoCommit(wasAutoCommit);
        connection.close();
      }
    }
  }

  /**
   * Deletes all units and the belonging unit-parameters in the list. This method commits during the
   * execution, so if something fails during the execution, something might already have been
   * committed. This is done for performance reasons.
   */
  public void deleteUnits(List<String> unitIds) throws SQLException {
    Connection connection = null;
    Statement s = null;
    String sql;
    boolean wasAutoCommit = false;
    try {
      connection = dataSource.getConnection();
      wasAutoCommit = connection.getAutoCommit();
      connection.setAutoCommit(false);
      s = connection.createStatement();
      int rowsDeleted = 0;
      for (int i = 0; i < unitIds.size(); i++) {
        sql = "DELETE FROM unit_param WHERE unit_id = '" + unitIds.get(i) + "'";
        s.setQueryTimeout(60);
        int upDeleted = s.executeUpdate(sql);
        logger.info(
            "Deleted all unit parameters for unit "
                + unitIds.get(i)
                + "("
                + upDeleted
                + " parameters deleted)");
        sql = "DELETE FROM unit WHERE unit_id = '" + unitIds.get(i) + "'";
        s.setQueryTimeout(60);
        rowsDeleted += s.executeUpdate(sql);
        SyslogClient.info(unitIds.get(i), "Deleted unit", syslog);
        logger.info("Deleted unit " + unitIds.get(i));
        if (i > 0 && i % 100 == 0) {
          connection.commit();
        }
      }
      connection.commit();
    } catch (SQLException sqle) {
      // We will rollback that which is not yet commited.
      if (connection != null) {
        connection.rollback();
      }
      throw sqle;
    } finally {
      if (s != null) {
        s.close();
      }
      if (connection != null) {
        connection.setAutoCommit(wasAutoCommit);
        connection.close();
      }
    }
  }

  /**
   * This method will return a map of fully populated Unit objects. Do not ask for a large number of
   * units (>100), since then it may take a long time to complete. Also the result-set may be
   * memory-intensive if very large.
   *
   * @param units - this list must be retrieved by running one of the getUnits() methods
   * @param unittype - may be null
   * @param profile - may be null
   * @return map of fully populated Unit objects
   */
  public List<Unit> getUnitsWithParameters(Unittype unittype, Profile profile, List<Unit> units)
      throws SQLException {
    Connection connection = null;
    boolean wasAutoCommit = false;
    try {
      connection = dataSource.getConnection();
      wasAutoCommit = connection.getAutoCommit();
      connection.setAutoCommit(false);
      UnitQueryCrossUnittype uqcu = new UnitQueryCrossUnittype(connection, acs, unittype, profile);
      return uqcu.getUnitsById(units);
    } finally {
      if (connection != null) {
        connection.setAutoCommit(wasAutoCommit);
        connection.close();
      }
    }
  }

  /**
   * @param searchStr - may be null, if not null it will search for matches against unit-ids or
   *     unit-parameter values
   * @param unittype - may be null
   * @param profile - may be null
   * @param maxRows - may be null
   * @return a set of units. The unit object is not populated with unit parameters
   */
  public Map<String, Unit> getUnits(
      String searchStr, Unittype unittype, Profile profile, Integer maxRows) throws SQLException {
    Connection connection = null;
    boolean wasAutoCommit = false;
    try {
      connection = dataSource.getConnection();
      wasAutoCommit = connection.getAutoCommit();
      connection.setAutoCommit(false);
      UnitQueryCrossUnittype uqcu = new UnitQueryCrossUnittype(connection, acs, unittype, profile);
      return uqcu.getUnits(searchStr, maxRows);
    } finally {
      if (connection != null) {
        connection.setAutoCommit(wasAutoCommit);
        connection.close();
      }
    }
  }

  public Map<String, Unit> getUnits(String searchStr, List<Profile> profiles, Integer maxRows)
      throws SQLException {
    Connection connection = null;
    boolean wasAutoCommit = false;
    try {
      connection = dataSource.getConnection();
      wasAutoCommit = connection.getAutoCommit();
      connection.setAutoCommit(false);
      UnitQueryCrossUnittype uqcu =
          new UnitQueryCrossUnittype(connection, acs, (Unittype) null, profiles);
      return uqcu.getUnits(searchStr, maxRows);
    } finally {
      if (connection != null) {
        connection.setAutoCommit(wasAutoCommit);
        connection.close();
      }
    }
  }

  /**
   * @param unittype - Must specify
   * @param profiles - If omitted, they will be set to the list of allowed profiles for this
   *     unittype
   * @param parameters - If preset, they must be from the unittype
   * @return A set of units with unit-parameters populated for those parameters asked for
   */
  public Map<String, Unit> getUnits(
      Unittype unittype, List<Profile> profiles, List<Parameter> parameters, Integer limit)
      throws SQLException {
    Connection connection = null;
    boolean wasAutoCommit = false;
    try {
      connection = dataSource.getConnection();
      wasAutoCommit = connection.getAutoCommit();
      connection.setAutoCommit(false);
      UnitQueryWithinUnittype uqwu =
          new UnitQueryWithinUnittype(connection, acs, unittype, profiles);
      return uqwu.getUnits(parameters, limit);
    } finally {
      if (connection != null) {
        connection.setAutoCommit(wasAutoCommit);
        connection.close();
      }
    }
  }

  public Map<String, Unit> getUnits(
      Unittype unittype, Profile profile, List<Parameter> parameters, Integer limit)
      throws SQLException {
    List<Profile> profiles = new ArrayList<>();
    if (profile != null) {
      profiles.add(profile);
    }
    return getUnits(unittype, profiles, parameters, limit);
  }

  public Map<String, Unit> getUnits(
      Unittype unittype, Profile profile, Parameter parameter, Integer limit) throws SQLException {
    List<Profile> profiles = new ArrayList<>();
    if (profile != null) {
      profiles.add(profile);
    }
    List<Parameter> parameters = new ArrayList<>();
    if (parameter != null) {
      parameters.add(parameter);
    }
    return getUnits(unittype, profiles, parameters, limit);
  }

  public Map<String, Unit> getUnits(
      Unittype unittype, List<Profile> profiles, Parameter parameter, Integer limit)
      throws SQLException {
    List<Parameter> parameters = new ArrayList<>();
    if (parameter != null) {
      parameters.add(parameter);
    }
    return getUnits(unittype, profiles, parameters, limit);
  }

  public Map<String, Unit> getUnits(Group group) throws SQLException {
    Group topParent = group.getTopParent();
    Profile profile = topParent.getProfile();
    Unittype unittype = group.getUnittype();
    return getUnits(
        unittype, profile, group.getGroupParameters().getAllParameters(group), Integer.MAX_VALUE);
  }

  public int getUnitCount(Group group) throws SQLException {
    Group topParent = group.getTopParent();
    Profile profile = topParent.getProfile();
    Unittype unittype = group.getUnittype();
    return getUnitCount(unittype, profile, group.getGroupParameters().getAllParameters(group));
  }

  /**
   * @param unittype - Must specify
   * @param profiles - If omitted, they will be set to the list of allowed profiles for this
   *     unittype
   * @param parameters - If preset, they must be from the unittype
   * @return A set of units with unit-parameters populated for those parameters asked for
   */
  public int getUnitCount(Unittype unittype, List<Profile> profiles, List<Parameter> parameters)
      throws SQLException {
    Connection connection = null;
    boolean wasAutoCommit = false;
    try {
      connection = dataSource.getConnection();
      wasAutoCommit = connection.getAutoCommit();
      connection.setAutoCommit(false);
      UnitQueryWithinUnittype uqwu =
          new UnitQueryWithinUnittype(connection, acs, unittype, profiles);
      return uqwu.getUnitCount(parameters);
    } finally {
      if (connection != null) {
        connection.setAutoCommit(wasAutoCommit);
        connection.close();
      }
    }
  }

  public int getUnitCount(List<Unittype> unittypes)
      throws SQLException {
    int count = 0;
    for (Unittype unittype: unittypes) {
      count += getUnitCount(unittype, Arrays.asList(unittype.getProfiles().getProfiles()), (Parameter) null);
    }
    return count;
  }

  public int getUnitCount(Unittype unittype, List<Profile> profiles, Parameter parameter)
      throws SQLException {
    List<Parameter> parameters = new ArrayList<>();
    if (parameter != null) {
      parameters.add(parameter);
    }
    return getUnitCount(unittype, profiles, parameters);
  }

  public int getUnitCount(Unittype unittype, Profile profile, List<Parameter> parameters)
      throws SQLException {
    List<Profile> profiles = new ArrayList<>();
    if (profile != null) {
      profiles.add(profile);
    }
    return getUnitCount(unittype, profiles, parameters);
  }

  public int getUnitCount(Unittype unittype, Profile profile, Parameter parameter)
      throws SQLException {
    List<Profile> profiles = new ArrayList<>();
    if (profile != null) {
      profiles.add(profile);
    }
    List<Parameter> parameters = new ArrayList<>();
    if (parameter != null) {
      parameters.add(parameter);
    }
    return getUnitCount(unittype, profiles, parameters);
  }

  public ACS getAcs() {
    return acs;
  }
}
