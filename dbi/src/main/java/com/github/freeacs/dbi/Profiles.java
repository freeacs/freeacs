package com.github.freeacs.dbi;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Profiles {
  private static Logger logger = LoggerFactory.getLogger(Profiles.class);
  private Map<String, Profile> nameMap;
  private Map<Integer, Profile> idMap;

  public Profiles(Map<Integer, Profile> idMap, Map<String, Profile> nameMap) {
    this.idMap = idMap;
    this.nameMap = nameMap;
  }

  public Profile getById(Integer id) {
    return idMap.get(id);
  }

  public Profile getByName(String name) {
    return nameMap.get(name);
  }

  public Profile[] getProfiles() {
    return nameMap.values().toArray(new Profile[] {});
  }

  @Override
  public String toString() {
    return "Contains " + nameMap.size() + " profiles";
  }

  public void addOrChangeProfile(Profile profile, ACS acs) throws SQLException {
    if (!acs.getUser().isProfileAdmin(profile.getUnittype().getId(), profile.getId())) {
      throw new IllegalArgumentException("Not allowed action for this user");
    }
    addOrChangeProfileImpl(profile, acs);
    nameMap.put(profile.getName(), profile);
    idMap.put(profile.getId(), profile);
    if (profile.getOldName() != null) {
      nameMap.remove(profile.getOldName());
      profile.setOldName(null);
    }
  }

  private int deleteProfileImpl(Profile profile, ACS acs) throws SQLException {
    Statement s = null;
    String sql;
    Connection c = acs.getDataSource().getConnection();
    try {
      s = c.createStatement();
      sql = "DELETE FROM profile WHERE ";
      sql += "profile_id = " + profile.getId();
      s.setQueryTimeout(60);
      int rowsDeleted = s.executeUpdate(sql);

      logger.info("Deleted profile " + profile.getName());
      if (acs.getDbi() != null) {
        acs.getDbi().publishDelete(profile, profile.getUnittype());
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
   * parameter is removed from the name- and id-Map.
   *
   * @throws SQLException
   */
  public int deleteProfile(Profile profile, ACS acs, boolean cascade) throws SQLException {
    if (!acs.getUser().isProfileAdmin(profile.getUnittype().getId(), profile.getId())) {
      throw new IllegalArgumentException("Not allowed action for this user");
    }
    if (cascade) {
      ProfileParameters pParams = profile.getProfileParameters();
      ProfileParameter[] pParamsArr = pParams.getProfileParameters();
      for (ProfileParameter pp : pParamsArr) {
        pParams.deleteProfileParameter(pp, acs);
      }
    }
    int rowsDeleted = deleteProfileImpl(profile, acs);
    nameMap.remove(profile.getName());
    idMap.remove(profile.getId());
    return rowsDeleted;
  }

  private void addOrChangeProfileImpl(Profile profile, ACS acs) throws SQLException {
    Statement s = null;
    String sql;
    Connection c = acs.getDataSource().getConnection();
    try {
      s = c.createStatement();
      if (profile.getId() == null) {
        sql = "INSERT INTO profile (unit_type_id, profile_name) VALUES (";
        sql += profile.getUnittype().getId() + ", ";
        sql += "'" + profile.getName() + "')";
        s.setQueryTimeout(60);
        s.executeUpdate(sql, new String[] {"profile_id"});
        ResultSet gk = s.getGeneratedKeys();
        if (gk.next()) {
          profile.setId(gk.getInt(1));
        }

        logger.info("Inserted profile " + profile.getName());
        if (acs.getDbi() != null) {
          acs.getDbi().publishAdd(profile, profile.getUnittype());
        }
      } else {
        sql = "UPDATE profile SET ";
        sql += "unit_type_id = " + profile.getUnittype().getId() + ", ";
        sql += "profile_name = '" + profile.getName() + "' ";
        sql += "WHERE profile_id = " + profile.getId();
        s.setQueryTimeout(60);
        s.executeUpdate(sql);

        logger.info("Updated profile " + profile.getName());
        if (acs.getDbi() != null) {
          acs.getDbi().publishChange(profile, profile.getUnittype());
        }
      }
    } finally {
      if (s != null) {
        s.close();
      }
      c.close();
    }
  }

  protected Map<String, Profile> getNameMap() {
    return nameMap;
  }

  protected Map<Integer, Profile> getIdMap() {
    return idMap;
  }

  /** Only to be used internally (to shape ACS object according to permissions). */
  protected void removePermission(Profile profile) {
    nameMap.remove(profile.getName());
    idMap.remove(profile.getId());
  }
}
