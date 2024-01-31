package com.github.freeacs.dbi;

import com.github.freeacs.common.util.NumberComparator;
import com.github.freeacs.dbi.Unittype.ProvisioningProtocol;
import com.github.freeacs.dbi.util.ACSVersionCheck;
import com.github.freeacs.dbi.util.MapWrapper;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The ACS class is the main interface for the following tables/concepts in the ACS database:
 *
 * <p>Unittype, UnittypeParameter, UnittypeParameterValues, Profile, ProfileParameter Group,
 * GroupParameter, Job, JobParameter, SyslogEvent, Filetore, Files, Certificates
 *
 * <p>All the information stored within these tables/objects can be retrieved from ACS, although you
 * need to start with the Unittype, and work you way down the object-tree to find the various
 * information.
 *
 * <p>The ACS object should always be retrieved from the DBI class, because the DBI takes care of
 * synching information about changes from other ACS objects in other modules/threads etc.
 *
 * <p>What is not covered in ACS:
 *
 * <p>a) To access information about Users/Permissions, you need to start with the Users-class. b)
 * To access information about Units/UnitParameters, you need to start with the ACSUnit class c) To
 * access information about Syslog, you need to start with the Syslog class
 *
 * @author Morten S
 */
public class ACS {
  private static Logger logger = LoggerFactory.getLogger(ACS.class);
  private static boolean strictOrder = true;

  private final DataSource dataSource;

  private Unittypes unittypes;

  private DBI dbi;

  protected Syslog syslog;

  private ScriptExecutions scriptExecutions;

  public ACS(DataSource dataSource, Syslog syslog) throws SQLException {
    long start = System.currentTimeMillis();
    this.dataSource = dataSource;
    this.syslog = syslog;
    /* Checks all necessary tables to see which version they're in */
    ACSVersionCheck.versionCheck(dataSource);
    this.unittypes = read();
    if (logger.isDebugEnabled()) {
      logger.debug("Read ACS object in " + (System.currentTimeMillis() - start) + " ms.");
    }
  }

  public User getUser() {
    return syslog.getIdentity().getUser();
  }

  public Users getUsers() {
    return getUser().getUsers();
  }

  public ScriptExecutions getScriptExecutions() {
    if (scriptExecutions == null) {
      scriptExecutions = new ScriptExecutions(getDataSource());
    }
    return scriptExecutions;
  }

  /**
   * The permissions will be applied in this method, and all unittypes/profile filtered through the
   * method. 0. If admin permission, return all objects (including certificates 1. If no unittype
   * permission or profile permissions for a unittype, remove unittype 2. If no unittype permission,
   * but profile permission exists, remove some objects from unittype and return profile
   *
   * @return
   * @throws SQLException
   */
  public Unittypes read() throws SQLException {
    unittypes = readAsAdmin();
    logger.debug("Updated ACS object, read " + unittypes.getUnittypes().length + " unittypes");
    User user = syslog.getIdentity().getUser();
    if (user.isAdmin()) {
      return unittypes;
    }
    for (Unittype unittype : unittypes.getUnittypes()) {
      boolean isUnittypeAdmin = user.isUnittypeAdmin(unittype.getId());
      if (!isUnittypeAdmin) {
        for (Profile profile : unittype.getProfiles().getProfiles()) {
          if (user.isProfileAdmin(
              unittype.getId(), profile.getId())) { // remove objects not related to profile
            unittype.removeObjects(profile);
          } else { // remove the whole profile
            unittype.getProfiles().removePermission(profile);
          }
        }
        if (unittype.getProfiles().getProfiles().length == 0) { // remove the whole unittype
          unittypes.removePermission(unittype);
        }
      }
    }
    return unittypes;
  }

  private Unittypes readAsAdmin() throws SQLException {
    boolean wasAutoCommit = false;
    Connection connection = null;
    try {
      connection = getDataSource().getConnection();
      wasAutoCommit = connection.getAutoCommit();
      connection.setAutoCommit(false);
      Unittypes tmpUnittypes = readUnittypes();
      readFilestore(tmpUnittypes);
      readUnittypeParameters(tmpUnittypes);
      readUnittypeParameterValues(tmpUnittypes);
      readProfiles(tmpUnittypes);
      readGroups(tmpUnittypes);
      if (ACSVersionCheck.heartbeatSupported) {
        readHeartbeats(tmpUnittypes);
      }
      readSyslogEvents(tmpUnittypes);
      readProfileParameters(tmpUnittypes);
      readGroupParameters(tmpUnittypes);
      readJobs(tmpUnittypes);
      if (ACSVersionCheck.triggerSupported) {
        readTriggers(tmpUnittypes);
      }
      return tmpUnittypes;
    } finally {
      if (connection != null) {
        connection.setAutoCommit(wasAutoCommit);
        connection.close();
      }
    }
  }

