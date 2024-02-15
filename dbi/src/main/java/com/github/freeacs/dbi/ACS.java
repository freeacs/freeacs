package com.github.freeacs.dbi;

import com.github.freeacs.common.util.NumberComparator;
import com.github.freeacs.dbi.sql.AutoCommitResettingConnectionWrapper;
import com.github.freeacs.dbi.sql.DynamicStatementWrapper;
import com.github.freeacs.dbi.sql.ReadConnectionWrapper;
import com.github.freeacs.dbi.sql.StatementWithTimeoutWrapper;
import com.github.freeacs.dbi.Unittype.ProvisioningProtocol;
import com.github.freeacs.dbi.util.ACSVersionCheck;
import com.github.freeacs.dbi.util.MapWrapper;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import javax.sql.DataSource;

import lombok.Getter;
import lombok.Setter;
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
@Getter
@Setter
public class ACS {
  private static final Logger logger = LoggerFactory.getLogger(ACS.class);
  @Getter
  @Setter
  private static boolean strictOrder = true;

  private final DataSource dataSource;
  private final Users users;

  private Unittypes unittypes;

  private DBI dbi;

  private ScriptExecutions scriptExecutions;

  public ACS(DataSource dataSource) throws SQLException {
    long start = System.currentTimeMillis();
    this.dataSource = dataSource;
    this.users = new Users(dataSource);
    /* Checks all necessary tables to see which version they're in */
    ACSVersionCheck.versionCheck(dataSource);
    this.unittypes = read();
    if (logger.isDebugEnabled()) {
      logger.debug("Read ACS object in " + (System.currentTimeMillis() - start) + " ms.");
    }
  }

  public ScriptExecutions getScriptExecutions() {
    if (scriptExecutions == null) {
      scriptExecutions = new ScriptExecutions(getDataSource());
    }
    return scriptExecutions;
  }

  /**
   * Read all unittypes
   *
   * @return the Unittypes object
   * @throws SQLException if something goes wrong
   */
  public Unittypes read() throws SQLException {
    unittypes = readAsAdmin();
    logger.debug("Updated ACS object, read " + unittypes.getUnittypes().length + " unittypes");
    return unittypes;
  }

  private Unittypes readAsAdmin() throws SQLException {
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
  }

  private void readGroupParameters(Unittypes unittypes) throws SQLException {
    String sql = """
      SELECT ut.unit_type_name,
      gp.id,
      gp.group_id, gp.unit_type_param_id,
      gp.operator, gp.data_type,
      gp.value
      FROM group_param gp, unit_type_param utp, unit_type ut
      WHERE gp.unit_type_param_id = utp.unit_type_param_id AND utp.unit_type_id = ut.unit_type_id ORDER BY ut.unit_type_name ASC
    """;
    try(AutoCommitResettingConnectionWrapper connectionWrapper =
                new ReadConnectionWrapper(dataSource.getConnection());
        StatementWithTimeoutWrapper statementWrapper =
                new StatementWithTimeoutWrapper(connectionWrapper, 60);
        ResultSet resultSet = statementWrapper.getStatement().executeQuery(sql)) {
      int counter = 0;
      while (resultSet.next()) {
        counter++;
        Unittype unittype = unittypes.getByName(resultSet.getString("unit_type_name"));
        Group group = unittype.getGroups().getById(resultSet.getInt("group_id"));
        Integer unittypeParamId = resultSet.getInt("gp.unit_type_param_id");
        UnittypeParameter utp =
            group.getUnittype().getUnittypeParameters().getById(unittypeParamId);
        String value = resultSet.getString("gp.value");
        Parameter.Operator op = Parameter.Operator.getOperator(resultSet.getString("operator"));
        Parameter.ParameterDataType pdt =
            Parameter.ParameterDataType.getDataType(resultSet.getString("data_type"));
        Parameter parameter = new Parameter(utp, value, op, pdt);
        GroupParameter groupParameter = new GroupParameter(parameter, group);
        groupParameter.setId(resultSet.getInt("gp.id"));
        GroupParameters groupParams = group.getGroupParameters();
        groupParams.addOrChangeGroupParameter(groupParameter);
      }
      if (logger.isDebugEnabled()) {
        logger.debug("Read " + counter + " group parameters");
      }
    }
  }

