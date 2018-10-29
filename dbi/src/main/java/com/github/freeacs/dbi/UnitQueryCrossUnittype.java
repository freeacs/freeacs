package com.github.freeacs.dbi;

import com.github.freeacs.dbi.util.SystemParameters;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Morten */
class UnitQueryCrossUnittype {
  private static Logger logger = LoggerFactory.getLogger(UnitQueryCrossUnittype.class);

  private List<Unittype> unittypes;
  private List<Profile> profiles;
  private Connection connection;
  private ACS acs;

  public UnitQueryCrossUnittype(
      Connection c, ACS acs, List<Unittype> unittypes, List<Profile> profiles) {
    this.connection = c;
    this.acs = acs;
    if (unittypes != null) {
      this.unittypes = unittypes;
    } else {
      this.unittypes = new ArrayList<>();
    }
    if (profiles != null) {
      this.profiles = profiles;
    } else {
      this.profiles = new ArrayList<>();
    }
    prepareUnittypesAndProfiles();
  }

  public UnitQueryCrossUnittype(Connection c, ACS acs, Unittype unittype, List<Profile> profiles) {
    this.connection = c;
    this.acs = acs;
    this.unittypes = new ArrayList<>();
    if (unittype != null) {
      unittypes.add(unittype);
    }
    if (profiles != null) {
      this.profiles = profiles;
    } else {
      this.profiles = new ArrayList<>();
    }
    prepareUnittypesAndProfiles();
  }

  public UnitQueryCrossUnittype(Connection c, ACS acs, Unittype unittype, Profile profile) {
    this.connection = c;
    this.acs = acs;
    this.unittypes = new ArrayList<>();
    if (unittype != null) {
      unittypes.add(unittype);
    }
    this.profiles = new ArrayList<>();
    if (profile != null) {
      profiles.add(profile);
    }
    prepareUnittypesAndProfiles();
  }

  /**
   * This method aims at two things:
   *
   * <p>1) reduce the number of profiles (if all within a unittype is specified) and replace them
   * with the unittype, to simplify the SQL to be run. 2) make sure that it's not possible to search
   * for all profiles or all unittypes by the process in 1) or accidentally by the client, if the
   * user to do not have the appropriate permissions.
   *
   * <p>Things to understand:
   *
   * <p>1) The list of unittypes/profiles coming into this method contains only allowed
   * unittypes/profiles.
   */
  private void prepareUnittypesAndProfiles() {
    User user = acs.getSyslog().getIdentity().getUser();

    if (!profiles.isEmpty()) {
      // Making a map of the number of profiles in the input, listed pr unittype
      Map<Integer, Integer> profileCountMap = new HashMap<>();
      for (Profile p : profiles) {
        Integer unittypeId = p.getUnittype().getId();
        Integer profileCount = profileCountMap.get(unittypeId);
        if (profileCount == null) {
          profileCount = 0;
        }
        profileCount++;
        profileCountMap.put(unittypeId, profileCount);
      }

      // This flag can only be kept true if user is unittypeAdmin for all unittypes
      // and all profiles are found.
      boolean allProfilesSpecified = true;
      for (Map.Entry<Integer, Integer> entry : profileCountMap.entrySet()) {
        Integer unittypeId = entry.getKey();
        boolean isUnittypeAdmin = user.isUnittypeAdmin(unittypeId);
        if (isUnittypeAdmin) {
          int allProfiles = acs.getUnittype(unittypeId).getProfiles().getProfiles().length;
          if (allProfiles > entry.getValue()) {
            allProfilesSpecified = false;
            break;
          }
        } else {
          allProfilesSpecified = false;
        }
      }
      if (allProfilesSpecified) {
        profiles = new ArrayList<>(); // search for all profiles in this unittype
        for (Integer unittypeId : profileCountMap.keySet()) {
          unittypes.add(acs.getUnittype(unittypeId));
        }
      } else {
        unittypes = new ArrayList<>(); // do not search for unittype, profiles must be specified
      }
    }
    if (user.isAdmin()) {
      if (unittypes.size() == acs.getUnittypes().getUnittypes().length) {
        unittypes = new ArrayList<>();
      } // search for all unittypes
    } else if (profiles.isEmpty() && unittypes.isEmpty()) {
      List<Unittype> allowedUnittypes = new ArrayList<>();
      List<Profile> allowedProfiles = new ArrayList<>();
      Unittype[] allowedUnittypeArr = acs.getUnittypes().getUnittypes();
      boolean allUnittypesAdmin = true;
      for (Unittype allowedU : allowedUnittypeArr) {
        if (!user.isUnittypeAdmin(allowedU.getId())) {
          allUnittypesAdmin = false;
        }
        allowedUnittypes.add(allowedU);
        Collections.addAll(allowedProfiles, allowedU.getProfiles().getProfiles());
      }
      if (allUnittypesAdmin) {
        unittypes = allowedUnittypes;
      } else {
        profiles = allowedProfiles;
      }
    }
  }