  private void readGroupParameters(Unittypes unittypes) throws SQLException {
    Statement s = null;
    ResultSet rs = null;
    String sql;
    boolean wasAutoCommit = false;
    Connection connection = null;
    try {
      sql = "SELECT ut.unit_type_name, ";
      sql += "gp.id, ";
      sql += "gp.group_id, gp.unit_type_param_id, ";
      sql += "gp.operator, gp.data_type, ";
      sql += "gp.value ";
      sql += " FROM group_param gp, unit_type_param utp, unit_type ut ";
      sql +=
          " WHERE gp.unit_type_param_id = utp.unit_type_param_id AND utp.unit_type_id = ut.unit_type_id ORDER BY ut.unit_type_name ASC";
      connection = getDataSource().getConnection();
      wasAutoCommit = connection.getAutoCommit();
      connection.setAutoCommit(false);
      s = connection.createStatement();
      s.setQueryTimeout(60);
      rs = s.executeQuery(sql);
      int counter = 0;
      while (rs.next()) {
        counter++;
        Unittype unittype = unittypes.getByName(rs.getString("unit_type_name"));
        Group group = unittype.getGroups().getById(rs.getInt("group_id"));
        Integer unittypeParamId = rs.getInt("gp.unit_type_param_id");
        UnittypeParameter utp =
            group.getUnittype().getUnittypeParameters().getById(unittypeParamId);
        String value = rs.getString("gp.value");
        Parameter.Operator op = Parameter.Operator.getOperator(rs.getString("operator"));
        Parameter.ParameterDataType pdt =
            Parameter.ParameterDataType.getDataType(rs.getString("data_type"));
        Parameter parameter = new Parameter(utp, value, op, pdt);
        GroupParameter groupParameter = new GroupParameter(parameter, group);
        groupParameter.setId(rs.getInt("gp.id"));
        GroupParameters groupParams = group.getGroupParameters();
        groupParams.addOrChangeGroupParameter(groupParameter);
      }
      if (logger.isDebugEnabled()) {
        logger.debug("Read " + counter + " group parameters");
      }
    } finally {
      if (rs != null) {
        rs.close();
      }
      if (s != null) {
        s.close();
      }
      if (connection != null) {
        connection.setAutoCommit(wasAutoCommit);
        connection.close();
      }
    }
  }

  private void readProfileParameters(Unittypes unittypes) throws SQLException {
    Statement s = null;
    ResultSet rs = null;
    String sql;
    boolean wasAutoCommit = false;
    Connection connection = null;
    try {
      Map<String, ProfileParameter> nameMap = null;
      Map<Integer, ProfileParameter> idMap = null;
      Profile lastProfile = null;
      sql =
          "SELECT utp.unit_type_id, pm.profile_id, pm.unit_type_param_id, pm.value FROM profile_param pm, unit_type_param utp WHERE pm.unit_type_param_id = utp.unit_type_param_id ORDER BY utp.unit_type_id ASC, pm.profile_id ASC";
      connection = getDataSource().getConnection();
      wasAutoCommit = connection.getAutoCommit();
      connection.setAutoCommit(false);
      s = connection.createStatement();
      s.setQueryTimeout(60);
      rs = s.executeQuery(sql);
      int counter = 0;
      while (rs.next()) {
        counter++;
        Unittype unittype = unittypes.getById(rs.getInt("unit_type_id"));
        Profile profile = unittype.getProfiles().getById(rs.getInt("profile_id"));
        Integer unittypeParamId = rs.getInt("unit_type_param_id");
        String value = rs.getString("value");
        if (value == null) {
          value = "";
        }
        UnittypeParameter unittypeParameter =
            unittype.getUnittypeParameters().getById(unittypeParamId);
        ProfileParameter profileParameter = new ProfileParameter(profile, unittypeParameter, value);
        if (lastProfile == null || lastProfile != profile) {
          nameMap = new MapWrapper<ProfileParameter>(isStrictOrder()).getMap();
          idMap = new HashMap<>();
          profile.setProfileParameters(new ProfileParameters(idMap, nameMap, profile));
          lastProfile = profile;
        }
        nameMap.put(unittypeParameter.getName(), profileParameter);
        idMap.put(unittypeParamId, profileParameter);
      }
      if (logger.isDebugEnabled()) {
        logger.debug("Read " + counter + " profile parameters");
      }
    } finally {
      if (rs != null) {
        rs.close();
      }
      if (s != null) {
        s.close();
      }
      if (connection != null) {
        connection.setAutoCommit(wasAutoCommit);
        connection.close();
      }
    }
  }

