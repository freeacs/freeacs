package com.github.freeacs.dbi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.sql.DataSource;

public class Permissions {
  /**
   * Generated Tue May 24 08:48:14 CEST 2011 public static final String[] WEB_PAGES = { "search",
   * "unit", "profile", "unittype", "group", "job", "software", "syslog", "report", "monitor",
   * "staging" };
   */
  public static final String[] WEB_PAGES = {
    "support", "limited-provisioning", "full-provisioning", "report", "staging", "monitor"
  };

  private DataSource dataSource;
  /** Private User user;. */
  private Map<Integer, Permission> idMap = new HashMap<>();
  /** A unittype-id maps to a set of permission-ids. */
  private Map<Integer, Set<Integer>> unittypeIdMap = new TreeMap<>();

  protected Permissions(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public Permission[] getPermissions() {
    Permission[] permissions = new Permission[idMap.size()];
    int counter = 0;
    if (permissions.length > 0) {
      for (Set<Integer> permSet : unittypeIdMap.values()) {
        for (Integer permId : permSet) {
          permissions[counter++] = idMap.get(permId);
        }
      }
    }
    return permissions;
  }

  public Permission getByUnittypeProfile(Integer unittypeId, Integer profileId) {
    Set<Integer> permissionIdSet = unittypeIdMap.get(unittypeId);
    if (permissionIdSet == null) {
      return null;
    }

    for (Integer permissionId : permissionIdSet) {
      Permission p = getById(permissionId);
      if (p.getProfileId() == null && profileId == null) {
        return p;
      }
      if (p.getProfileId() == null || profileId == null) {
        continue;
      }
      if (p.getProfileId().intValue() == profileId) {
        return p;
      }
    }
    return null;
  }

  public Permission getById(Integer id) {
    return idMap.get(id);
  }

  protected void delete(Permission permission) throws SQLException {
    Connection c = null;
    PreparedStatement ps = null;
    SQLException sqle = null;
    try {
      c = dataSource.getConnection();
      DynamicStatement ds = new DynamicStatement();
      ds.addSqlAndArguments("DELETE FROM permission_ WHERE id = ?", permission.getId());
      ps = ds.makePreparedStatement(c);
      ps.executeUpdate();
    } catch (SQLException sqlex) {
      sqle = sqlex;
      throw sqlex;
    } finally {
      if (ps != null) {
        ps.close();
      }
      if (c != null) {
        c.close();
      }
    }
    idMap.remove(permission.getId());
    Set<Integer> permissionIdSet = unittypeIdMap.get(permission.getUnittypeId());
    if (permissionIdSet != null) {
      permissionIdSet.remove(permission.getId());
    }
  }

  protected void add(Permission permission) {
    idMap.put(permission.getId(), permission);
    Set<Integer> permissionIdSet = unittypeIdMap.get(permission.getUnittypeId());
    if (permissionIdSet == null) {
      permissionIdSet = new TreeSet<>();
    }
    permissionIdSet.add(permission.getId());
    unittypeIdMap.put(permission.getUnittypeId(), permissionIdSet);
  }

  private void checkAmbigiousPermissions(Permission p) {
    Set<Integer> utperms = unittypeIdMap.get(p.getUnittypeId());
    if (utperms == null) {
      return;
    }
    if (p.getProfileId() == null) {
      if (utperms.size() > 1 || (utperms.size() == 1 && !utperms.contains(p.getId()))) {
        throw new IllegalArgumentException(
            "Cannot create a new Unit Type permission, since a Profile permission already exists");
      }
    } else if (utperms.size() == 1) {
      Permission existingPermission = idMap.get(utperms.toArray()[0]);
      if (existingPermission.getProfileId() == null) {
        throw new IllegalArgumentException(
            "Cannot create a new Profile permission, since a Unit Type permission already exists");
      }
    }
  }

  protected void addOrChange(Permission permission) throws SQLException {
    checkAmbigiousPermissions(
        permission); // can throw IllegalArgumentException containg error message
    Connection c = null;
    PreparedStatement ps = null;
    try {
      c = dataSource.getConnection();
      DynamicStatement ds = new DynamicStatement();
      if (permission.getId() == null) {
        ds.addSqlAndArguments("INSERT INTO permission_ (user_id, ", permission.getUser().getId());
        if (permission.getProfileId() != null) {
          ds.addSqlAndArguments("profile_id, ", permission.getProfileId());
        }
        ds.addSqlAndArguments("unit_type_id) ", permission.getUnittypeId());
        ds.addSql("VALUES (" + ds.getQuestionMarks() + ")");
        ps = ds.makePreparedStatement(c, "id");
        ps.executeUpdate();
        ResultSet gk = ps.getGeneratedKeys();
        if (gk.next()) {
          permission.setId(gk.getInt(1));
        }
        add(permission); // handles the memory model
      } else {
        ds.addSqlAndArguments("UPDATE permission_ SET user_id = ?, ", permission.getUser().getId());
        if (permission.getProfileId() != null) {
          ds.addSqlAndArguments("profile_id = ?, ", permission.getProfileId());
        }
        ds.addSqlAndArguments(
            "unit_type_id = ? WHERE id = ?", permission.getUnittypeId(), permission.getId());
        ps = ds.makePreparedStatement(c);
        int rowsUpdated = ps.executeUpdate();
        if (rowsUpdated > 0) {
          add(permission);
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
}