  private Map<String, Unit> getUnitsImpl(Map<String, Unit> units, String searchStr, Integer limit)
      throws SQLException {
    DynamicStatement ds;
    ds = computeSQL(searchStr);
    ResultSet rs = null;
    PreparedStatement pp = null;
    try {
      pp = ds.makePreparedStatement(connection);
      if (limit != null && limit > 0 && limit < 10000) {
        pp.setFetchSize(limit);
      } else {
        pp.setFetchSize(1000);
      }
      rs = pp.executeQuery();
      if (logger.isDebugEnabled()) {
        logger.debug("-SQL: " + ds.getSqlQuestionMarksSubstituted());
      }
      while (rs.next()) {
        String unitId = rs.getString("unit_id");
        Integer profileId = rs.getInt("profile_id");
        Integer unittypeId = rs.getInt("unit_type_id");
        Unittype unittype = acs.getUnittype(unittypeId);
        if (unittype == null) {
          break;
        } // can happen if user has no unittypes allowed
        Profile profile = unittype.getProfiles().getById(profileId);
        if (profile == null) {
          break;
        } // can happen if user has no profiles allowed, although expect the previous break
        // to hit first
        units.put(unitId, new Unit(unitId, unittype, profile));
        if (limit != null && limit > 0 && units.size() == limit) {
          break;
        }
      }
      return units;
    } catch (SQLException sqle) {
      logger.error("The sql that failed:" + ds.getSqlQuestionMarksSubstituted());
      throw sqle;
    } finally {
      if (rs != null) {
        rs.close();
      }
      if (pp != null) {
        pp.close();
      }
    }
  }

  private DynamicStatement computeSQL(String searchStr) {
    DynamicStatement ds = new DynamicStatement();
    ds.addSql("SELECT u1.unit_id, u1.profile_id, u1.unit_type_id FROM unit u1 WHERE ");
    if (searchStr != null) {
      searchStr = searchStr.replace('*', '%');
      if (searchStr.startsWith("%^")) {
        searchStr = searchStr.substring(2);
      } else if (searchStr.startsWith("^")) {
        searchStr = searchStr.substring(1);
      }
      if (searchStr.endsWith("$%")) {
        searchStr = searchStr.substring(0, searchStr.length() - 2);
      } else if (searchStr.endsWith("$")) {
        searchStr = searchStr.substring(0, searchStr.length() - 1);
      }
      boolean likeness =
          searchStr.contains("%") || searchStr.contains("_"); // Can search for equality or likeness
      boolean equalValue = true; // Can search for negated values
      if (searchStr.startsWith("!")) {
        searchStr = searchStr.substring(1);
        equalValue = false;
      } else if (searchStr.startsWith("%!")) {
        searchStr = "%" + searchStr.substring(2);
        equalValue = false;
      }
      ds.addSqlAndArguments(
          "(u1.unit_id IN (SELECT up.unit_id FROM unit_param up WHERE value "
              + operator(likeness, equalValue)
              + " ?) ",
          searchStr);
      ds.addSqlAndArguments(
          "OR u1.unit_id " + operator(likeness, equalValue) + " ?) AND ", searchStr);
    }
    if (!profiles.isEmpty()) {
      ds = searchAmongManyProfiles("u1", ds);
    } else if (!unittypes.isEmpty()) {
      ds = searchAmongManyUnittypes("u1", ds);
    }
    ds.cleanupSQLTail();
    return ds;
  }