  private void readUnittypeParameterValues(Unittypes unittypes) throws SQLException {
    Statement s = null;
    ResultSet rs = null;
    String sql;
    boolean wasAutoCommit = false;
    Connection connection = null;
    try {
      sql = "SELECT utp.unit_type_id, utpv.unit_type_param_id, value, priority, type ";
      sql += "FROM unit_type_param_value utpv, unit_type_param utp ";
      sql += "WHERE utpv.unit_type_param_id = utp.unit_type_param_id ";
      sql += "ORDER BY utp.unit_type_id ASC, utpv.unit_type_param_id, utpv.priority ASC";
      connection = getDataSource().getConnection();
      wasAutoCommit = connection.getAutoCommit();
      connection.setAutoCommit(false);
      s = connection.createStatement();
      s.setQueryTimeout(60);
      rs = s.executeQuery(sql);
      UnittypeParameterValues values = null;
      Integer lastUnittypeParameterId = null;
      int counter = 0;
      while (rs.next()) {
        counter++;
        Unittype unittype = unittypes.getById(rs.getInt("unit_type_id"));
        Integer utpId = rs.getInt("unit_type_param_id");
        if (lastUnittypeParameterId == null || !lastUnittypeParameterId.equals(utpId)) {
          UnittypeParameter up = unittype.getUnittypeParameters().getById(utpId);
          values = new UnittypeParameterValues();
          up.setValuesFromACS(values);
          lastUnittypeParameterId = utpId;
        }
        String type = rs.getString("type");
        String value = rs.getString("value");
        values.setType(type);
        if (type.equals(UnittypeParameterValues.REGEXP)) {
          values.setPattern(value);
        } else if (type.equals(UnittypeParameterValues.ENUM)) {
          values.getValues().add(value);
        }
      }
      if (logger.isDebugEnabled()) {
        logger.debug("Read " + counter + " unittype parameter values");
      }
    } finally {
      if (rs != null) {
        rs.close();
      }
      if (s != null) {
        s.close();
      }
      if (connection != null) {
        connection.setAutoCommit(wasAutoCommit);
        connection.close();
      }
    }
  }

