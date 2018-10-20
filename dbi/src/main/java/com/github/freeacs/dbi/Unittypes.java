package com.github.freeacs.dbi;

import com.github.freeacs.dbi.InsertOrUpdateStatement.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Unittypes {
  private static Logger logger = LoggerFactory.getLogger(Unittypes.class);
  private Map<String, Unittype> nameMap;
  private Map<Integer, Unittype> idMap;

  public Unittypes(Map<String, Unittype> nameMap, Map<Integer, Unittype> idMap) {
    this.nameMap = nameMap;
    this.idMap = idMap;
  }

  public Unittype getByName(String name) {
    return nameMap.get(name);
  }

  /**
   * Only to be used internally (to shape ACS object according to permissions).
   *
   * @param unittype
   * @return
   */
  protected void removePermission(Unittype unittype) {
    nameMap.remove(unittype.getName());
    idMap.remove(unittype.getId());
  }

  public Unittype getById(Integer id) {
    return idMap.get(id);
  }

  public Unittype[] getUnittypes() {
    Unittype[] unittypes = new Unittype[nameMap.size()];
    nameMap.values().toArray(unittypes);
    return unittypes;
  }

  @Override
  public String toString() {
    return "Contains " + nameMap.size() + " unittypes (" + super.toString() + ")";
  }

  private void addOrChangeUnittypeImpl(Unittype unittype, ACS acs) throws SQLException {
    Connection c = acs.getDataSource().getConnection();
    PreparedStatement s = null;
    try {
      InsertOrUpdateStatement ious =
          new InsertOrUpdateStatement("unit_type", new Field("unit_type_id", unittype.getId()));
      ious.addField(new Field("unit_type_name", unittype.getName()));
      ious.addField(new Field("description", unittype.getDescription()));
      ious.addField(new Field("protocol", unittype.getProtocol().toString()));
      s = ious.makePreparedStatement(c);
      s.setQueryTimeout(60);
      s.executeUpdate();
      if (ious.isInsert()) {
        ResultSet gk = s.getGeneratedKeys();
        if (gk.next()) {
          unittype.setId(gk.getInt(1));
        }
        int changedSystemParameters = unittype.ensureValidSystemParameters(acs);
        logger.info(
            "Added unittype " + unittype.getName() + ", changed/added " + changedSystemParameters);
        if (acs.getDbi() != null) {
          acs.getDbi().publishAdd(unittype, unittype);
        }
      } else {
        int changedSystemParameters = unittype.ensureValidSystemParameters(acs);
        logger.info(
            "Updated unittype "
                + unittype.getName()
                + ", changed/added "
                + changedSystemParameters);
        if (acs.getDbi() != null) {
          acs.getDbi().publishChange(unittype, unittype);
        }
      }
    } finally {
      if (s != null) {
        s.close();
      }
      c.close();
    }
  }

  public void addOrChangeUnittype(Unittype unittype, ACS acs) throws SQLException {
    if ((unittype.getId() == null && !acs.getUser().isAdmin())
        || !acs.getUser().isUnittypeAdmin(unittype.getId())) {
      throw new IllegalArgumentException("Not allowed action for this user");
    }
    addOrChangeUnittypeImpl(unittype, acs);
    unittype.setAcs(acs);
    nameMap.put(unittype.getName(), unittype);
    idMap.put(unittype.getId(), unittype);
    if (unittype.getOldName() != null) {
      nameMap.remove(unittype.getOldName());
      unittype.setOldName(null);
    }
    Profiles profiles = unittype.getProfiles();
    if (profiles.getProfiles().length == 0) {
      profiles.addOrChangeProfile(new Profile("Default", unittype), acs);
    }
  }

  private int deleteUnittypeImpl(Unittype unittype, ACS acs) throws SQLException {
    Statement s = null;
    String sql;
    Connection c = acs.getDataSource().getConnection();
    try {
      s = c.createStatement();
      sql = "DELETE FROM unit_type WHERE ";
      sql += "unit_type_id = " + unittype.getId();
      s.setQueryTimeout(60);
      int rowsDeleted = s.executeUpdate(sql);

      logger.info("Deleted unittype " + unittype.getName());
      if (acs.getDbi() != null) {
        acs.getDbi().publishDelete(unittype, unittype);
      }
      return rowsDeleted;
    } finally {
      if (s != null) {
        s.close();
      }
      c.close();
    }
  }

  /**
   * The first time this method is run, the flag is set. The second time this method is run, the
   * parameter is removed from the nameMap. Setting the cascade argument = true will also delete all
   * unittype parameters and enumerations for all these parameters.
   *
   * @param unittype
   * @throws SQLException
   */
  public int deleteUnittype(Unittype unittype, ACS acs, boolean cascade) throws SQLException {
    if (!acs.getUser().isUnittypeAdmin(unittype.getId())) {
      throw new IllegalArgumentException("Not allowed action for this user");
    }
    if (cascade) {
      UnittypeParameters utParams = unittype.getUnittypeParameters();
      UnittypeParameter[] utParamsArr = utParams.getUnittypeParameters();
      Profile defaultProfile = unittype.getProfiles().getByName("Default");
      // Delete the defaultProfile if this is the only profile in existence and if the profile has
      // no profile parameters
      if (defaultProfile != null
          && unittype.getProfiles().getProfiles().length == 1
          && defaultProfile.getProfileParameters().getProfileParameters().length == 0) {
        unittype.getProfiles().deleteProfile(defaultProfile, acs, false);
      }
      utParams.deleteUnittypeParameters(Arrays.asList(utParamsArr), acs);
      Groups groups = unittype.getGroups();
      for (Group g : groups.getGroups()) {
        groups.deleteGroup(g, acs);
      }

      SyslogEvents syslogEvents = unittype.getSyslogEvents();
      for (SyslogEvent sg : syslogEvents.getSyslogEvents()) {
        if (sg.getUnittype() != null) {
          syslogEvents.deleteSyslogEventImpl(sg, acs);
        }
      }
    }
    int rowsDeleted = deleteUnittypeImpl(unittype, acs);
    nameMap.remove(unittype.getName());
    idMap.remove(unittype.getId());
    return rowsDeleted;
  }
}