  /**
   * This is a special case - to ask for a set of unittypes can only happen when no profile or
   * unittype parameters is specified.
   */
  private DynamicStatement searchAmongManyUnittypes(String alias, DynamicStatement ds) {
    ds.addSql("(");
    for (Unittype unittype : unittypes) {
      ds.addSqlAndArguments(alias + ".unit_type_id = ? OR ", unittype.getId());
    }
    ds.cleanupSQLTail();
    ds.addSql(") AND ");
    return ds;
  }

  private DynamicStatement searchAmongManyProfiles(String alias, DynamicStatement ds) {
    ds.addSql("(");
    for (Profile profile : profiles) {
      ds.addSqlAndArguments(alias + ".profile_id = ? OR ", profile.getId());
    }
    ds.cleanupSQLTail();
    ds.addSql(") AND ");
    return ds;
  }

  /**
   * If a profile matches the unit parameter in question the rule is to search for the set of units
   * which have the opposite value of the unit parameter value.
   *
   * <p>If one search for likeness, then one must use LIKE instead of =
   *
   * <p>If one search for equalValue, then obviously we want the value to be the same as the value
   * we search for.
   */
  private String operator(boolean likeness, boolean equalValue) {
    if (!likeness && !equalValue) {
      return "<>";
    }
    if (!likeness && equalValue) {
      return "=";
    }
    if (likeness && !equalValue) {
      return "NOT LIKE";
    }
    if (likeness && equalValue) {
      return "LIKE";
    }
    return ""; // Can never happen!
  }

  /**
   * This method requires a list of unitIds, minimum 1 unitId.
   *
   * @param units
   * @return
   */
  public List<Unit> getUnitsById(List<Unit> units) throws SQLException {
    if (units == null || units.isEmpty()) {
      return new ArrayList<>();
    }
    DynamicStatement ds = new DynamicStatement();
    ds.addSql(
        "SELECT u.unit_id, u.profile_id, u.unit_type_id, up.unit_type_param_id, up.value FROM unit u ");
    ds.addSql("LEFT JOIN unit_param up ON u.unit_id = up.unit_id WHERE ");
    StringBuilder sb = new StringBuilder();
    for (Unit unit : units) {
      if (unit != units.get(units.size() - 1)) {
        sb.append("'").append(unit.getId()).append("', ");
      } else {
        sb.append("'").append(unit.getId()).append("'");
      }
    }
    ds.addSql("u.unit_id IN (" + sb + ") AND ");
    if (!profiles.isEmpty()) {
      ds = searchAmongManyProfiles("u", ds);
    } else if (!unittypes.isEmpty()) {
      ds = searchAmongManyUnittypes("u", ds);
    }
    ds.cleanupSQLTail();
    ds.addSql(" ORDER BY u.unit_id");
    ResultSet rs = null;
    PreparedStatement pp = null;
    List<Unit> unitsWithDetails = new ArrayList<>();
    Unit lastUnit = null;
    try {
      pp = ds.makePreparedStatement(connection);
      pp.setQueryTimeout(60);
      rs = pp.executeQuery();
      if (logger.isDebugEnabled()) {
        logger.debug(ds.getDebugMessage());
      }
      Unit unit = null;
      Profile pr = null;
      Unittype ut = null;
      while (rs.next()) {
        String uid = rs.getString("unit_id");
        Integer profileId = rs.getInt("profile_id");
        Integer unittypeId = rs.getInt("unit_type_id");
        if (lastUnit == null || !lastUnit.getId().equals(uid)) {
          ut = acs.getUnittype(unittypeId);
          if (ut != null) {
            pr = ut.getProfiles().getById(profileId);
          }
          unit = new Unit(uid, ut, pr);
          unitsWithDetails.add(unit);
          lastUnit = unit;
        }
        if (!uid.equals(unit.getId())) {
          break;
        } // could happen for unitParamValue-search
        String unittypeParameterIdStr = rs.getString("unit_type_param_id");
        String value = rs.getString("value");
        if (value == null) {
          value = "";
        }
        if (unittypeParameterIdStr != null) {
          addUnitParameter(unit, pr, ut, uid, unittypeParameterIdStr, value);
        }
        unit.setParamsAvailable(true);
      }
      return unitsWithDetails;
    } catch (SQLException sqle) {
      logger.error("The sql that failed:" + ds.getSqlQuestionMarksSubstituted());
      throw sqle;
    } finally {
      if (rs != null) {
        rs.close();
      }
      if (pp != null) {
        pp.close();
      }
    }
  }

