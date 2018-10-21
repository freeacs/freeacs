package com.github.freeacs.dbi;

import com.github.freeacs.dbi.DynamicStatement.NullInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Groups {
  private static Logger logger = LoggerFactory.getLogger(Groups.class);
  private Map<String, Group> nameMap;
  private Map<Integer, Group> idMap;
  private Unittype unittype;

  public Groups(Map<Integer, Group> idMap, Map<String, Group> nameMap, Unittype unittype) {
    this.idMap = idMap;
    this.nameMap = nameMap;
    this.unittype = unittype;
  }

  public Group getById(Integer id) {
    return idMap.get(id);
  }

  public Group getByName(String name) {
    return nameMap.get(name);
  }

  /** Returns all groups. */
  public Group[] getGroups() {
    Group[] groups = new Group[nameMap.size()];
    nameMap.values().toArray(groups);
    return groups;
  }

  @Override
  public String toString() {
    return "Contains " + nameMap.size() + " groups";
  }

  protected static void checkPermission(Group group, ACS acs) {
    if (group.getTopParent().getProfile() == null) {
      if (!acs.getUser().isUnittypeAdmin(group.getUnittype().getId())) {
        throw new IllegalArgumentException("Not allowed action for this user");
      }
    } else if (!acs.getUser()
        .isProfileAdmin(group.getUnittype().getId(), group.getTopParent().getProfile().getId())) {
      throw new IllegalArgumentException("Not allowed action for this user");
    }
  }

  public void addOrChangeGroup(Group group, ACS acs) throws SQLException {
    checkPermission(group, acs);
    addOrChangeGroupImpl(group, acs);
    group.setUnittype(unittype);
    nameMap.put(group.getName(), group);
    idMap.put(group.getId(), group);
    if (group.getOldName() != null) {
      nameMap.remove(group.getOldName());
      group.setOldName(null);
    }
  }

  public List<Group> getTopLevelGroups() {
    Group[] allGroups = getGroups();
    List<Group> topLevelGroups = new ArrayList<>();
    for (Group g : allGroups) {
      if (g.getParent() == null) {
        topLevelGroups.add(g);
      }
    }
    return topLevelGroups;
  }

  /** Only used to refresh the cache, used from DBI. */
  private static void refreshGroupParameter(Group group, Connection c) throws SQLException {
    Statement s = null;
    ResultSet rs = null;
    String sql = null;
    try {
      sql = "SELECT * FROM group_param WHERE group_id = " + group.getId();
      s = c.createStatement();
      s.setQueryTimeout(60);
      rs = s.executeQuery(sql);
      Set<Integer> groupParamIdSet = new HashSet<>();
      while (rs.next()) {
        Integer unittypeParamId = rs.getInt("unit_type_param_id");
        logger.info(
            "refreshGroupParameter: Group: " + group + ", unittypeParamId: " + unittypeParamId);
        if (group != null) {
          logger.info("refreshGroupParameter: Group.getUnittype(): " + group.getUnittype());
        }
        if (group.getUnittype() != null) {
          logger.info(
              "refreshGroupParameter: Group.getUnittype().getUnittypeParameters(): "
                  + group.getUnittype().getUnittypeParameters());
        }
        if (group.getUnittype().getUnittypeParameters() != null) {
          logger.info(
              "refreshGroupParameter: Group.getUnittype().getUnittypeParameters().getById(utpId): "
                  + group.getUnittype().getUnittypeParameters().getById(unittypeParamId));
        }
        UnittypeParameter utp =
            group.getUnittype().getUnittypeParameters().getById(unittypeParamId);
        String value = rs.getString("value");
        Integer groupParamId;
        groupParamId = rs.getInt("id");
        Parameter.Operator op = Parameter.Operator.getOperator(rs.getString("operator"));
        Parameter.ParameterDataType pdt =
            Parameter.ParameterDataType.getDataType(rs.getString("data_type"));
        groupParamIdSet.add(groupParamId);
        Parameter parameter = new Parameter(utp, value, op, pdt);
        GroupParameter groupParameter = new GroupParameter(parameter, group);
        groupParameter.setId(groupParamId);
        GroupParameters groupParams = group.getGroupParameters();
        groupParams.addOrChangeGroupParameter(groupParameter);
      }
      // Find out if any group parameter has been deleted
      GroupParameters groupParams = group.getGroupParameters();
      for (GroupParameter gp : group.getGroupParameters().getGroupParameters()) {
        if (!groupParamIdSet.contains(gp.getId())) {
          groupParams.deleteGroupParameter(gp);
        }
      }
    } finally {
      if (rs != null) {
        rs.close();
      }
      if (s != null) {
        s.close();
      }
    }
  }

  /** Only used to refresh the cache, used from DBI. */
  protected static void refreshGroup(Integer groupId, ACS acs) throws SQLException {
    ResultSet rs = null;
    PreparedStatement ps = null;
    Connection c = acs.getDataSource().getConnection();
    try {
      DynamicStatement ds = new DynamicStatement();
      ds.addSqlAndArguments(
          "SELECT unit_type_id, group_name, description, parent_group_id, profile_id, count FROM group_ WHERE group_id = ?",
          groupId);
      ps = ds.makePreparedStatement(c);
      rs = ps.executeQuery();
      if (rs.next()) {
        Unittype unittype = acs.getUnittype(rs.getInt(1));
        if (unittype == null) {
          return; // The unittype is not accessible for this user
        }
        Group group = unittype.getGroups().getById(groupId);
        if (group == null) {
          return; // The group is not accessible for this user
        }
        group.setName(rs.getString("group_name"));
        group.setDescription(rs.getString("description"));
        Integer parentGroupId = rs.getInt("parent_group_id");
        if (parentGroupId != null) {
          group.setParent(unittype.getGroups().getById(parentGroupId));
        } else {
          group.setParent(null);
        }
        group.setProfile(unittype.getProfiles().getById(rs.getInt("profile_id")));
        group.setCount(rs.getInt("count"));
        group.setUnittype(unittype);
        refreshGroupParameter(group, c);
        logger.debug("Refreshed group " + group);
      }
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

  private void deleteGroupImpl(Group group, ACS acs) throws SQLException {
    PreparedStatement s = null;
    String sql;
    Connection c = acs.getDataSource().getConnection();
    try {
      sql = "UPDATE group_ SET parent_group_id = ?, profile_id = ? WHERE parent_group_id = ?";
      s = c.prepareStatement(sql);
      if (group.getParent() != null) {
        s.setInt(1, group.getParent().getId());
      } else {
        s.setNull(1, Types.INTEGER);
      }
      if (group.getProfile() != null) {
        s.setInt(2, group.getProfile().getId());
      } else {
        s.setNull(2, Types.INTEGER);
      }
      s.setInt(3, group.getId());

      s.setQueryTimeout(60);
      int rowsAffected = s.executeUpdate();
      s.close();
      logger.info(
          "Updated "
              + rowsAffected
              + " childgroups of group "
              + group
              + " with either a new parent or no parent");
      try (Statement delStmt = c.createStatement()) {
        sql = "DELETE FROM group_ WHERE group_id = " + group.getId();
        delStmt.setQueryTimeout(60);
        delStmt.executeUpdate(sql);
      }
      logger.info("Deleted group " + group);
      if (acs.getDbi() != null) {
        acs.getDbi().publishDelete(group, group.getUnittype());
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
   * @throws SQLException
   */
  public void deleteGroup(Group group, ACS acs) throws SQLException {
    checkPermission(group, acs);
    for (GroupParameter gp : group.getGroupParameters().getGroupParameters()) {
      group.getGroupParameters().deleteGroupParameter(gp, acs);
    }
    deleteGroupImpl(group, acs);
    removeGroupFromDataModel(group);
  }

  protected void removeGroupFromDataModel(Group group) {
    Group parent = group.getParent();
    for (Group child : group.getChildren()) {
      child.setParentFromDelete(parent);
      if (parent != null) {
        parent.addChild(child);
      }
      child.setProfileFromDelete(group.getProfile());
    }
    if (parent != null) {
      parent.removeChild(group);
    }
    nameMap.remove(group.getName());
    idMap.remove(group.getId());
  }

  public Unittype getUnittype() {
    return unittype;
  }

  private void addOrChangeGroupImpl(Group group, ACS acs) throws SQLException {
    PreparedStatement s = null;
    if (group.getParent() != null && group.getId() == null) {
      addOrChangeGroup(group.getParent(), acs);
    }
    Connection c = acs.getDataSource().getConnection();
    try {
      if (group.getId() == null) {
        DynamicStatement ds = new DynamicStatement();
        ds.addSqlAndArguments(
            "INSERT INTO group_ (group_name, unit_type_id",
            group.getName(),
            group.getUnittype().getId());
        if (group.getDescription() != null) {
          ds.addSqlAndArguments(", description", group.getDescription());
        }
        if (group.getParent() != null) {
          ds.addSqlAndArguments(", parent_group_id", group.getParent().getId());
        }
        if (group.getProfile() != null) {
          ds.addSqlAndArguments(", profile_id", group.getProfile().getId());
        }
        if (group.getCount() != null) {
          ds.addSqlAndArguments(", count", group.getCount());
        }
        ds.setSql(ds.getSql() + ") VALUES (" + ds.getQuestionMarks() + ")");
        s = ds.makePreparedStatement(c, "group_id");
        s.setQueryTimeout(60);
        s.executeUpdate();
        ResultSet gk = s.getGeneratedKeys();
        if (gk.next()) {
          group.setId(gk.getInt(1));
        }
        s.close();
        logger.info("Inserted group " + group.getName());
        if (acs.getDbi() != null) {
          acs.getDbi().publishAdd(group, group.getUnittype());
        }
      } else {
        DynamicStatement ds = new DynamicStatement();
        ds.addSqlAndArguments("UPDATE group_ SET group_name = ?, ", group.getName());
        ds.addSqlAndArguments("description = ?, ", group.getDescription());
        if (group.getParent() != null) {
          ds.addSqlAndArguments("parent_group_id  = ?, ", group.getParent().getId());
        } else {
          ds.addSqlAndArguments("parent_group_id  = ?, ", new NullInteger());
        }
        if (group.getCount() != null) {
          ds.addSqlAndArguments("count = ?, ", group.getCount());
        }
        if (group.getProfile() != null) {
          ds.addSqlAndArguments("profile_id = ? ", group.getProfile().getId());
        } else {
          ds.addSqlAndArguments("profile_id = ? ", new NullInteger());
        }
        ds.addSqlAndArguments("WHERE group_id = ?", group.getId());
        PreparedStatement ps = ds.makePreparedStatement(c);
        ps.setQueryTimeout(60);
        ps.executeUpdate();

        logger.info("Updated group " + group.getName());
        if (acs.getDbi() != null) {
          acs.getDbi().publishChange(group, group.getUnittype());
        }
      }
    } finally {
      if (s != null) {
        s.close();
      }
      c.close();
    }
  }

  protected Map<String, Group> getNameMap() {
    return nameMap;
  }

  protected Map<Integer, Group> getIdMap() {
    return idMap;
  }
}