  private void readProfileParameters(Unittypes unittypes) throws SQLException {
    String sql = """
      SELECT utp.unit_type_id, pm.profile_id, pm.unit_type_param_id, pm.value
      FROM profile_param pm, unit_type_param utp
      WHERE pm.unit_type_param_id = utp.unit_type_param_id
      ORDER BY utp.unit_type_id ASC, pm.profile_id ASC
    """;
    try(AutoCommitResettingConnectionWrapper connectionWrapper =
                new ReadConnectionWrapper(getDataSource().getConnection());
        StatementWithTimeoutWrapper statementWrapper =
                new StatementWithTimeoutWrapper(connectionWrapper, 60);
        ResultSet resultSet = statementWrapper.getStatement().executeQuery(sql)) {
      Map<String, ProfileParameter> nameMap = null;
      Map<Integer, ProfileParameter> idMap = null;
      Profile lastProfile = null;
      int counter = 0;
      while (resultSet.next()) {
        counter++;
        Unittype unittype = unittypes.getById(resultSet.getInt("unit_type_id"));
        Profile profile = unittype.getProfiles().getById(resultSet.getInt("profile_id"));
        Integer unittypeParamId = resultSet.getInt("unit_type_param_id");
        String value = resultSet.getString("value");
        if (value == null) {
          value = "";
        }
        UnittypeParameter unittypeParameter =
            unittype.getUnittypeParameters().getById(unittypeParamId);
        ProfileParameter profileParameter = new ProfileParameter(profile, unittypeParameter, value);
        if (lastProfile == null || lastProfile != profile) {
          nameMap = new MapWrapper<ProfileParameter>(strictOrder).getMap();
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
    }
  }

  private void readUnittypeParameterValues(Unittypes unittypes) throws SQLException {
    final var sql = """
        SELECT utp.unit_type_id, utpv.unit_type_param_id, value, priority, type
        FROM unit_type_param_value utpv, unit_type_param utp
        WHERE utpv.unit_type_param_id = utp.unit_type_param_id
        ORDER BY utp.unit_type_id ASC, utpv.unit_type_param_id, utpv.priority ASC
    """;
    try(AutoCommitResettingConnectionWrapper connectionWrapper =
                new ReadConnectionWrapper(dataSource.getConnection());
        StatementWithTimeoutWrapper statementWrapper =
                new StatementWithTimeoutWrapper(connectionWrapper, 60);
        ResultSet resultSet = statementWrapper.getStatement().executeQuery(sql)) {
      UnittypeParameterValues values = null;
      Integer lastUnittypeParameterId = null;
      int counter = 0;
      while (resultSet.next()) {
        counter++;
        Unittype unittype = unittypes.getById(resultSet.getInt("unit_type_id"));
        Integer utpId = resultSet.getInt("unit_type_param_id");
        if (lastUnittypeParameterId == null || !lastUnittypeParameterId.equals(utpId)) {
          UnittypeParameter up = unittype.getUnittypeParameters().getById(utpId);
          values = new UnittypeParameterValues();
          up.setValuesFromACS(values);
          lastUnittypeParameterId = utpId;
        }
        String type = resultSet.getString("type");
        String value = resultSet.getString("value");
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
    }
  }

  private void readUnittypeParameters(Unittypes unittypes) throws SQLException {
    String sql = """
      SELECT unit_type_id, unit_type_param_id, name, flags
      FROM unit_type_param ORDER BY unit_type_id ASC
    """;
    try(AutoCommitResettingConnectionWrapper connectionWrapper =
                new ReadConnectionWrapper(dataSource.getConnection());
        StatementWithTimeoutWrapper statementWrapper =
                new StatementWithTimeoutWrapper(connectionWrapper, 60);
        ResultSet resultSet = statementWrapper.getStatement().executeQuery(sql)) {
      Map<Integer, UnittypeParameter> idMap = null;
      Map<String, UnittypeParameter> nameMap = null;
      Unittype lastUnittype = null;
      int counter = 0;
      while (resultSet.next()) {
        counter++;
        Unittype unittype = unittypes.getById(resultSet.getInt("unit_type_id"));
        Integer unittypeParamId = resultSet.getInt("unit_type_param_id");
        String name = resultSet.getString("name");
        UnittypeParameterFlag unittypeParameterFlag =
            new UnittypeParameterFlag(resultSet.getString("flags"));
        UnittypeParameter unittypeParameter =
            new UnittypeParameter(unittype, name, unittypeParameterFlag);
        unittypeParameter.setId(unittypeParamId);
        if (lastUnittype == null || lastUnittype != unittype) {
          idMap = new HashMap<>();
          nameMap = new MapWrapper<UnittypeParameter>(strictOrder).getMap();
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
    }
  }

  /**
   * SyslogEvent differ from the other read-operations because not all syslog-event has a
   * unittype-id.
   *
   * @throws SQLException if something goes wrong
   */
  private void readSyslogEvents(Unittypes unittypes) throws SQLException {
    String sql = ACSVersionCheck.syslogEventReworkSupported
            ? "SELECT * FROM syslog_event ORDER BY unit_type_id ASC"
            : "SELECT * FROM syslog_event ORDER BY unit_type_name ASC";
    try(AutoCommitResettingConnectionWrapper connectionWrapper =
                new ReadConnectionWrapper(dataSource.getConnection());
        StatementWithTimeoutWrapper statementWrapper =
                new StatementWithTimeoutWrapper(connectionWrapper, 60);
        ResultSet resultSet = statementWrapper.getStatement().executeQuery(sql)) {
      TreeMap<Integer, SyslogEvent> syslogIdMap = null;
      Unittype lastUnittype = null;

      int counter = 0;
      while (resultSet.next()) {
        counter++;
        Unittype unittype;
        if (ACSVersionCheck.syslogEventReworkSupported) {
          unittype = unittypes.getById(resultSet.getInt("unit_type_id"));
        } else {
          unittype = unittypes.getByName(resultSet.getString("unit_type_name"));
        }

        SyslogEvent syslogEvent = new SyslogEvent();
        syslogEvent.validateInput(false);
        syslogEvent.setUnittype(unittype);
        syslogEvent.setId(resultSet.getInt("id"));
        syslogEvent.setEventId(resultSet.getInt("syslog_event_id"));
        syslogEvent.setName(resultSet.getString("syslog_event_name"));
        syslogEvent.setExpression(resultSet.getString("expression"));
        syslogEvent.setDescription(resultSet.getString("description"));
        String deleteLimitStr = resultSet.getString("delete_limit");
        if (deleteLimitStr != null) {
          syslogEvent.setDeleteLimit(Integer.valueOf(deleteLimitStr));
        }
        if (ACSVersionCheck.syslogEventReworkSupported) {
          String groupId = resultSet.getString("group_id");
          if (groupId != null) {
            syslogEvent.setGroup(unittype.getGroups().getById(Integer.valueOf(groupId)));
          }
          syslogEvent.setStorePolicy(SyslogEvent.StorePolicy.valueOf(resultSet.getString("store_policy")));
          String filestoreId = resultSet.getString("filestore_id");
          if (filestoreId != null) {
            syslogEvent.setScript(unittype.getFiles().getById(Integer.valueOf(filestoreId)));
          }
        } else {
          String taskStr = resultSet.getString("task");
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
    }
  }

  private void readHeartbeats(Unittypes unittypes) throws SQLException {
    try(AutoCommitResettingConnectionWrapper connectionWrapper =
                new ReadConnectionWrapper(dataSource.getConnection());
        DynamicStatementWrapper statementWrapper =
                new DynamicStatementWrapper(connectionWrapper,"SELECT * FROM heartbeat ORDER BY unit_type_id ASC");
        ResultSet resultSet =
                statementWrapper.getPreparedStatement().executeQuery()) {
      Map<String, Heartbeat> nameMap = null;
      Map<Integer, Heartbeat> idMap = null;
      Unittype lastUnittype = null;
      int counter = 0;
      while (resultSet.next()) {
        counter++;
        Heartbeat hb = new Heartbeat();
        Unittype unittype = unittypes.getById(resultSet.getInt("unit_type_id"));
        hb.setValidateInput(false);
        hb.setUnittype(unittype);
        hb.setId(resultSet.getInt("id"));
        hb.setName(resultSet.getString("name"));
        hb.setGroup(unittype.getGroups().getById(resultSet.getInt("heartbeat_group_id")));
        hb.setExpression(resultSet.getString("heartbeat_expression"));
        hb.setTimeoutHours(resultSet.getInt("heartbeat_timeout_hour"));
        hb.setValidateInput(true);
        if (lastUnittype == null || lastUnittype != unittype) {
          nameMap = new MapWrapper<Heartbeat>(strictOrder).getMap();
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
    }
  }

  private void readTriggers(Unittypes unittypes) throws SQLException {
    String sql = "SELECT * FROM trigger_ ORDER BY unit_type_id ASC";
    try(AutoCommitResettingConnectionWrapper connectionWrapper =
                new ReadConnectionWrapper(dataSource.getConnection());
        DynamicStatementWrapper statementWrapper =
                new DynamicStatementWrapper(connectionWrapper, sql);
        ResultSet resultSet =
                statementWrapper.getPreparedStatement().executeQuery()) {
      Map<String, Trigger> nameMap = null;
      Map<Integer, Trigger> idMap = null;
      Unittype lastUnittype = null;
      Map<Integer, Integer> triggerIdParentIdMap = new HashMap<>();
      int counter = 0;
      while (resultSet.next()) {
        counter++;
        Unittype unittype = unittypes.getById(resultSet.getInt("unit_type_id"));
        int id = resultSet.getInt("id");
        int triggerType = resultSet.getInt("trigger_type");
        int action = resultSet.getInt("notify_type");

        // early/short constructor - to save amount of code and local
        // variables - will validate the object later on
        Trigger trigger = new Trigger(triggerType, action);
        trigger.setUnittype(unittype);
        trigger.setId(id);
        trigger.setActive(resultSet.getInt("active") == 1);
        trigger.setName(resultSet.getString("name"));
        trigger.setDescription(resultSet.getString("description"));
        trigger.setEvalPeriodMinutes(resultSet.getInt("eval_period_minutes"));
        String nihStr = resultSet.getString("notify_interval_hours");
        if (nihStr != null) {
          trigger.setNotifyIntervalHours(Integer.parseInt(nihStr));
        }
        String filestoreIdStr = resultSet.getString("filestore_id");
        if (filestoreIdStr != null) {
          trigger.setScript(unittype.getFiles().getById(Integer.parseInt(filestoreIdStr)));
        }
        String parentTriggerStr = resultSet.getString("parent_trigger_id");
        if (parentTriggerStr != null) {
          triggerIdParentIdMap.put(id, Integer.parseInt(parentTriggerStr));
        }
        trigger.setToList(resultSet.getString("to_list"));
        String syslogEventIdStr = resultSet.getString("syslog_event_id");
        if (syslogEventIdStr != null) {
          trigger.setSyslogEvent(SyslogEvents.getById(Integer.parseInt(syslogEventIdStr)));
        }
        String neStr = resultSet.getString("no_events");
        if (neStr != null) {
          trigger.setNoEvents(Integer.parseInt(neStr));
        }
        String nepuStr = resultSet.getString("no_events_pr_unit");
        if (nepuStr != null) {
          trigger.setNoEventPrUnit(Integer.parseInt(nepuStr));
        }
        String noStr = resultSet.getString("no_units");
        if (noStr != null) {
          trigger.setNoUnits(Integer.parseInt(noStr));
        }

        if (lastUnittype == null || lastUnittype != unittype) {
          nameMap = new MapWrapper<Trigger>(strictOrder).getMap();
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
    }
  }

  private void readGroups(Unittypes unittypes) throws SQLException {
    String sql = "SELECT * FROM group_ ORDER BY unit_type_id ASC";
    try(AutoCommitResettingConnectionWrapper connectionWrapper =
                new ReadConnectionWrapper(dataSource.getConnection());
        StatementWithTimeoutWrapper statementWrapper =
                new StatementWithTimeoutWrapper(connectionWrapper, 60);
        ResultSet resultSet =
                statementWrapper.getStatement().executeQuery(sql)) {
      Map<String, Group> nameMap = null;
      Map<Integer, Group> idMap = null;
      Unittype lastUnittype = null;
      int counter = 0;
      while (resultSet.next()) {
        counter++;

        // Read row data
        Unittype unittype = unittypes.getById(resultSet.getInt("unit_type_id"));
        Integer groupId = resultSet.getInt("group_id");
        String groupName = resultSet.getString("group_name");
        String description = resultSet.getString("description");
        String parentIdStr = resultSet.getString("parent_group_id");
        String profileIdStr = resultSet.getString("profile_id");
        Integer count = resultSet.getInt("count");

        // Make maps
        if (lastUnittype == null || lastUnittype != unittype) {
          nameMap = new MapWrapper<Group>(strictOrder).getMap();
          idMap = new HashMap<>();
          unittype.setGroups(new Groups(idMap, nameMap, unittype));
          lastUnittype = unittype;
        }

        // Populate group object, make empty parent object if necessary
        // Populate maps
        Group parent = null;
        if (parentIdStr != null) {
          Integer parentId = Integer.valueOf(parentIdStr);
          parent = idMap.computeIfAbsent(parentId, Group::new);
        }
        Group thisGroup = idMap.computeIfAbsent(groupId, Group::new);
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
    }
  }

  private void readProfiles(Unittypes unittypes) throws SQLException {
    String sql = "SELECT unit_type_id, profile_id, profile_name FROM profile ORDER BY unit_type_id ASC";
    try(AutoCommitResettingConnectionWrapper connectionWrapper =
                new ReadConnectionWrapper(dataSource.getConnection());
        StatementWithTimeoutWrapper statementWrapper =
                new StatementWithTimeoutWrapper(connectionWrapper, 60);
        ResultSet resultSet =
                statementWrapper.getStatement().executeQuery(sql)) {
      Map<String, Profile> nameMap = null;
      Map<Integer, Profile> idMap = null;
      Unittype lastUnittype = null;
      int max = 0;
      int counter = 0;
      while (resultSet.next()) {
        counter++;
        Unittype unittype = unittypes.getById(resultSet.getInt("unit_type_id"));
        Integer profileId = resultSet.getInt("profile_id");
        if (profileId > max) {
          max = profileId;
        }
        String profileName = resultSet.getString("profile_name");
        Profile profile = new Profile(profileName, unittype);
        profile.setId(profileId);
        if (lastUnittype == null || lastUnittype != unittype) {
          nameMap = new MapWrapper<Profile>(strictOrder).getMap();
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
    }
  }

  private void readFilestore(Unittypes unittypes) throws SQLException {
    String sql = """
      SELECT unit_type_id, id, name, type, description, version, timestamp_, length(content) as length%s
      FROM filestore ORDER BY unit_type_id ASC
    """.formatted(ACSVersionCheck.fileReworkSupported ? ", target_name, owner " : "");
    try(AutoCommitResettingConnectionWrapper connectionWrapper =
                new ReadConnectionWrapper(dataSource.getConnection());
        StatementWithTimeoutWrapper statementWrapper =
                new StatementWithTimeoutWrapper(connectionWrapper, 60);
        ResultSet resultSet =
                statementWrapper.getStatement().executeQuery(sql)) {
      Map<Integer, File> idMap = null;
      Map<String, File> nameMap = null;
      TreeMap<String, File> versionTypeMap = null;
      Unittype lastUnittype = null;
      int counter = 0;
      while (resultSet.next()) {
        counter++;
        File file = new File();
        file.setValidateInput(false);
        Unittype unittype = unittypes.getById(resultSet.getInt("unit_type_id"));
        file.setUnittype(unittype);
        file.setId(resultSet.getInt("id"));
        file.setName(resultSet.getString("name"));
        String typeStr = resultSet.getString("type");
        file.setType(FileType.fromString(typeStr));
        file.setDescription(resultSet.getString("description"));
        file.setVersion(resultSet.getString("version"));
        file.setTimestamp(resultSet.getTimestamp("timestamp_"));
        file.setLength(resultSet.getInt("length"));
        String targetName = null;
        User owner = null;
        if (ACSVersionCheck.fileReworkSupported) {
          targetName = resultSet.getString("target_name");
          String userIdStr = resultSet.getString("owner");
          if (userIdStr != null) {
            try {
              owner = users.getUnprotected(Integer.valueOf(userIdStr));
            } catch (NumberFormatException ignored) {
              // ignore
            }
          }
        }
        file.setTargetName(targetName);
        file.setOwner(owner);
        file.setValidateInput(true);
        file.setConnectionProperties(getDataSource());
        file.resetContentToNull();
        if (lastUnittype == null || lastUnittype != unittype) {
          idMap = new HashMap<>();
          nameMap = new MapWrapper<File>(strictOrder).getMap();
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
    }
  }

  private Unittypes readUnittypes() throws SQLException {
    String sql = "SELECT * FROM unit_type";
    try(AutoCommitResettingConnectionWrapper connectionWrapper =
                new ReadConnectionWrapper(dataSource.getConnection());
        StatementWithTimeoutWrapper statementWrapper =
                new StatementWithTimeoutWrapper(connectionWrapper, 60);
        ResultSet resultSet =
                statementWrapper.getStatement().executeQuery(sql)) {
      MapWrapper<Unittype> mw = new MapWrapper<>(strictOrder);
      Map<String, Unittype> unittypeMap = mw.getMap();
      Map<Integer, Unittype> idMap = new HashMap<>();
      while (resultSet.next()) {
        Integer id = resultSet.getInt("unit_type_id");
        String unittypeName = resultSet.getString("unit_type_name");
        String vendorName = resultSet.getString("vendor_name");
        String desc = resultSet.getString("description");
        String protocol = resultSet.getString("protocol");
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
    }
  }

  private void readJobs(Unittypes unittypes) throws SQLException {
    String sql = "SELECT * FROM job j, group_ g WHERE j.group_id = g.group_id ORDER BY g.unit_type_id ASC, j.job_id_dependency ASC";
    try(AutoCommitResettingConnectionWrapper connectionWrapper =
                new ReadConnectionWrapper(dataSource.getConnection());
        StatementWithTimeoutWrapper statementWrapper =
                new StatementWithTimeoutWrapper(connectionWrapper, 60);
        ResultSet resultSet =
                statementWrapper.getStatement().executeQuery(sql)) {
      Map<Integer, Job> idMap = null;
      Map<String, Job> nameMap = null;
      Unittype lastUnittype = null;
      int jobCounter = 0;
      while (resultSet.next()) {
        jobCounter++;
        Job job = new Job();
        job.validateInput(false);
        Unittype unittype = unittypes.getById(resultSet.getInt("unit_type_id"));
        job.setUnittype(unittype);
        job.setId(resultSet.getInt("job_id"));
        job.setName(resultSet.getString("job_name"));
        job.setFlags(new JobFlag(resultSet.getString("job_type")));
        job.setDescription(resultSet.getString("description"));
        job.setGroup(unittype.getGroups().getById(resultSet.getInt("group_id")));
        job.setUnconfirmedTimeout(resultSet.getInt("unconfirmed_timeout"));
        job.setStopRules(resultSet.getString("stop_rules"));
        String statusStr = resultSet.getString("status");
        try {
          job.setStatus(JobStatus.valueOf(statusStr));
        } catch (Throwable t) { // Convert from old types
          if ("STOPPED".equals(statusStr)) {
            job.setStatus(JobStatus.PAUSED);
          }
        }
        job.setCompletedNoFailures(resultSet.getInt("completed_no_failure"));
        job.setCompletedHadFailures(resultSet.getInt("completed_had_failure"));
        job.setConfirmedFailed(resultSet.getInt("confirmed_failed"));
        job.setUnconfirmedFailed(resultSet.getInt("unconfirmed_failed"));
        job.setStartTimestamp(resultSet.getTimestamp("start_timestamp"));
        job.setEndTimestamp(resultSet.getTimestamp("end_timestamp"));
        String fileIdStr = resultSet.getString("firmware_id");
        if (fileIdStr != null) {
          job.setFile(unittype.getFiles().getById(Integer.parseInt(fileIdStr)));
        }
        String depIdStr = resultSet.getString("job_id_dependency");
        if (depIdStr != null) {
          Integer depId = Integer.parseInt(depIdStr);
          Job depJob = new Job();
          depJob.setId(depId);
          job.setDependency(depJob);
        }
        String repeatCountStr = resultSet.getString("repeat_count");
        if (repeatCountStr != null) {
          job.setRepeatCount(Integer.parseInt(repeatCountStr));
        }
        String repeatIntervalStr = resultSet.getString("repeat_interval");
        if (repeatIntervalStr != null) {
          job.setRepeatInterval(Integer.parseInt(repeatIntervalStr));
        }

        job.validateInput(true);

        // The job has been retrieved. put it in the map
        if (lastUnittype == null || lastUnittype != unittype) {
          idMap = new TreeMap<>();
          nameMap = new MapWrapper<Job>(strictOrder).getMap();
          unittype.setJobs(new Jobs(idMap, nameMap, unittype));
          lastUnittype = unittype;
        }
        idMap.put(job.getId(), job);
        nameMap.put(job.getName(), job);
      }

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
    }

    // Update job parameters
    sql = """
      SELECT ut.unit_type_id, jp.job_id, utp.unit_type_param_id, jp.value
      FROM job_param jp
      JOIN unit_type_param utp ON utp.unit_type_param_id = jp.unit_type_param_id
      JOIN unit_type ut ON utp.unit_type_id = ut.unit_type_id
      WHERE jp.unit_id = ?
      ORDER BY jp.job_id ASC
    """;
    try(AutoCommitResettingConnectionWrapper connectionWrapper =
                new ReadConnectionWrapper(dataSource.getConnection());
        DynamicStatementWrapper statementWrapper =
                new DynamicStatementWrapper(connectionWrapper, sql, Job.ANY_UNIT_IN_GROUP);
        ResultSet resultSet =
                statementWrapper.getPreparedStatement().executeQuery()) {
      int paramsCounter = 0;
      while (resultSet.next()) {
        paramsCounter++;
        Unittype unittype = unittypes.getById(resultSet.getInt("unit_type_id"));
        Job job = unittype.getJobs().getById(resultSet.getInt("job_id"));
        Integer unitTypeParamId = resultSet.getInt("unit_type_param_id");
        String value = resultSet.getString("value");
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
    }
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

  public String toString() {
    return "ACS";
  }
}