  private void addUnitParameter(
      Unit unit, Profile pr, Unittype ut, String uid, String unittypeParameterIdStr, String value) {
    Integer unittypeParameterId = Integer.parseInt(unittypeParameterIdStr);
    UnittypeParameter unittypeParameter = ut.getUnittypeParameters().getById(unittypeParameterId);
    UnitParameter uParam = new UnitParameter(unittypeParameter, uid, value, pr);
    unit.getUnitParameters().put(unittypeParameter.getName(), uParam);
  }

  public Unit getUnitById(String unitId) throws SQLException {
    DynamicStatement ds = new DynamicStatement();
    ds.addSql(
        "SELECT u.unit_id, u.profile_id, u.unit_type_id, up.unit_type_param_id, up.value FROM unit u ");
    ds.addSql("LEFT JOIN unit_param up ON u.unit_id = up.unit_id WHERE ");
    if (unitId != null) {
      ds.addSqlAndArguments("u.unit_id = ? AND ", unitId);
    }
    if (!profiles.isEmpty()) {
      ds = searchAmongManyProfiles("u", ds);
    } else if (!unittypes.isEmpty()) {
      ds = searchAmongManyUnittypes("u", ds);
    }
    ds.cleanupSQLTail();
    return getUnit(ds);
  }

  protected Unit addSessionParameters(Unit unit) throws SQLException {
    DynamicStatement ds = new DynamicStatement();
    ds.addSqlAndArguments("SELECT * FROM unit_param_session WHERE unit_id = ?", unit.getId());
    ResultSet rs = null;
    PreparedStatement pp = null;
    Unittype ut = unit.getUnittype();
    try {
      pp = ds.makePreparedStatement(connection);
      pp.setQueryTimeout(60);
      rs = pp.executeQuery();
      if (logger.isDebugEnabled()) {
        logger.debug(ds.getDebugMessage());
      }
      while (rs.next()) {
        String unittypeParameterIdStr = rs.getString("unit_type_param_id");
        String value = rs.getString("value");
        if (value == null) {
          value = "";
        }
        if (unittypeParameterIdStr != null) {
          Integer unittypeParameterId = Integer.parseInt(unittypeParameterIdStr);
          UnittypeParameter unittypeParameter =
              ut.getUnittypeParameters().getById(unittypeParameterId);
          UnitParameter sp =
              new UnitParameter(unittypeParameter, unit.getId(), value, unit.getProfile());
          unit.getSessionParameters().put(unittypeParameter.getName(), sp);
        }
      }
      return unit;
    } catch (SQLException sqle) {
      logger.error("The sql that failed:" + ds.getSqlQuestionMarksSubstituted());
      throw sqle;
    } finally {
      if (rs != null) {
        rs.close();
      }
      if (pp != null) {
        pp.close();
      }
    }
  }

