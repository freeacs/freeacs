package com.github.freeacs.dbi;

import com.github.freeacs.common.util.NaturalComparator;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnittypeParameters {
  private static Logger logger = LoggerFactory.getLogger(UnittypeParameters.class);
  private Map<Integer, UnittypeParameter> idMap;
  private Map<String, UnittypeParameter> nameMap;
  private Map<Integer, UnittypeParameter> alwaysMap;
  private Map<Integer, UnittypeParameter> displayableMap;
  private Map<String, String> displayableNameMap;
  private Map<Integer, UnittypeParameter> searchableMap;
  private Unittype unittype;
  private Boolean hasDeviceParameters;

  public UnittypeParameters(
      Map<Integer, UnittypeParameter> idMap,
      Map<String, UnittypeParameter> nameMap,
      Unittype unittype) {
    this.idMap = idMap;
    this.nameMap = nameMap;
    this.unittype = unittype;
    this.alwaysMap = new HashMap<>();
    this.displayableMap = new HashMap<>();
    this.searchableMap = new HashMap<>();
    for (Entry<Integer, UnittypeParameter> entry : idMap.entrySet()) {
      if (entry.getValue().getFlag().isAlwaysRead()) {
        alwaysMap.put(entry.getKey(), entry.getValue());
      }
      if (entry.getValue().getFlag().isDisplayable()) {
        displayableMap.put(entry.getKey(), entry.getValue());
      }
      if (entry.getValue().getFlag().isSearchable()) {
        searchableMap.put(entry.getKey(), entry.getValue());
      }
    }
  }

  protected void updateInternalMaps(UnittypeParameter utp) {
    if (utp.getFlag().isAlwaysRead()) {
      alwaysMap.put(utp.getId(), utp);
    }
    if (utp.getFlag().isDisplayable()) {
      displayableMap.put(utp.getId(), utp);
    }
    if (utp.getFlag().isSearchable()) {
      searchableMap.put(utp.getId(), utp);
    }
  }

  public UnittypeParameter getById(Integer id) {
    return idMap.get(id);
  }

  public UnittypeParameter getByName(String name) {
    return nameMap.get(name);
  }

  public Map<Integer, UnittypeParameter> getSearchableMap() {
    if (searchableMap != null) {
      return searchableMap;
    }
    return new HashMap<>();
  }

  public Map<Integer, UnittypeParameter> getDisplayableMap() {
    if (displayableMap != null) {
      return displayableMap;
    }
    return new HashMap<>();
  }

  public Map<Integer, UnittypeParameter> getAlwaysMap() {
    if (alwaysMap != null) {
      return alwaysMap;
    }
    return new HashMap<>();
  }

  public UnittypeParameter[] getUnittypeParameters() {
    UnittypeParameter[] upArr = new UnittypeParameter[nameMap.size()];
    nameMap.values().toArray(upArr);
    return upArr;
  }

  @Override
  public String toString() {
    return "Contains " + nameMap.size() + " unittype parameters";
  }

  public void addOrChangeUnittypeParameters(List<UnittypeParameter> unittypeParameters, ACS acs)
      throws SQLException {
    if (!acs.getUser().isUnittypeAdmin(unittype.getId())) {
      throw new IllegalArgumentException("Not allowed action for this user");
    }
    for (UnittypeParameter unittypeParameter : unittypeParameters) {
      if (unittypeParameter.getName().contains(",")
          || unittypeParameter.getName().contains("\"")
          || unittypeParameter.getName().contains("'")) {
        throw new IllegalArgumentException("Cannot use SQL specific characters in parameter name");
      }
    }
    addOrChangeUnittypeParameterImpl(unittypeParameters, unittype, acs);
    displayableNameMap = null;
    for (UnittypeParameter unittypeParameter : unittypeParameters) {
      nameMap.put(unittypeParameter.getName(), unittypeParameter);
      idMap.put(unittypeParameter.getId(), unittypeParameter);
      if (unittypeParameter.getOldName() != null) {
        nameMap.remove(unittypeParameter.getOldName());
        unittypeParameter.setOldName(null);
      }
      if (unittypeParameter.getFlag().isAlwaysRead()) {
        alwaysMap.put(unittypeParameter.getId(), unittypeParameter);
      }
      if (unittypeParameter.getFlag().isAlwaysRead()
          && !getAlwaysMap().containsKey(unittypeParameter.getId())) {
        alwaysMap.put(unittypeParameter.getId(), unittypeParameter);
      } else if (!unittypeParameter.getFlag().isAlwaysRead()
          && getAlwaysMap().containsKey(unittypeParameter.getId())) {
        alwaysMap.remove(unittypeParameter.getId());
      }

      if (unittypeParameter.getFlag().isDisplayable()) {
        displayableMap.put(unittypeParameter.getId(), unittypeParameter);
      }
      if (unittypeParameter.getFlag().isDisplayable()
          && !getDisplayableMap().containsKey(unittypeParameter.getId())) {
        displayableMap.put(unittypeParameter.getId(), unittypeParameter);
      } else if (!unittypeParameter.getFlag().isDisplayable()
          && getDisplayableMap().containsKey(unittypeParameter.getId())) {
        displayableMap.remove(unittypeParameter.getId());
      }

      if (unittypeParameter.getFlag().isSearchable()) {
        searchableMap.put(unittypeParameter.getId(), unittypeParameter);
      }
      if (unittypeParameter.getFlag().isSearchable()
          && !getSearchableMap().containsKey(unittypeParameter.getId())) {
        searchableMap.put(unittypeParameter.getId(), unittypeParameter);
      } else if (!unittypeParameter.getFlag().isSearchable()
          && getSearchableMap().containsKey(unittypeParameter.getId())) {
        searchableMap.remove(unittypeParameter.getId());
      }
    }
  }

  private Map<String, String> getUnittypeParameterNamesShort(
      Collection<UnittypeParameter> utParams) {
    Map<String, String> resultMap = new TreeMap<>(new NaturalComparator());
    for (UnittypeParameter outerEntry : utParams) {
      int counter;
      String[] utpNameArr = outerEntry.getName().split("\\.");
      String utpNamePart = "";
      for (int i = utpNameArr.length - 1; i >= 0; i--) {
        counter = 0;
        utpNamePart = "." + utpNameArr[i] + utpNamePart;
        for (UnittypeParameter innerEntry : utParams) {
          String utpName = "." + innerEntry.getName();
          if (utpName.endsWith(utpNamePart)) {
            counter++;
          }
        }
        if (counter == 1) {
          resultMap.put(outerEntry.getName(), utpNamePart.substring(1));
          break;
        }
      }
    }
    return resultMap;
  }

  public void addOrChangeUnittypeParameter(UnittypeParameter unittypeParameter, ACS acs)
      throws SQLException {
    if (!acs.getUser().isUnittypeAdmin(unittype.getId())) {
      throw new IllegalArgumentException("Not allowed action for this user");
    }
    List<UnittypeParameter> unittypeParameters = new ArrayList<>();
    unittypeParameters.add(unittypeParameter);
    addOrChangeUnittypeParameters(unittypeParameters, acs);
  }

  private void deleteUnittypeParameterValues(UnittypeParameter unittypeParameter, Statement s)
      throws SQLException {
    String sql = "DELETE FROM unit_type_param_value WHERE ";
    sql += "unit_type_param_id = " + unittypeParameter.getId();
    s.setQueryTimeout(60);
    s.executeUpdate(sql);

    logger.info("Deleted all unittype parameter values for utp:" + unittypeParameter.getName());
  }

  private void deleteUnittypeParameterImpl(
      List<UnittypeParameter> unittypeParameters, Unittype unittype, ACS acs) throws SQLException {
    Statement s = null;
    String sql;
    boolean wasAutoCommit;
    Connection c = acs.getDataSource().getConnection();
    wasAutoCommit = c.getAutoCommit();
    c.setAutoCommit(false);
    try {
      for (UnittypeParameter unittypeParameter : unittypeParameters) {
        s = c.createStatement();
        if (unittypeParameter.getValues() != null) {
          deleteUnittypeParameterValues(unittypeParameter, s);
        }
        sql = "DELETE FROM unit_type_param WHERE ";
        sql += "unit_type_param_id = " + unittypeParameter.getId();
        s.setQueryTimeout(60);
        int rowsDeleted = s.executeUpdate(sql);
        if (rowsDeleted > 0) {
          logger.info("Deleted unittype parameter " + unittypeParameter.getName());
        }
      }
      c.commit();
      c.setAutoCommit(true);
      if (acs.getDbi() != null) {
        acs.getDbi().publishChange(unittype, unittype);
      }
    } finally {
      if (s != null) {
        s.close();
      }
      c.setAutoCommit(wasAutoCommit);
      c.close();
    }
  }

  /**
   * The first time this method is run, the flag is set. The second time this method is run, the
   * parameter is removed from the name- and id-Map.
   */
  public void deleteUnittypeParameter(UnittypeParameter unittypeParameter, ACS acs)
      throws SQLException {
    if (!acs.getUser().isUnittypeAdmin(unittype.getId())) {
      throw new IllegalArgumentException("Not allowed action for this user");
    }
    List<UnittypeParameter> unittypeParameters = new ArrayList<>();
    unittypeParameters.add(unittypeParameter);
    deleteUnittypeParameters(unittypeParameters, acs);
  }

  public void deleteUnittypeParameters(List<UnittypeParameter> unittypeParameters, ACS acs)
      throws SQLException {
    if (!acs.getUser().isUnittypeAdmin(unittype.getId())) {
      throw new IllegalArgumentException("Not allowed action for this user");
    }
    deleteUnittypeParameterImpl(unittypeParameters, unittype, acs);
    for (UnittypeParameter unittypeParameter : unittypeParameters) {
      resetHasDeviceParameters();
      nameMap.remove(unittypeParameter.getName());
      idMap.remove(unittypeParameter.getId());
      alwaysMap.remove(unittypeParameter.getId());
      displayableMap.remove(unittypeParameter.getId());
      searchableMap.remove(unittypeParameter.getId());
      displayableNameMap = null;
    }
  }

  private void addOrChangeUnittypeParameterImpl(
      List<UnittypeParameter> unittypeParameters, Unittype unittype, ACS acs) throws SQLException {
    boolean wasAutoCommit;
    Connection c = acs.getDataSource().getConnection();
    wasAutoCommit = c.getAutoCommit();
    c.setAutoCommit(false);
    PreparedStatement ps = null;
    try {
      for (UnittypeParameter unittypeParameter : unittypeParameters) {
        DynamicStatement ds = new DynamicStatement();
        if (unittypeParameter.getFlag() == null) {
          throw new IllegalArgumentException(
              "The flag of unittypeParameter " + unittypeParameter + " is not correct.");
        }

        if (unittypeParameter.getId() == null) {
          ds.addSql("INSERT INTO unit_type_param (unit_type_id, name, flags) VALUES (?,?,?)");
          ds.addArguments(
              unittype.getId(), unittypeParameter.getName(), unittypeParameter.getFlag().getFlag());
          ps = ds.makePreparedStatement(c, "unit_type_param_id");
          ps.setQueryTimeout(60);
          ps.executeUpdate();
          ResultSet gk = ps.getGeneratedKeys();
          if (gk.next()) {
            unittypeParameter.setId(gk.getInt(1));
          }

          logger.info("Added unittype parameter " + unittypeParameter.getName());
        } else {
          ds.addSql("UPDATE unit_type_param SET ");
          ds.addSqlAndArguments("flags = ?, ", unittypeParameter.getFlag().getFlag());
          ds.addSqlAndArguments("name = ? ", unittypeParameter.getName());
          ds.addSqlAndArguments("WHERE unit_type_id = ? ", unittype.getId());
          ds.addSqlAndArguments("AND unit_type_param_id = ?", unittypeParameter.getId());
          ps = ds.makePreparedStatement(c);
          ps.setQueryTimeout(60);
          ps.executeUpdate();
        }
        if (unittypeParameter.getValues() != null) {
          if (!unittypeParameter.getValues().getValues().isEmpty()) {
            addOrChangeUnittypeParameterValues(unittypeParameter, ps);
          } else {
            deleteUnittypeParameterValues(unittypeParameter, ps);
          }
        }
      }
      logger.info("Added/changed " + unittypeParameters.size() + " unittype parameters");
      c.commit();
      c.setAutoCommit(true);
      if (acs.getDbi() != null) {
        acs.getDbi().publishChange(unittype, unittype);
      }
    } finally {
      if (ps != null) {
        ps.close();
      }
      c.setAutoCommit(wasAutoCommit);
      c.close();
    }
  }

  private void addOrChangeUnittypeParameterValues(
      UnittypeParameter unittypeParameter, PreparedStatement s) throws SQLException {
    String sql;
    sql = "DELETE FROM unit_type_param_value WHERE ";
    sql += "unit_type_param_id = " + unittypeParameter.getId();
    s.setQueryTimeout(60);
    int rowsDeleted = s.executeUpdate(sql);
    if (rowsDeleted > 0) {
      logger.info("Deleted all unittype parameter values for utp:" + unittypeParameter.getName());
    }
    UnittypeParameterValues values = unittypeParameter.getValues();
    if (values.getType().equals(UnittypeParameterValues.REGEXP) && values.getPattern() != null) {
      sql = "INSERT INTO unit_type_param_value ";
      sql += "(unit_type_param_id, value, priority, type) VALUES (";
      sql += unittypeParameter.getId() + ", '";
      String pattern = values.getPattern().toString().replaceAll("\\\\", "\\\\\\\\");
      sql += pattern + "', 1, '" + UnittypeParameterValues.REGEXP + "')";
      s.setQueryTimeout(60);
      s.executeUpdate(sql);
    } else if (values.getType().equals(UnittypeParameterValues.ENUM)
        && !values.getValues().isEmpty()) {
      for (int i = 0; i < values.getValues().size(); i++) {
        sql = "INSERT INTO unit_type_param_value ";
        sql += "(unit_type_param_id, value, priority, type) VALUES (";
        sql += unittypeParameter.getId() + ", '";
        sql += values.getValues().get(i) + "', " + i + ", '" + UnittypeParameterValues.ENUM + "')";
        s.setQueryTimeout(60);
        s.executeUpdate(sql);
      }

      logger.info("Added all unittype parameter values for utp:" + unittypeParameter.getName());
    }
  }

  public boolean hasDeviceParameters() {
    if (hasDeviceParameters == null) {
      for (String utpName : nameMap.keySet()) {
        if (!utpName.startsWith("System")) {
          hasDeviceParameters = true;
          return hasDeviceParameters;
        }
      }
      return false;
    }
    return hasDeviceParameters;
  }

  private void resetHasDeviceParameters() {
    this.hasDeviceParameters = null;
  }

  public Map<String, String> getDisplayableNameMap() {
    if (displayableNameMap == null) {
      displayableNameMap = getUnittypeParameterNamesShort(displayableMap.values());
    }
    return displayableNameMap;
  }
}
