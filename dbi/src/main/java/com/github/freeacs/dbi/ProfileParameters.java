package com.github.freeacs.dbi;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProfileParameters {
  private static Logger logger = LoggerFactory.getLogger(ProfileParameters.class);
  private Map<String, ProfileParameter> nameMap;
  private Map<Integer, ProfileParameter> idMap;
  private Profile profile;

  public ProfileParameters(
      Map<Integer, ProfileParameter> idMap,
      Map<String, ProfileParameter> nameMap,
      Profile profile) {
    this.idMap = idMap;
    this.nameMap = nameMap;
    this.profile = profile;
  }

  public ProfileParameter getByName(String name) {
    return nameMap.get(name);
  }

  public ProfileParameter getById(Integer id) {
    return idMap.get(id);
  }

  @Override
  public String toString() {
    return "Contains " + nameMap.size() + " profile parameters";
  }

  public ProfileParameter[] getProfileParameters() {
    ProfileParameter[] pArr = new ProfileParameter[nameMap.size()];
    nameMap.values().toArray(pArr);
    return pArr;
  }

  private void addOrChangeProfileParameterImpl(
      ProfileParameter profileParameter, Profile profile, ACS acs) throws SQLException {
    Connection c = acs.getDataSource().getConnection();
    Statement s = null;
    String sql;
    try {
      s = c.createStatement();
      String logMsg = "profile parameter " + profileParameter.getUnittypeParameter().getName();
      if (profileParameter.getUnittypeParameter().getFlag().isConfidential()) {
        logMsg += " with confidential value (*****)";
      } else {
        logMsg += " with value " + profileParameter.getValue();
      }
      if (getById(profileParameter.getUnittypeParameter().getId()) == null) {
        sql = "INSERT INTO profile_param (profile_id, unit_type_param_id, value) VALUES (";
        sql += profileParameter.getProfile().getId() + ", ";
        sql += profileParameter.getUnittypeParameter().getId() + ", ";
        sql += "'" + profileParameter.getValue() + "')";
        s.setQueryTimeout(60);
        s.executeUpdate(sql);

        logger.info("Added " + logMsg);
        if (acs.getDbi() != null) {
          acs.getDbi().publishAdd(profileParameter, profile.getUnittype());
        }
      } else {
        sql = "UPDATE profile_param SET ";
        sql += "VALUE = '" + profileParameter.getValue() + "' ";
        sql += "WHERE profile_id = " + profileParameter.getProfile().getId() + " AND ";
        sql += "unit_type_param_id = " + profileParameter.getUnittypeParameter().getId();
        s.setQueryTimeout(60);
        s.executeUpdate(sql);

        logger.info("Updated " + logMsg);
        if (acs.getDbi() != null) {
          acs.getDbi().publishChange(profileParameter, profile.getUnittype());
        }
      }
    } finally {
      if (s != null) {
        s.close();
      }
      c.close();
    }
  }

  public void addOrChangeProfileParameter(ProfileParameter profileParameter, ACS acs)
      throws SQLException {
    if (!acs.getUser().isProfileAdmin(profile.getUnittype().getId(), profile.getId())) {
      throw new IllegalArgumentException("Not allowed action for this user");
    }
    //		if (profileParameter.getUnittypeParameter().getFlag().isInspection())
    //			throw new IllegalArgumentException("The unit type parameter is an inspection parameter -
    // cannot be set on a profile");
    addOrChangeProfileParameterImpl(profileParameter, profile, acs);
    nameMap.put(profileParameter.getUnittypeParameter().getName(), profileParameter);
    idMap.put(profileParameter.getUnittypeParameter().getId(), profileParameter);
  }

  private void deleteProfileParameterImpl(
      ProfileParameter profileParameter, Profile profile, ACS acs) throws SQLException {
    Statement s = null;
    String sql = null;
    Connection c = acs.getDataSource().getConnection();
    try {
      s = c.createStatement();
      sql = "DELETE FROM profile_param WHERE ";
      sql += "profile_id = " + profileParameter.getProfile().getId() + " AND ";
      sql += "unit_type_param_id = " + profileParameter.getUnittypeParameter().getId();
      s.setQueryTimeout(60);
      s.executeUpdate(sql);

      logger.info("Deleted profile parameter " + profileParameter.getUnittypeParameter().getName());
      if (acs.getDbi() != null) {
        acs.getDbi().publishDelete(profileParameter, profile.getUnittype());
      }
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
   * @param profileParameter
   * @throws SQLException
   */
  public void deleteProfileParameter(ProfileParameter profileParameter, ACS acs)
      throws SQLException {
    if (!acs.getUser().isProfileAdmin(profile.getUnittype().getId(), profile.getId())) {
      throw new IllegalArgumentException("Not allowed action for this user");
    }
    deleteProfileParameterImpl(profileParameter, profile, acs);
    nameMap.remove(profileParameter.getUnittypeParameter().getName());
    idMap.remove(profileParameter.getId());
  }
}