  public Unit getLimitedUnitByValue(String uniqueUnitParamValue) throws SQLException {
    DynamicStatement ds = new DynamicStatement();
    ds.addSqlAndArguments("SELECT unit_id FROM unit_param WHERE value = ?", uniqueUnitParamValue);
    ResultSet rs = null;
    PreparedStatement pp = null;
    try {
      pp = ds.makePreparedStatement(connection);
      rs = pp.executeQuery();
      if (rs.next()) {
        String unitId = rs.getString("unit_id");
        ds = new DynamicStatement();
        ds.addSqlAndArguments(
            "SELECT unit_type_id, profile_id FROM unit WHERE unit_id = ?", unitId);
        pp = ds.makePreparedStatement(connection);
        rs = pp.executeQuery();
        rs.next();
        Unittype ut = acs.getUnittype(rs.getInt("unit_type_id"));
        Profile pr = ut.getProfiles().getById(rs.getInt("profile_id"));
        UnittypeParameter swUtp =
            ut.getUnittypeParameters().getByName(SystemParameters.SOFTWARE_VERSION);
        ds = new DynamicStatement();
        ds.addSqlAndArguments(
            "SELECT value FROM unit_param WHERE unit_type_param_id = ? AND unit_id = ?",
            swUtp.getId(),
            unitId);
        pp = ds.makePreparedStatement(connection);
        rs = pp.executeQuery();
        Unit unit = new Unit(unitId, ut, pr);
        if (rs.next()) {
          unit.getUnitParameters()
              .put(
                  SystemParameters.SOFTWARE_VERSION,
                  new UnitParameter(swUtp, unitId, rs.getString("value"), pr));
        }
        return unit;
      } else {
        return null;
      }
    } catch (SQLException sqle) {
      logger.error("The sql that failed:" + ds.getSqlQuestionMarksSubstituted(), sqle);
      throw sqle;
    } finally {
      if (rs != null) {
        rs.close();
      }
      if (pp != null) {
        pp.close();
      }
    }
  }

  public Unit getUnitByValue(String uniqueUnitParamValue) throws SQLException {
    DynamicStatement ds = new DynamicStatement();
    ds.addSql(
        "SELECT u.unit_id, u.profile_id, u.unit_type_id, up.unit_type_param_id, up.value FROM unit u ");
    ds.addSql("LEFT JOIN unit_param up ON u.unit_id = up.unit_id WHERE ");
    if (!profiles.isEmpty()) {
      ds = searchAmongManyProfiles("u", ds);
    } else if (!unittypes.isEmpty()) {
      ds = searchAmongManyUnittypes("u", ds);
    }
    if (uniqueUnitParamValue != null) {
      ds.addSql("u.unit_id IN (SELECT unit_id FROM unit_param up WHERE ");
      ds.addSqlAndArguments("up.value = ?) ORDER BY u.unit_id", uniqueUnitParamValue);
    }

    ds.cleanupSQLTail();
    return getUnit(ds);
  }

  private Unit getUnit(DynamicStatement ds) throws SQLException {
    PreparedStatement pp = null;
    ResultSet rs = null;
    try {
      pp = ds.makePreparedStatement(connection);
      pp.setQueryTimeout(60);
      rs = pp.executeQuery();
      if (logger.isDebugEnabled()) {
        logger.debug(ds.getDebugMessage());
      }
      Unit unit = null;
      Profile pr = null;
      Unittype ut = null;
      while (rs.next()) {
        String uid = rs.getString("unit_id");
        Integer profileId = rs.getInt("profile_id");
        Integer unittypeId = rs.getInt("unit_type_id");
        if (unit == null) {
          ut = acs.getUnittype(unittypeId);
          if (ut != null) {
            pr = ut.getProfiles().getById(profileId);
          }
          unit = new Unit(uid, ut, pr);
        }
        if (!uid.equals(unit.getId())) {
          break;
        } // could happen for unitParamValue-search
        String unittypeParameterIdStr = rs.getString("unit_type_param_id");
        String value = rs.getString("value");
        if (value == null) {
          value = "";
        }
        if (ut != null && unittypeParameterIdStr != null) {
          addUnitParameter(unit, pr, ut, uid, unittypeParameterIdStr, value);
        }
        unit.setParamsAvailable(true);
      }
      return unit;
    } catch (SQLException sqle) {
      logger.error("The sql that failed:" + ds.getSqlQuestionMarksSubstituted());
      throw sqle;
    } finally {
      if (rs != null) {
        rs.close();
      }
      if (pp != null) {
        pp.close();
      }
    }
  }

  public Map<String, Unit> getUnits(String searchStr, Integer limit) throws SQLException {
    Map<String, Unit> units;
    if (ACS.isStrictOrder()) {
      units = new TreeMap<>();
    } else {
      units = new HashMap<>();
    }
    return getUnitsImpl(units, searchStr, limit);
  }
}