  private void readUnittypeParameters(Unittypes unittypes) throws SQLException {
    Statement s = null;
    ResultSet rs = null;
    String sql;
    boolean wasAutoCommit = false;
    Connection connection = null;
    try {
      Map<Integer, UnittypeParameter> idMap = null;
      Map<String, UnittypeParameter> nameMap = null;
      Unittype lastUnittype = null;
      sql =
          "SELECT unit_type_id, unit_type_param_id, name, flags FROM unit_type_param ORDER BY unit_type_id ASC";
      connection = getDataSource().getConnection();
      wasAutoCommit = connection.getAutoCommit();
      connection.setAutoCommit(false);
      s = connection.createStatement();
      s.setQueryTimeout(60);
      rs = s.executeQuery(sql);
      int counter = 0;
      while (rs.next()) {
        counter++;
        Unittype unittype = unittypes.getById(rs.getInt("unit_type_id"));
        Integer unittypeParamId = rs.getInt("unit_type_param_id");
        String name = rs.getString("name");
        UnittypeParameterFlag unittypeParameterFlag =
            new UnittypeParameterFlag(rs.getString("flags"), true);
        UnittypeParameter unittypeParameter =
            new UnittypeParameter(unittype, name, unittypeParameterFlag);
        unittypeParameter.setId(unittypeParamId);
        if (lastUnittype == null || lastUnittype != unittype) {
          idMap = new HashMap<>();
          nameMap = new MapWrapper<UnittypeParameter>(isStrictOrder()).getMap();
          unittype.setUnittypeParameters(new UnittypeParameters(idMap, nameMap, unittype));
          lastUnittype = unittype;
        }
        nameMap.put(name, unittypeParameter);
        idMap.put(unittypeParamId, unittypeParameter);
        unittype.getUnittypeParameters().updateInternalMaps(unittypeParameter);
      }
      if (logger.isDebugEnabled()) {
        logger.debug("Read " + counter + " unittype params");
      }
    } finally {
      if (rs != null) {
        rs.close();
      }
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
   * SyslogEvent differ from the other read-operations because not all syslog-event has a
   * unittype-id.
   *
   * @return
   * @throws SQLException
   */
  private void readSyslogEvents(Unittypes unittypes) throws SQLException {
    Statement s = null;
    ResultSet rs = null;
    String sql;
    boolean wasAutoCommit = false;
    Connection connection = null;
    try {
      TreeMap<Integer, SyslogEvent> syslogIdMap = null;
      Unittype lastUnittype = null;
      if (ACSVersionCheck.syslogEventReworkSupported) {
        sql = "SELECT * FROM syslog_event ORDER BY unit_type_id ASC";
      } else {
        sql = "SELECT * FROM syslog_event ORDER BY unit_type_name ASC";
      }
      connection = getDataSource().getConnection();
      wasAutoCommit = connection.getAutoCommit();
      connection.setAutoCommit(false);
      s = connection.createStatement();
      s.setQueryTimeout(60);
      rs = s.executeQuery(sql);
      int counter = 0;
      while (rs.next()) {
        counter++;
        Unittype unittype;
        if (ACSVersionCheck.syslogEventReworkSupported) {
          unittype = unittypes.getById(rs.getInt("unit_type_id"));
        } else {
          unittype = unittypes.getByName(rs.getString("unit_type_name"));
        }

        SyslogEvent syslogEvent = new SyslogEvent();
        syslogEvent.validateInput(false);
        syslogEvent.setUnittype(unittype);
        syslogEvent.setId(rs.getInt("id"));
        syslogEvent.setEventId(rs.getInt("syslog_event_id"));
        syslogEvent.setName(rs.getString("syslog_event_name"));
        syslogEvent.setExpression(rs.getString("expression"));
        syslogEvent.setDescription(rs.getString("description"));
        String deleteLimitStr = rs.getString("delete_limit");
        if (deleteLimitStr != null) {
          syslogEvent.setDeleteLimit(Integer.valueOf(deleteLimitStr));
        }
        if (ACSVersionCheck.syslogEventReworkSupported) {
          String groupId = rs.getString("group_id");
          if (groupId != null) {
            syslogEvent.setGroup(unittype.getGroups().getById(Integer.valueOf(groupId)));
          }
          syslogEvent.setStorePolicy(SyslogEvent.StorePolicy.valueOf(rs.getString("store_policy")));
          String filestoreId = rs.getString("filestore_id");
          if (filestoreId != null) {
            syslogEvent.setScript(unittype.getFiles().getById(Integer.valueOf(filestoreId)));
          }
        } else {
          String taskStr = rs.getString("task");
          if (taskStr.startsWith("DUCT") || taskStr.startsWith("DCT")) {
            syslogEvent.setStorePolicy(SyslogEvent.StorePolicy.DUPLICATE);
          } else if (taskStr.startsWith("DISCARD")) {
            syslogEvent.setStorePolicy(SyslogEvent.StorePolicy.DISCARD);
          } else {
            syslogEvent.setStorePolicy(SyslogEvent.StorePolicy.STORE);
          }
          if ("CALL".equalsIgnoreCase(taskStr.substring(0, 4))) {
            String fileName = taskStr.substring(4).trim();
            int varPos = fileName.indexOf(" -v");
            if (varPos > -1) {
              fileName = fileName.substring(0, varPos);
            }
            syslogEvent.setScript(unittype.getFiles().getByName(fileName));
          }
        }
        syslogEvent.validateInput(true);

        if (lastUnittype == null || lastUnittype != unittype) {
          syslogIdMap = new TreeMap<>(new NumberComparator());
          unittype.setSyslogEvents(new SyslogEvents(syslogIdMap, unittype));
          lastUnittype = unittype;
        }
        syslogIdMap.put(syslogEvent.getEventId(), syslogEvent);
        SyslogEvents.updateIdMap(syslogEvent);
      }
      if (logger.isDebugEnabled()) {
        logger.debug("Read " + counter + " syslog events");
      }
    } finally {
      if (rs != null) {
        rs.close();
      }
      if (s != null) {
        s.close();
      }
      if (connection != null) {
        connection.setAutoCommit(wasAutoCommit);
        connection.close();
      }
    }
  }

  private void readHeartbeats(Unittypes unittypes) throws SQLException {
    DynamicStatement ds = new DynamicStatement();
    PreparedStatement ps = null;
    ResultSet rs = null;
    boolean wasAutoCommit = false;
    Connection connection = null;
    try {
      Map<String, Heartbeat> nameMap = null;
      Map<Integer, Heartbeat> idMap = null;
      Unittype lastUnittype = null;
      ds.addSqlAndArguments("SELECT * FROM heartbeat ORDER BY unit_type_id ASC");
      connection = getDataSource().getConnection();
      wasAutoCommit = connection.getAutoCommit();
      connection.setAutoCommit(false);
      ps = ds.makePreparedStatement(connection);
      ps.setQueryTimeout(60);
      rs = ps.executeQuery();
      int counter = 0;
      while (rs.next()) {
        counter++;
        Heartbeat hb = new Heartbeat();
        Unittype unittype = unittypes.getById(rs.getInt("unit_type_id"));
        hb.validateInput(false);
        hb.setUnittype(unittype);
        hb.setId(rs.getInt("id"));
        hb.setName(rs.getString("name"));
        hb.setGroup(unittype.getGroups().getById(rs.getInt("heartbeat_group_id")));
        hb.setExpression(rs.getString("heartbeat_expression"));
        hb.setTimeoutHours(rs.getInt("heartbeat_timeout_hour"));
        hb.validateInput(true);
        if (lastUnittype == null || lastUnittype != unittype) {
          nameMap = new MapWrapper<Heartbeat>(isStrictOrder()).getMap();
          idMap = new HashMap<>();
          unittype.setHeartbeats(new Heartbeats(idMap, nameMap, unittype));
          lastUnittype = unittype;
        }
        idMap.put(hb.getId(), hb);
        nameMap.put(hb.getName(), hb);
      }
      if (logger.isDebugEnabled()) {
        logger.debug("Read " + counter + " heartbeats");
      }
    } finally {
      if (rs != null) {
        rs.close();
      }
      if (ps != null) {
        ps.close();
      }
      if (connection != null) {
        connection.setAutoCommit(wasAutoCommit);
        connection.close();
      }
    }
  }

  private void readTriggers(Unittypes unittypes) throws SQLException {
    DynamicStatement ds = new DynamicStatement();
    PreparedStatement ps = null;
    ResultSet rs = null;
    boolean wasAutoCommit = false;
    Connection connection = null;
    try {
      Map<String, Trigger> nameMap = null;
      Map<Integer, Trigger> idMap = null;
      Unittype lastUnittype = null;
      ds.addSqlAndArguments("SELECT * FROM trigger_ ORDER BY unit_type_id ASC");
      connection = getDataSource().getConnection();
      wasAutoCommit = connection.getAutoCommit();
      connection.setAutoCommit(false);
      ps = ds.makePreparedStatement(connection);
      ps.setQueryTimeout(60);
      rs = ps.executeQuery();
      Map<Integer, Integer> triggerIdParentIdMap = new HashMap<>();
      int counter = 0;
      while (rs.next()) {
        counter++;
        Unittype unittype = unittypes.getById(rs.getInt("unit_type_id"));
        int id = rs.getInt("id");
        int triggerType = rs.getInt("trigger_type");
        int action = rs.getInt("notify_type");

        // early/short constructor - to save amount of code and local
        // variables - will validate the object later on
        Trigger trigger = new Trigger(triggerType, action);
        trigger.setUnittype(unittype);
        trigger.setId(id);
        trigger.setActive(rs.getInt("active") == 1);
        trigger.setName(rs.getString("name"));
        trigger.setDescription(rs.getString("description"));
        trigger.setEvalPeriodMinutes(rs.getInt("eval_period_minutes"));
        String nihStr = rs.getString("notify_interval_hours");
        if (nihStr != null) {
          trigger.setNotifyIntervalHours(Integer.parseInt(nihStr));
        }
        String filestoreIdStr = rs.getString("filestore_id");
        if (filestoreIdStr != null) {
          trigger.setScript(unittype.getFiles().getById(Integer.parseInt(filestoreIdStr)));
        }
        String parentTriggerStr = rs.getString("parent_trigger_id");
        if (parentTriggerStr != null) {
          triggerIdParentIdMap.put(id, Integer.parseInt(parentTriggerStr));
        }
        trigger.setToList(rs.getString("to_list"));
        String syslogEventIdStr = rs.getString("syslog_event_id");
        if (syslogEventIdStr != null) {
          trigger.setSyslogEvent(SyslogEvents.getById(Integer.parseInt(syslogEventIdStr)));
        }
        String neStr = rs.getString("no_events");
        if (neStr != null) {
          trigger.setNoEvents(Integer.parseInt(neStr));
        }
        String nepuStr = rs.getString("no_events_pr_unit");
        if (nepuStr != null) {
          trigger.setNoEventPrUnit(Integer.parseInt(nepuStr));
        }
        String noStr = rs.getString("no_units");
        if (noStr != null) {
          trigger.setNoUnits(Integer.parseInt(noStr));
        }

        if (lastUnittype == null || lastUnittype != unittype) {
          nameMap = new MapWrapper<Trigger>(isStrictOrder()).getMap();
          idMap = new HashMap<>();
          unittype.setTriggers(new Triggers(idMap, nameMap, unittype));
          lastUnittype = unittype;
        }
        idMap.put(id, trigger);
        nameMap.put(trigger.getName(), trigger);
      }
      for (Unittype unittype : unittypes.getUnittypes()) {
        Triggers triggers = unittype.getTriggers();
        idMap = triggers.getIdMap();
        for (Map.Entry<Integer, Integer> entry : triggerIdParentIdMap.entrySet()) {
          Integer triggerId = entry.getKey();
          Trigger childTrigger = idMap.get(triggerId);
          if (childTrigger != null) {
            Trigger parentTrigger = idMap.get(entry.getValue());
            childTrigger.setParent(parentTrigger);
            parentTrigger.addChild(childTrigger);
          }
        }
      }
      if (logger.isDebugEnabled()) {
        logger.debug("Read " + counter + " triggers");
      }
    } finally {
      if (rs != null) {
        rs.close();
      }
      if (ps != null) {
        ps.close();
      }
      if (connection != null) {
        connection.setAutoCommit(wasAutoCommit);
        connection.close();
      }
    }
  }

  private void readGroups(Unittypes unittypes) throws SQLException {
    Statement s = null;
    ResultSet rs = null;
    String sql;
    boolean wasAutoCommit = false;
    Connection connection = null;
    try {
      Map<String, Group> nameMap = null;
      Map<Integer, Group> idMap = null;
      Unittype lastUnittype = null;
      sql = "SELECT * FROM group_ ORDER BY unit_type_id ASC";
      connection = getDataSource().getConnection();
      wasAutoCommit = connection.getAutoCommit();
      connection.setAutoCommit(false);
      s = connection.createStatement();
      s.setQueryTimeout(60);
      rs = s.executeQuery(sql);
      int counter = 0;
      while (rs.next()) {
        counter++;

        // Read row data
        Unittype unittype = unittypes.getById(rs.getInt("unit_type_id"));
        Integer groupId = rs.getInt("group_id");
        String groupName = rs.getString("group_name");
        String description = rs.getString("description");
        String parentIdStr = rs.getString("parent_group_id");
        String profileIdStr = rs.getString("profile_id");
        Integer count;
        count = rs.getInt("count");

        // Make maps
        if (lastUnittype == null || lastUnittype != unittype) {
          nameMap = new MapWrapper<Group>(isStrictOrder()).getMap();
          idMap = new HashMap<>();
          unittype.setGroups(new Groups(idMap, nameMap, unittype));
          lastUnittype = unittype;
        }

        // Populate group object, make empty parent object if necessary
        // Populate maps
        Group parent = null;
        if (parentIdStr != null) {
          Integer parentId = Integer.valueOf(parentIdStr);
          parent = idMap.get(parentId);
          if (parent == null) {
            parent = new Group(parentId);
            idMap.put(parentId, parent);
          }
        }
        Group thisGroup = idMap.get(groupId);
        if (thisGroup == null) {
          thisGroup = new Group(groupId);
          idMap.put(groupId, thisGroup);
        }
        thisGroup.setUnittype(unittype);
        thisGroup.setName(groupName);
        thisGroup.setDescription(description);
        thisGroup.setCount(count);
        if (profileIdStr != null) {
          Integer profileId = Integer.parseInt(profileIdStr);
          thisGroup.setProfile(unittype.getProfiles().getById(profileId));
        }
        thisGroup.setParent(parent);
        if (parent != null) {
          parent.addChild(thisGroup);
        }

        nameMap.put(groupName, thisGroup);
      }
      if (logger.isDebugEnabled()) {
        logger.debug("Read " + counter + " groups");
      }
    } finally {
      if (rs != null) {
        rs.close();
      }
      if (s != null) {
        s.close();
      }
      if (connection != null) {
        connection.setAutoCommit(wasAutoCommit);
        connection.close();
      }
    }
  }

  private void readProfiles(Unittypes unittypes) throws SQLException {
    Statement s = null;
    ResultSet rs = null;
    String sql;
    boolean wasAutoCommit = false;
    Connection connection = null;
    try {
      Map<String, Profile> nameMap = null;
      Map<Integer, Profile> idMap = null;
      Unittype lastUnittype = null;
      sql = "SELECT unit_type_id, profile_id, profile_name FROM profile ORDER BY unit_type_id ASC";
      connection = getDataSource().getConnection();
      wasAutoCommit = connection.getAutoCommit();
      connection.setAutoCommit(false);
      s = connection.createStatement();
      s.setQueryTimeout(60);
      rs = s.executeQuery(sql);
      int max = 0;
      int counter = 0;
      while (rs.next()) {
        counter++;
        Unittype unittype = unittypes.getById(rs.getInt("unit_type_id"));
        Integer profileId = rs.getInt("profile_id");
        if (profileId > max) {
          max = profileId;
        }
        String profileName = rs.getString("profile_name");
        Profile profile = new Profile(profileName, unittype);
        profile.setId(profileId);
        if (lastUnittype == null || lastUnittype != unittype) {
          nameMap = new MapWrapper<Profile>(isStrictOrder()).getMap();
          idMap = new HashMap<>();
          unittype.setProfiles(new Profiles(idMap, nameMap));
          lastUnittype = unittype;
        }
        nameMap.put(profileName, profile);
        idMap.put(profileId, profile);
      }
      if (logger.isDebugEnabled()) {
        logger.debug("Read " + counter + " profiles");
      }
    } finally {
      if (rs != null) {
        rs.close();
      }
      if (s != null) {
        s.close();
      }
      if (connection != null) {
        connection.setAutoCommit(wasAutoCommit);
        connection.close();
      }
    }
  }

  private void readFilestore(Unittypes unittypes) throws SQLException {
    Statement s = null;
    ResultSet rs = null;
    String sql;
    boolean wasAutoCommit = false;
    Connection connection = null;
    try {
      Map<Integer, File> idMap = null;
      Map<String, File> nameMap = null;
      TreeMap<String, File> versionTypeMap = null;
      Unittype lastUnittype = null;
      connection = getDataSource().getConnection();
      wasAutoCommit = connection.getAutoCommit();
      connection.setAutoCommit(false);
      s = connection.createStatement();
      s.setQueryTimeout(120);
      sql =
          "SELECT unit_type_id, id, name, type, description, version, timestamp_, length(content) as length";
      if (ACSVersionCheck.fileReworkSupported) {
        sql += ", target_name, owner ";
      }
      sql += " FROM filestore ORDER BY unit_type_id ASC";
      rs = s.executeQuery(sql);
      int counter = 0;
      while (rs.next()) {
        counter++;
        File file = new File();
        file.validateInput(false);
        Unittype unittype = unittypes.getById(rs.getInt("unit_type_id"));
        file.setUnittype(unittype);
        file.setId(rs.getInt("id"));
        file.setName(rs.getString("name"));
        String typeStr = rs.getString("type");
        FileType ft = null;
        try {
          ft = FileType.valueOf(typeStr);
        } catch (Throwable t) { // Convert from old types
          if ("SCRIPT".equals(typeStr)) {
            ft = FileType.SHELL_SCRIPT;
          }
          if ("CONFIG".equals(typeStr)) {
            ft = FileType.TR069_SCRIPT;
          }
        }
        file.setType(ft);
        file.setDescription(rs.getString("description"));
        file.setVersion(rs.getString("version"));
        file.setTimestamp(rs.getTimestamp("timestamp_"));
        file.setLength(rs.getInt("length"));
        String targetName = null;
        User owner = null;
        if (ACSVersionCheck.fileReworkSupported) {
          targetName = rs.getString("target_name");
          String userIdStr = rs.getString("owner");
          if (userIdStr != null) {
            try {
              owner =
                  unittype.getAcs().getUser().getUsers().getUnprotected(Integer.valueOf(userIdStr));
            } catch (NumberFormatException ignored) {
              // ignore
            }
          }
        }
        file.setTargetName(targetName);
        file.setOwner(owner);
        file.validateInput(true);
        file.setConnectionProperties(getDataSource());
        file.resetContentToNull();
        if (lastUnittype == null || lastUnittype != unittype) {
          idMap = new HashMap<>();
          nameMap = new MapWrapper<File>(isStrictOrder()).getMap();
          versionTypeMap = new TreeMap<>();
          unittype.setFiles(new Files(idMap, nameMap, versionTypeMap, unittype));
          lastUnittype = unittype;
        }
        idMap.put(file.getId(), file);
        nameMap.put(file.getName(), file);
        versionTypeMap.put(file.getVersion() + typeStr, file);
      }
      if (logger.isDebugEnabled()) {
        logger.debug("Read " + counter + " files");
      }
    } finally {
      if (rs != null) {
        rs.close();
      }
      if (s != null) {
        s.close();
      }
      if (connection != null) {
        connection.setAutoCommit(wasAutoCommit);
        connection.close();
      }
    }
  }

  private Unittypes readUnittypes() throws SQLException {
    Statement s = null;
    ResultSet rs = null;
    String sql;
    boolean wasAutoCommit = false;
    Connection connection = null;
    try {
      MapWrapper<Unittype> mw = new MapWrapper<>(isStrictOrder());
      Map<String, Unittype> unittypeMap = mw.getMap();
      Map<Integer, Unittype> idMap = new HashMap<>();
      sql = "SELECT * FROM unit_type";
      connection = getDataSource().getConnection();
      wasAutoCommit = connection.getAutoCommit();
      connection.setAutoCommit(false);
      s = connection.createStatement();
      s.setQueryTimeout(60);
      rs = s.executeQuery(sql);
      while (rs.next()) {
        Integer id = rs.getInt("unit_type_id");
        String unittypeName = rs.getString("unit_type_name");
        String vendorName = rs.getString("vendor_name");
        String desc = rs.getString("description");
        String protocol = rs.getString("protocol");
        Unittype unittype =
            new Unittype(unittypeName, vendorName, desc, ProvisioningProtocol.toEnum(protocol));
        unittype.setId(id);
        unittype.setAcs(this);
        unittypeMap.put(unittypeName, unittype);
        idMap.put(id, unittype);
      }
      if (logger.isDebugEnabled()) {
        logger.debug("Read " + idMap.size() + " unittypes");
      }
      return new Unittypes(unittypeMap, idMap);
    } finally {
      if (rs != null) {
        rs.close();
      }
      if (s != null) {
        s.close();
      }
      if (connection != null) {
        connection.setAutoCommit(wasAutoCommit);
        connection.close();
      }
    }
  }

  private void readJobs(Unittypes unittypes) throws SQLException {
    Statement s = null;
    ResultSet rs = null;
    boolean wasAutoCommit = false;
    Connection connection = null;
    try {
      Map<Integer, Job> idMap = null;
      Map<String, Job> nameMap = null;
      Unittype lastUnittype = null;
      connection = getDataSource().getConnection();
      wasAutoCommit = connection.getAutoCommit();
      connection.setAutoCommit(false);
      s = connection.createStatement();
      s.setQueryTimeout(60);
      rs =
          s.executeQuery(
              "SELECT * FROM job j, group_ g WHERE j.group_id = g.group_id ORDER BY g.unit_type_id ASC, j.job_id_dependency ASC"); // will list non-dependent jobs first
      int jobCounter = 0;
      while (rs.next()) {
        jobCounter++;
        Job j = new Job();
        j.validateInput(false);
        Unittype unittype = unittypes.getById(rs.getInt("unit_type_id"));
        j.setUnittype(unittype);
        j.setId(rs.getInt("job_id"));
        j.setName(rs.getString("job_name"));
        j.setFlags(new JobFlag(rs.getString("job_type")));
        j.setDescription(rs.getString("description"));
        j.setGroup(unittype.getGroups().getById(rs.getInt("group_id")));
        j.setUnconfirmedTimeout(rs.getInt("unconfirmed_timeout"));
        j.setStopRules(rs.getString("stop_rules"));
        String statusStr = rs.getString("status");
        try {
          j.setStatus(JobStatus.valueOf(statusStr));
        } catch (Throwable t) { // Convert from old types
          if ("STOPPED".equals(statusStr)) {
            j.setStatus(JobStatus.PAUSED);
          }
        }
        j.setCompletedNoFailures(rs.getInt("completed_no_failure"));
        j.setCompletedHadFailures(rs.getInt("completed_had_failure"));
        j.setConfirmedFailed(rs.getInt("confirmed_failed"));
        j.setUnconfirmedFailed(rs.getInt("unconfirmed_failed"));
        j.setStartTimestamp(rs.getTimestamp("start_timestamp"));
        j.setEndTimestamp(rs.getTimestamp("end_timestamp"));
        String fileIdStr = rs.getString("firmware_id");
        if (fileIdStr != null) {
          j.setFile(unittype.getFiles().getById(Integer.parseInt(fileIdStr)));
        }
        String depIdStr = rs.getString("job_id_dependency");
        if (depIdStr != null) {
          Integer depId = Integer.parseInt(depIdStr);
          Job depJob = new Job();
          depJob.setId(depId);
          j.setDependency(depJob);
        }
        String repeatCountStr = rs.getString("repeat_count");
        if (repeatCountStr != null) {
          j.setRepeatCount(Integer.parseInt(repeatCountStr));
        }
        String repeatIntervalStr = rs.getString("repeat_interval");
        if (repeatIntervalStr != null) {
          j.setRepeatInterval(Integer.parseInt(repeatIntervalStr));
        }

        j.validateInput(true);

        // The job has been retrieved. put it in the map
        if (lastUnittype == null || lastUnittype != unittype) {
          idMap = new TreeMap<>();
          nameMap = new MapWrapper<Job>(isStrictOrder()).getMap();
          unittype.setJobs(new Jobs(idMap, nameMap, unittype));
          lastUnittype = unittype;
        }
        idMap.put(j.getId(), j);
        nameMap.put(j.getName(), j);
      }
      s.close();

      // Update dependencies
      for (Unittype unittype : unittypes.getUnittypes()) {
        for (Job j : unittype.getJobs().getJobs()) {
          if (j.getDependency() != null) {
            Integer depId = j.getDependency().getId();
            Job depJob = unittype.getJobs().getById(depId);
            j.setDependency(depJob);
          }
        }
      }

      if (logger.isDebugEnabled()) {
        logger.debug("Read " + jobCounter + " jobs");
      }

      // Update job parameters
      s = connection.createStatement();
      s.setQueryTimeout(60);
      rs =
          s.executeQuery(
              "SELECT utp.unit_type_id, jp.job_id, jp.unit_type_param_id, jp.value FROM job_param jp, unit_type_param utp WHERE jp.unit_type_param_id = utp.unit_type_param_id AND unit_id = '"
                  + Job.ANY_UNIT_IN_GROUP
                  + "'");
      int paramsCounter = 0;
      while (rs.next()) {
        paramsCounter++;
        Unittype unittype = unittypes.getById(rs.getInt("unit_type_id"));
        Job job = unittype.getJobs().getById(rs.getInt("job_id"));
        Integer unitTypeParamId = rs.getInt("unit_type_param_id");
        String value = rs.getString("value");
        if (value == null) {
          value = "";
        }
        UnittypeParameter utp = unittype.getUnittypeParameters().getById(unitTypeParamId);
        JobParameter jp = new JobParameter(job, Job.ANY_UNIT_IN_GROUP, new Parameter(utp, value));
        job.getDefaultParameters().put(utp.getName(), jp);
      }
      if (logger.isDebugEnabled()) {
        logger.debug("Read " + paramsCounter + " job parameters");
      }
    } finally {
      if (rs != null) {
        rs.close();
      }
      if (s != null) {
        s.close();
      }
      if (connection != null) {
        connection.setAutoCommit(wasAutoCommit);
        connection.close();
      }
    }
  }

  public Unittypes getUnittypes() {
    return unittypes;
  }

  public Unittype getUnittype(Integer unittypeId) {
    return unittypes.getById(unittypeId);
  }

  public Unittype getUnittype(String unittypeName) {
    return unittypes.getByName(unittypeName);
  }

  public UnittypeParameter getUnittypeParameter(Integer unittypeParameterId) {
    Unittype[] unittypeArr = unittypes.getUnittypes();
    for (Unittype anUnittypeArr : unittypeArr) {
      UnittypeParameters unittypeParameters = anUnittypeArr.getUnittypeParameters();
      if (unittypeParameters.getById(unittypeParameterId) != null) {
        return unittypeParameters.getById(unittypeParameterId);
      }
    }
    return null;
  }

  public UnittypeParameter getUnittypeParameter(String unittypeName, String unittypeParameterName) {
    Unittype unittype = getUnittype(unittypeName);
    if (unittype != null) {
      return unittype.getUnittypeParameters().getByName(unittypeParameterName);
    }
    return null;
  }

  public Group getGroup(Integer groupId) {
    for (Unittype u : unittypes.getUnittypes()) {
      Group g = u.getGroups().getById(groupId);
      if (g != null) {
        return g;
      }
    }
    return null;
  }

  public Profile getProfile(Integer profileId) {
    Unittype[] unittypeArr = unittypes.getUnittypes();
    for (Unittype anUnittypeArr : unittypeArr) {
      Profiles profiles = anUnittypeArr.getProfiles();
      if (profiles.getById(profileId) != null) {
        return profiles.getById(profileId);
      }
    }
    return null;
  }

  public Profile getProfile(String unittypeName, String profileName) {
    Unittype unittype = getUnittype(unittypeName);
    if (unittype != null) {
      return unittype.getProfiles().getByName(profileName);
    }
    return null;
  }

  public Syslog getSyslog() {
    return syslog;
  }

  public void setSyslog(Syslog syslog) {
    this.syslog = syslog;
  }

  public String toString() {
    return "ACS";
  }

  public static boolean isStrictOrder() {
    return strictOrder;
  }

  public static void setStrictOrder(boolean strictOrder) {
    ACS.strictOrder = strictOrder;
  }

  public void setDbi(DBI dbi) {
    this.dbi = dbi;
  }

  public DBI getDbi() {
    return dbi;
  }

  public DataSource getConnectionProperties() {
    return getDataSource();
  }

  public DataSource getDataSource() {
    return dataSource;
  }
}
