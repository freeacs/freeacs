package com.github.freeacs.dbi;

import com.github.freeacs.dbi.exceptions.AcsException;
import com.github.freeacs.dbi.sql.AutoCommitResettingConnectionWrapper;
import com.github.freeacs.dbi.sql.DynamicStatementWrapper;
import com.github.freeacs.dbi.sql.ReadConnectionWrapper;
import com.github.freeacs.dbi.sql.WriteConnectionWrapper;
import com.github.freeacs.dbi.util.ACSVersionCheck;
import com.github.freeacs.dbi.util.SyslogClient;
import com.github.freeacs.dbi.util.SystemParameters;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.github.freeacs.dbi.util.NumberUtils.parseStringId;

@Slf4j
public class ACSDao {

    private static final String UNITTYPE_COLUMNS = """
        ut.unit_type_id,
        ut.unit_type_name,
        ut.vendor_name,
        ut.description as unit_type_description,
        ut.protocol
    """;

    private static final String UNITTYPE_PARAM_COLUMNS = """
        utp.unit_type_param_id,
        utp.unit_type_id,
        utp.name,
        utp.flags
    """;

    private static final Object PROFILE_COLUMNS = """
        p.profile_id,
        p.profile_name
    """;

    private static final String GET_UNITTYPES = """
        SELECT
            %s
        FROM unit_type ut
    """.formatted(UNITTYPE_COLUMNS);

    private static final String GET_UNITTYPE_BY_ID = """
        SELECT
            %s
        FROM unit_type ut
        WHERE ut.unit_type_id = ?
    """.formatted(UNITTYPE_COLUMNS);

    private static final String GET_UNITTYPE_BY_NAME = """
        SELECT
            %s
        FROM unit_type ut
        WHERE ut.unit_type_name = ?
    """.formatted(UNITTYPE_COLUMNS);

    private static final String GET_UNITTYPE_PARAMETERS_BY_UNITTYPE_ID = """
        SELECT
            %s
        FROM unit_type_param utp
        WHERE utp.unit_type_id = ?
    """.formatted(UNITTYPE_PARAM_COLUMNS);

    private static final String GET_UNITTYPE_PARAMETER_BY_UNITTYPE_PARAM_ID = """
        SELECT
            %s
        FROM unit_type_param utp
        WHERE utp.unit_type_param_id = ?
    """.formatted(UNITTYPE_PARAM_COLUMNS);

    private static final String GET_UNITTYPE_PARAMETER_BY_UNITTYPE_PARAM_NAME = """
        SELECT
            %s
        FROM unit_type_param utp
        WHERE utp.unit_type_id = ? AND utp.name = ?
    """.formatted(UNITTYPE_PARAM_COLUMNS);

    private static final String GET_PROFILE_PARAMETERS_BY_PROFILE_ID = """
        SELECT
            %s,
            %s,
            pp.value
        FROM profile_param AS pp
        JOIN unit_type_param AS utp ON pp.unit_type_param_id = utp.unit_type_param_id
        JOIN unit_type AS ut ON ut.unit_type_id = utp.unit_type_id
        WHERE pp.profile_id = ?
    """.formatted(UNITTYPE_COLUMNS, UNITTYPE_PARAM_COLUMNS);

    private static final String GET_JOB_PARAMETERS_BY_JOB_ID = """
        SELECT
            %s,
            %s,
            jp.job_id,
            jp.value
        FROM job_param AS jp
        JOIN unit_type_param AS utp ON jp.unit_type_param_id = utp.unit_type_param_id
        JOIN unit_type AS ut ON ut.unit_type_id = utp.unit_type_id
        WHERE jp.job_id = ?
    """.formatted(UNITTYPE_COLUMNS, UNITTYPE_PARAM_COLUMNS);

    private static final String GET_GROUP_PARAMETERS_BY_GROUP_ID = """
        SELECT
            %s,
            %s,
            gp.id,
            gp.group_id,
            gp.operator,
            gp.data_type,
            gp.value
        FROM group_param AS gp
        JOIN unit_type_param AS utp ON gp.unit_type_param_id = utp.unit_type_param_id
        JOIN unit_type AS ut ON ut.unit_type_id = utp.unit_type_id
        WHERE gp.group_id = ?
    """.formatted(UNITTYPE_COLUMNS, UNITTYPE_PARAM_COLUMNS);

    private static final String GROUP_COLUMNS_TEMPLATE = """
        {}.group_id,
        {}.group_name,
        {}.description as group_description,
        {}.parent_group_id,
        {}.count
    """;

    private static final String GET_GROUP_BY_ID = """
        WITH RECURSIVE ancestry AS (
          SELECT
            %s,
            %s,
            %s
          FROM group_ g
          JOIN unit_type ut ON g.unit_type_id = ut.unit_type_id
          LEFT JOIN profile p ON g.profile_id = p.profile_id
          WHERE g.group_id = ?
          UNION ALL
          SELECT
            %s,
            %s,
            %s
          FROM group_ i
          JOIN unit_type ut ON i.unit_type_id = ut.unit_type_id
          JOIN ancestry a ON i.group_id = a.parent_group_id
          LEFT JOIN profile p ON i.profile_id = p.profile_id
        )
        SELECT * FROM ancestry;
    """.formatted(
            GROUP_COLUMNS_TEMPLATE.replaceAll("\\{}", "g"),
            UNITTYPE_COLUMNS,
            PROFILE_COLUMNS,
            GROUP_COLUMNS_TEMPLATE.replaceAll("\\{}", "i"),
            UNITTYPE_COLUMNS,
            PROFILE_COLUMNS
    );

    public static final String GET_FILE_CONTENT_BY_ID = """
        SELECT content
        FROM filestore
        WHERE id = ?
    """;

    public static final String GET_FILES = """
        SELECT
          unit_type_id,
          id,
          name,
          type,
          description,
          version,
          timestamp_,
          length(content) as length{}
        FROM filestore
    """;

    public static final String GET_FILE_BY_ID = """
        %s
        WHERE id = ?
    """.formatted(GET_FILES);

    public static final String GET_FILE_BY_TYPE_AND_VERSION = """
        %s
        WHERE unit_type_id = ? AND type = ? AND version = ?
        ORDER BY unit_type_id ASC
    """.formatted(GET_FILES);

    private static final Object JOB_COLUMNS = """
        j.job_id,
        j.job_name,
        j.job_type,
        j.description,
        j.group_id,
        j.unconfirmed_timeout,
        j.stop_rules,
        j.status,
        j.completed_no_failure,
        j.completed_had_failure,
        j.confirmed_failed,
        j.unconfirmed_failed,
        j.start_timestamp,
        j.end_timestamp,
        j.firmware_id,
        j.job_id_dependency,
        j.repeat_count,
        j.repeat_interval
    """;

    public static String GET_JOBS = """
        SELECT
            %s,
            %s,
            %s,
            %s
        FROM job j
        LEFT JOIN group_ g ON j.group_id = g.group_id
        LEFT JOIN unit_type ut ON g.unit_type_id = ut.unit_type_id
        LEFT JOIN profile p ON g.profile_id = p.profile_id
    """.formatted(
            JOB_COLUMNS,
            GROUP_COLUMNS_TEMPLATE.replaceAll("\\{}", "g"),
            UNITTYPE_COLUMNS,
            PROFILE_COLUMNS
    );

    private static final String GET_PROFILE_BY_ID = """
        SELECT
            %s,
            %s
        FROM profile p
        JOIN unit_type ut ON p.unit_type_id = ut.unit_type_id
        WHERE p.profile_id = ?
    """.formatted(UNITTYPE_COLUMNS, PROFILE_COLUMNS);


    private final DataSource dataSource;
    private final Syslog syslog;

    public ACSDao(DataSource dataSource, Syslog syslog) {
        this.dataSource = dataSource;
        this.syslog = syslog;
    }

    public List<Unittype> getUnitTypes() {
        try (var connection = new ReadConnectionWrapper(dataSource.getConnection());
             var statement = new DynamicStatementWrapper(connection, GET_UNITTYPES);
             var resultSet = statement.getPreparedStatement().executeQuery()) {
            var unittypes = new ArrayList<Unittype>();
            while (resultSet.next()) {
                var unittype = parseUnittype(resultSet);
                var unittypeParametersList = getUnittypeParametersByUnitTypeId(unittype.getId());
                var unittypeParameters = new UnittypeParameters(unittypeParametersList, unittype);
                unittype.setUnittypeParameters(unittypeParameters);
                unittypes.add(unittype);
            }
            return unittypes;
        } catch (SQLException e) {
            throw new AcsException("Failed to fetch unit types", e);
        }
    }

    public Unittype getUnitTypeById(int unitTypeId) {
        return getUnitTypeById(unitTypeId, true);
    }

    public Unittype getUnitTypeById(int unitTypeId, boolean includeParameters) {
        if (unitTypeId == 0) {
            throw new IllegalArgumentException("unitTypeId cannot be null");
        }
        try (var connection = new ReadConnectionWrapper(dataSource.getConnection());
             var statement = new DynamicStatementWrapper(connection, GET_UNITTYPE_BY_ID, unitTypeId);
             var resultSet = statement.getPreparedStatement().executeQuery()) {
            if (resultSet.next()) {
                var unittype = parseUnittype(resultSet);
                if (includeParameters) {
                    var unittypeParametersList = getUnittypeParametersByUnitTypeId(unittype.getId());
                    var unittypeParameters = new UnittypeParameters(unittypeParametersList, unittype);
                    unittype.setUnittypeParameters(unittypeParameters);
                }
                return unittype;
            } else {
                log.debug("No unittype found with id {}", unitTypeId);
                return null;
            }
        } catch (SQLException e) {
            throw new AcsException("Failed to fetch unittype by id: %s", e, unitTypeId);
        }
    }

    public Unittype getUnitTypeByName(String unitTypeName) {
        return getUnitTypeByName(unitTypeName, true);
    }

    public Unittype getUnitTypeByName(String unitTypeName, boolean includeParameters) {
        if (unitTypeName == null) {
            throw new IllegalArgumentException("unitTypeName cannot be null");
        }
        try (var connection = new ReadConnectionWrapper(dataSource.getConnection());
             var statement = new DynamicStatementWrapper(connection, GET_UNITTYPE_BY_NAME, unitTypeName);
             var resultSet = statement.getPreparedStatement().executeQuery()) {
            if (resultSet.next()) {
                var unittype = parseUnittype(resultSet);
                if (includeParameters) {
                    var unittypeParametersList = getUnittypeParametersByUnitTypeId(unittype.getId());
                    var unittypeParameters = new UnittypeParameters(unittypeParametersList, unittype);
                    unittype.setUnittypeParameters(unittypeParameters);
                }
                return unittype;
            } else {
                log.debug("No unittype found with id {}", unitTypeName);
                return null;
            }
        } catch (SQLException e) {
            throw new AcsException("Failed to fetch unittype by name: %s", e, unitTypeName);
        }
    }

    public List<UnittypeParameter> getUnittypeParametersByUnitTypeId(int unitTypeId) {
        if (unitTypeId == 0) {
            throw new IllegalArgumentException("unitTypeId cannot be null");
        }
        var unittype = getUnitTypeById(unitTypeId, false);
        try (var connection = new ReadConnectionWrapper(dataSource.getConnection());
             var statement = new DynamicStatementWrapper(connection, GET_UNITTYPE_PARAMETERS_BY_UNITTYPE_ID, unitTypeId);
             var resultSet = statement.getPreparedStatement().executeQuery()) {
            var unittypeParameters = new ArrayList<UnittypeParameter>();
            while (resultSet.next()) {
                var unitTypeParam = parseUnittypeParameter(resultSet, unittype);
                unittypeParameters.add(unitTypeParam);
            }
            return unittypeParameters;
        } catch (SQLException e) {
            throw new AcsException("Failed to fetch unittype parameters by unittype id: %s", e, unitTypeId);
        }
    }

    public UnittypeParameter getUnittypeParameterByUnitTypeParameId(int unitTypeParamId) {
        if (unitTypeParamId == 0) {
            throw new IllegalArgumentException("unitTypeParamId cannot be null");
        }
        try (var connection = new ReadConnectionWrapper(dataSource.getConnection());
             var statement = new DynamicStatementWrapper(connection, GET_UNITTYPE_PARAMETER_BY_UNITTYPE_PARAM_ID, unitTypeParamId);
             var resultSet = statement.getPreparedStatement().executeQuery()) {
            if (resultSet.next()) {
                return parseUnittypeParameter(resultSet, null);
            }
            return null;
        } catch (SQLException e) {
            throw new AcsException("Failed to fetch unittype parameters by unittype param id: %s", e, unitTypeParamId);
        }
    }

    public UnittypeParameter getUnittypeParameterByUnitTypeParameName(Integer unitTypeId, String unitTypeParamName) {
        if (unitTypeParamName == null) {
            throw new IllegalArgumentException("unitTypeParamName cannot be null");
        }
        try (var connection = new ReadConnectionWrapper(dataSource.getConnection());
             var statement = new DynamicStatementWrapper(connection, GET_UNITTYPE_PARAMETER_BY_UNITTYPE_PARAM_NAME, unitTypeId, unitTypeParamName);
             var resultSet = statement.getPreparedStatement().executeQuery()) {
            if (resultSet.next()) {
                return parseUnittypeParameter(resultSet, null);
            }
            return null;
        } catch (SQLException e) {
            throw new AcsException("Failed to fetch unittype parameters by unittype param name: %s", e, unitTypeParamName);
        }
    }


    public Group getGroupById(int groupId) {
        if (groupId == 0) {
            throw new IllegalArgumentException("groupId cannot be null");
        }
        try(var connection = new ReadConnectionWrapper(dataSource.getConnection());
            var statement = new DynamicStatementWrapper(connection, GET_GROUP_BY_ID, groupId);
            var resultSet = statement.getPreparedStatement().executeQuery()) {
            if (resultSet.next()) {
                return parseGroup(resultSet, new HashMap<>());
            } else {
                log.debug("No group found with id {}", groupId);
                return null;
            }
        } catch (SQLException e) {
            throw new AcsException("Failed to fetch group by id: %s", e, groupId);
        }
    }
    
    public Profile getProfileById(int profileId) {
        return getProfileById(profileId, true);
    }

    public Profile getProfileById(int profileId, boolean includeParameters) {
        if (profileId == 0) {
            throw new IllegalArgumentException("profileId cannot be null");
        }
        try(var connection = new ReadConnectionWrapper(dataSource.getConnection());
            var statement = new DynamicStatementWrapper(connection, GET_PROFILE_BY_ID, profileId);
            var resultSet = statement.getPreparedStatement().executeQuery()) {
            if (resultSet.next()) {
                var profile = parseProfile(resultSet);
                if (includeParameters) {
                    var profileParametersList = getProfileParametersByProfileId(profile.getId());
                    var profileParameters = new ProfileParameters(profileParametersList, profile);
                    profile.setProfileParameters(profileParameters);
                }
                return profile;
            } else {
                log.debug("No profile found with id {}", profileId);
                return null;
            }
        } catch (SQLException e) {
            throw new AcsException("Failed to fetch profile by id: %s", e, profileId);
        }
    }

    public Job getJobById(int jobId) {
        if (jobId == 0) {
            throw new IllegalArgumentException("jobId cannot be null");
        }
        try(var connection = new ReadConnectionWrapper(dataSource.getConnection());
            var statement = new DynamicStatementWrapper(connection, GET_JOBS + " WHERE j.job_id = ?", jobId);
            var resultSet = statement.getPreparedStatement().executeQuery()) {
            if (resultSet.next()) {
                return parseJob(resultSet);
            } else {
                log.debug("No job found with id {}", jobId);
                return null;
            }
        } catch (SQLException e) {
            throw new AcsException("Failed to fetch job by id: %s", e, jobId);
        }
    }

    private Job parseJob(ResultSet resultSet) throws SQLException {
        var job = new Job();
        job.setName(resultSet.getString("job_name"));
        job.setId(resultSet.getInt("job_id"));
        job.setDescription(resultSet.getString("description"));
        var groupId = resultSet.getInt("group_id");
        if (groupId != 0) {
            job.setGroup(parseGroup(resultSet, new HashMap<>()));
            // a group is always associated with a unittype
            job.setUnittype(parseUnittype(resultSet));
        }
        job.setFlags(new JobFlag(resultSet.getString("job_type")));
        job.setUnconfirmedTimeout(resultSet.getInt("unconfirmed_timeout"));
        job.setStopRules(resultSet.getString("stop_rules"));
        job.setStatus(JobStatus.valueOf(resultSet.getString("status")));
        job.setCompletedNoFailures(resultSet.getInt("completed_no_failure"));
        job.setCompletedHadFailures(resultSet.getInt("completed_had_failure"));
        job.setConfirmedFailed(resultSet.getInt("confirmed_failed"));
        job.setUnconfirmedFailed(resultSet.getInt("unconfirmed_failed"));
        job.setStartTimestamp(resultSet.getTimestamp("start_timestamp"));
        job.setEndTimestamp(resultSet.getTimestamp("end_timestamp"));
        var jobIdDependency = resultSet.getInt("job_id_dependency");
        if (jobIdDependency != 0) {
            var jobDependency = getJobById(jobIdDependency);
            job.setDependency(jobDependency);
        }
        var firmwareId = resultSet.getInt("firmware_id");
        if (firmwareId != 0) {
            job.setFile(getFileById(resultSet.getInt("firmware_id")));
        }
        job.setRepeatCount(resultSet.getInt("repeat_count"));
        job.setRepeatInterval(resultSet.getInt("repeat_interval"));
        return job;
    }

    public List<GroupParameter> getGroupParametersByGroupId(int groupId) {
        if (groupId == 0) {
            throw new IllegalArgumentException("groupId cannot be null");
        }
        var group = getGroupById(groupId);
        try(var connection = new ReadConnectionWrapper(dataSource.getConnection());
            var statement = new DynamicStatementWrapper(connection, GET_GROUP_PARAMETERS_BY_GROUP_ID, groupId);
            var resultSet = statement.getPreparedStatement().executeQuery()) {
            var groupParameters = new ArrayList<GroupParameter>();
            while (resultSet.next()) {
                var unittype = parseUnittype(resultSet);
                var unittypeParameter = parseUnittypeParameter(resultSet, unittype);
                var value = resultSet.getString("value");
                var operator = resultSet.getString("operator");
                var op = Parameter.Operator.getOperator(operator);
                var dataType = resultSet.getString("data_type");
                var pdt = Parameter.ParameterDataType.getDataType(dataType);
                var parameter = new Parameter(unittypeParameter, value, op, pdt);
                var groupParameter = new GroupParameter(parameter, group);
                groupParameter.setId(resultSet.getInt("id"));
                groupParameters.add(groupParameter);
            }
            return groupParameters;
        } catch (SQLException e) {
            throw new AcsException("Failed to fetch group parameters by group id: %s", e, groupId);
        }
    }

    public List<ProfileParameter> getProfileParametersByProfileId(int profileId) {
        if (profileId == 0) {
            throw new IllegalArgumentException("profileId cannot be null");
        }
        var profile = getProfileById(profileId, false);
        try(var connection = new ReadConnectionWrapper(dataSource.getConnection());
            var statement = new DynamicStatementWrapper(connection, GET_PROFILE_PARAMETERS_BY_PROFILE_ID, profileId);
            var resultSet = statement.getPreparedStatement().executeQuery()) {
            var profileParameters = new ArrayList<ProfileParameter>();
            while (resultSet.next()) {
                var unittype = parseUnittype(resultSet);
                var unittypeParameter = parseUnittypeParameter(resultSet, unittype);
                var value = resultSet.getString("value");
                var profileParameter = new ProfileParameter(profile, unittypeParameter, value);
                profileParameters.add(profileParameter);
            }
            return profileParameters;
        } catch (SQLException e) {
            throw new AcsException("Failed to fetch profile parameters by profile id: %s", e, profileId);
        }
    }

    public List<JobParameter> getJobParametersByJobId(int jobId) {
        if (jobId == 0) {
            throw new IllegalArgumentException("jobId cannot be null");
        }
        var job = getJobById(jobId);
        try(var connection = new ReadConnectionWrapper(dataSource.getConnection());
            var statement = new DynamicStatementWrapper(connection, GET_JOB_PARAMETERS_BY_JOB_ID, jobId);
            var resultSet = statement.getPreparedStatement().executeQuery()) {
            var jobParameters = new ArrayList<JobParameter>();
            while (resultSet.next()) {
                var unittype = parseUnittype(resultSet);
                var unittypeParameter = parseUnittypeParameter(resultSet, unittype);
                var value = resultSet.getString("value");
                var param = new Parameter(unittypeParameter, value);
                var jobParameter = new JobParameter(job, Job.ANY_UNIT_IN_GROUP, param);
                jobParameters.add(jobParameter);
            }
            return jobParameters;
        } catch (SQLException e) {
            throw new AcsException("Failed to fetch job parameters by job id: %s", e, jobId);
        }
    }

    public File getFileByUnitTypeIdAndFileTypeAndVersion(Unittype unittype, FileType fileType, String firmwareVersion) throws SQLException {
        if (unittype == null) {
            throw new IllegalArgumentException("unittype cannot be null");
        }
        if (fileType == null) {
            throw new IllegalArgumentException("fileType cannot be null");
        }
        if (firmwareVersion == null) {
            throw new IllegalArgumentException("firmwareVersion cannot be null");
        }
        var sql = GET_FILE_BY_TYPE_AND_VERSION.replace("{}", ACSVersionCheck.fileReworkSupported ? ", target_name, owner " : "");
        try(var connectionWrapper = new ReadConnectionWrapper(dataSource.getConnection());
            var statementWrapper = new DynamicStatementWrapper(connectionWrapper, sql, unittype.getId(), fileType.name(), firmwareVersion);
            var resultSet = statementWrapper.getPreparedStatement().executeQuery()) {
            if (resultSet.next()) {
                return parseFile(unittype, resultSet);
            }
            log.debug("Found 0 files for unittype {} and firmware version {}", unittype.getName(), firmwareVersion);
            return null;
        }
    }

    public File getFileById(int fileId) throws SQLException {
        if (fileId == 0) {
            throw new IllegalArgumentException("fileId cannot be null");
        }
        var sql = GET_FILE_BY_ID.replace("{}", ACSVersionCheck.fileReworkSupported ? ", target_name, owner " : "");
        try(var connectionWrapper = new ReadConnectionWrapper(dataSource.getConnection());
            var statementWrapper = new DynamicStatementWrapper(connectionWrapper, sql, fileId);
            var resultSet = statementWrapper.getPreparedStatement().executeQuery()) {
            if (resultSet.next()) {
                return parseFile(null, resultSet);
            }
            log.debug("No file found with id {}", fileId);
            return null;
        }
    }

    private File parseFile(Unittype unittype, ResultSet resultSet) throws SQLException {
        File file = new File();
        file.setValidateInput(false);
        if (unittype != null) {
            file.setUnittype(unittype);
        } else {
            file.setUnittype(getUnitTypeById(resultSet.getInt("unit_type_id")));
        }
        file.setId(resultSet.getInt("id"));
        file.setName(resultSet.getString("name"));
        file.setType(FileType.fromString(resultSet.getString("type")));
        file.setDescription(resultSet.getString("description"));
        file.setVersion(resultSet.getString("version"));
        file.setTimestamp(resultSet.getTimestamp("timestamp_"));
        file.setLength(resultSet.getInt("length"));
        if (ACSVersionCheck.fileReworkSupported) {
            file.setTargetName(resultSet.getString("target_name"));
            parseStringId(resultSet.getString("owner"))
                    .map(id -> new User().withId(id))
                    .ifPresent(file::setOwner);
        }
        file.setValidateInput(true);
        file.setConnectionProperties(dataSource);
        file.resetContentToNull();
        return file;
    }

    public byte[] getFileContents(int fileId) throws SQLException {
        if (fileId == 0) {
            throw new IllegalArgumentException("fileId cannot be null");
        }
        try(var connectionWrapper =
                    new ReadConnectionWrapper(dataSource.getConnection());
            var statementWrapper =
                    new DynamicStatementWrapper(connectionWrapper, GET_FILE_CONTENT_BY_ID, fileId);
            var resultSet = statementWrapper.getPreparedStatement().executeQuery()) {

            if (resultSet.next()) {
                Blob blob = resultSet.getBlob("content");
                return blob.getBytes(1, (int) blob.length());
            } else {
                log.debug("No content found for file with id {}", fileId);
                return null;
            }
        }
    }

    private static Unittype parseUnittype(ResultSet resultSet) throws SQLException {
        var unitTypeName = resultSet.getString("unit_type_name");
        var vendorName = resultSet.getString("vendor_name");
        var unitTypeDescription = resultSet.getString("unit_type_description");
        var protocol = Unittype.ProvisioningProtocol.valueOf(resultSet.getString("protocol"));
        var unittype = new Unittype(unitTypeName, vendorName, unitTypeDescription, protocol);
        var unitTypeId = resultSet.getInt("unit_type_id");
        unittype.setId(unitTypeId);
        return unittype;
    }

    private static UnittypeParameter parseUnittypeParameter(ResultSet resultSet, Unittype unittype) throws SQLException {
        var name = resultSet.getString("name");
        var param = new UnittypeParameterFlag(resultSet.getString("flags"));
        var unitTypeParam = new UnittypeParameter();
        unitTypeParam.setUnittype(unittype);
        unitTypeParam.setName(name);
        unitTypeParam.setFlag(param);
        unitTypeParam.setId(resultSet.getInt("unit_type_param_id"));
        return unitTypeParam;
    }

    private static Profile parseProfile(ResultSet resultSet) throws SQLException {
        var unitType = parseUnittype(resultSet);
        var profile = new Profile(resultSet.getString("profile_name"), unitType);
        var profileId = resultSet.getInt("profile_id");
        profile.setId(profileId);
        return profile;
    }

    private Group parseGroup(ResultSet resultSet, HashMap<Integer, Group> groupCache) throws SQLException {
        var group = new Group(resultSet.getInt("group_id"));
        group.setName(resultSet.getString("group_name"));
        group.setDescription(resultSet.getString("group_description"));
        group.setUnittype(parseUnittype(resultSet));
        var profileId = resultSet.getInt("profile_id");
        if (profileId != 0) {
            var profile = parseProfile(resultSet);
            group.setProfile(profile);
        }
        group.setCount(resultSet.getInt("count"));
        groupCache.put(group.getId(), group);
        int parentGroupId = resultSet.getInt("parent_group_id");
        if (parentGroupId != 0 && resultSet.next()) {
            var parentGroup = groupCache.containsKey(parentGroupId)
                    ? groupCache.get(parentGroupId)
                    : groupCache.put(parentGroupId, parseGroup(resultSet, groupCache));
            group.setParent(parentGroup);
        }
        return group;
    }

    public void addOrChangeUnitParameters(List<UnitParameter> unitSessionParameters) throws SQLException {
        addOrChangeUnitParameters(unitSessionParameters, false);
    }

    public void addOrChangeSessionUnitParameters(List<UnitParameter> unitSessionParameters) throws SQLException {
        if (ACSVersionCheck.unitParamSessionSupported) {
            addOrChangeUnitParameters(unitSessionParameters, true);
        }
    }

    private void addOrChangeUnitParameters(List<UnitParameter> unitParameters, boolean session) throws SQLException {
        String tableName = session ? "unit_param_session" : "unit_param";
        try(var connection = new AutoCommitResettingConnectionWrapper(dataSource.getConnection(), false, true)) {
            try {
                for (int i = 0; unitParameters != null && i < unitParameters.size(); i++) {
                    UnitParameter unitParameter = unitParameters.get(i);
                    String unitId = unitParameter.getUnitId();
                    Parameter parameter = unitParameter.getParameter();
                    if (parameter.getValue() != null && parameter.getValue().length() > 512) {
                        parameter.setValue(parameter.getValue().substring(0, 509) + "...");
                    }
                    String value = parameter.getValue(); // will be "" if value was null
                    String utpName = parameter.getUnittypeParameter().getName();
                    String action = "Added";
                    String sql = "INSERT INTO " + tableName + " (value, unit_id, unit_type_param_id) VALUES (?, ?, ?)";
                    try {
                        executeSql(sql, connection.getConnection(), parameter.getUnittypeParameter(), value, unitId);
                    } catch (SQLException insertEx) {
                        sql = "UPDATE " + tableName + " SET value = ? WHERE unit_id = ? AND unit_type_param_id = ?";
                        int rowsupdated = executeSql(sql, connection.getConnection(), parameter.getUnittypeParameter(), value, unitId);
                        if (rowsupdated == 0) {
                            throw insertEx;
                        }
                        action = "Updated";
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
                    log.info(msg);
                    if (i > 0 && i % 100 == 0) {
                        connection.getConnection().commit();
                    }
                }
                connection.getConnection().commit();
            } catch (SQLException sqle) {
                connection.getConnection().rollback();
                throw sqle;
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
                    log.info("Added unit " + unitId);
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
                        log.info("Moved unit " + unitId + " to profile " + profile.getName());
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

    public void addOrChangeQueuedUnitParameters(Unit unit) throws SQLException {
        List<UnitParameter> queuedParameters = unit.flushWriteQueue();
        // TODO necessary to check if the value is the same as the one already stored in the DB?
        Iterator<UnitParameter> iterator = queuedParameters.iterator();
        while (iterator.hasNext()) {
            UnitParameter queuedUp = iterator.next();
            UnitParameter storedUp =
                    unit.getUnitParameters().get(queuedUp.getParameter().getUnittypeParameter().getName());
            if (storedUp != null
                    && storedUp.getValue() != null
                    && storedUp.getValue().equals(queuedUp.getValue())) {
                iterator.remove(); // don't write the queued Unit Parameter if it has the same value as already stored
            }
        }
        addOrChangeUnitParameters(queuedParameters);
    }

    /**
     * @param unittype - may be null
     * @param profile - may be null
     * @return a unit object will all unit parameters found
     */
    public Unit getUnitById(
            String unitId,
            Unittype unittype,
            Profile profile,
            Function<Integer, Unittype> getUnittypeFunction,
            Supplier<List<Unittype>> getUnittypesFunction,
            Function<Integer, UnittypeParameter> getUnittypeParameterFunction,
            Function<Integer, Profile> getProfileFunction) throws SQLException {
        Connection connection = null;
        boolean wasAutoCommit = false;
        try {
            connection = dataSource.getConnection();
            wasAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            // Passing null for acs since we are not using functions that require it
            UnitQueryCrossUnittype uqcu = new UnitQueryCrossUnittype(connection, null, syslog, unittype, profile, getUnittypeFunction, getUnittypesFunction, getUnittypeParameterFunction, getProfileFunction);
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

    public Unit getUnitById(String unitId, Unittype unittype, Profile profile) throws SQLException {
        return getUnitById(unitId, unittype, profile, (unitTypeId) -> this.getUnitTypeById(unitTypeId, true), this::getUnitTypes, this::getUnittypeParameterByUnitTypeParameId, (profileId) -> this.getProfileById(profileId, true));
    }

    public Unit getUnitById(
            String unitId,
            Function<Integer, Unittype> getUnittypeFunction,
            Supplier<List<Unittype>> getUnittypesFunction,
            Function<Integer, UnittypeParameter> getUnittypeParamFunction,
            Function<Integer, Profile> getProfileFunction) throws SQLException {
        return getUnitById(unitId, null, null, getUnittypeFunction, getUnittypesFunction, getUnittypeParamFunction, getProfileFunction);
    }

    public List<Job> getJobsByUnitTypeId(Integer unitTypeId) {
        if (unitTypeId == 0) {
            throw new IllegalArgumentException("unitTypeId cannot be null");
        }
        try(var connection = new ReadConnectionWrapper(dataSource.getConnection());
            var statement = new DynamicStatementWrapper(connection, GET_JOBS + " WHERE ut.unit_type_id = ?", unitTypeId);
            var resultSet = statement.getPreparedStatement().executeQuery()) {
            var jobs = new ArrayList<Job>();
            while (resultSet.next()) {
                jobs.add(parseJob(resultSet));
            }
            return jobs;
        } catch (SQLException e) {
            throw new AcsException("Failed to fetch jobs by unit type id: %s", e, unitTypeId);
        }
    }

    public void addOrChangeUnittype(Unittype ut) {
        if (ut.getId() == null) {
            addUnittype(ut);
        } else {
            changeUnittype(ut);
        }
    }

    private void changeUnittype(Unittype ut) {
        try(var connection = new WriteConnectionWrapper(dataSource.getConnection());
            var statement = new DynamicStatementWrapper(connection, "UPDATE unit_type SET unit_type_name = ?, vendor_name = ?, description = ?, protocol = ? WHERE unit_type_id = ?", ut.getName(), ut.getVendor(), ut.getDescription(), ut.getProtocol().name(), ut.getId());
            var resultSet = statement.getPreparedStatement().executeQuery()) {
            if (!resultSet.next()) {
                throw new AcsException("Failed to change unit type: %s. It does not exist.", ut);
            }
            ensureValidSystemParameters(ut);
        } catch (SQLException e) {
            throw new AcsException("Failed to change unit type: %s", e, ut);
        }
    }

    private void addUnittype(Unittype ut) {
        try(var connection = new WriteConnectionWrapper(dataSource.getConnection());
            var statement = new DynamicStatementWrapper(connection, "INSERT INTO unit_type (unit_type_name, vendor_name, description, protocol) VALUES (?,?,?,?)", ut.getName(), ut.getVendor(), ut.getDescription(), ut.getProtocol().name());
            var resultSet = statement.getPreparedStatement().executeQuery()) {
            if (!resultSet.next()) {
                throw new AcsException("Failed to add unit type: %s", ut);
            }
            var gk = statement.getPreparedStatement().getGeneratedKeys();
            if (!gk.next()) {
                throw new AcsException("Unable to retrieve autogenerated id for: %s", ut);
            }
            ut.setId(gk.getInt(1));
            ensureValidSystemParameters(ut);
        } catch (SQLException e) {
            throw new AcsException("Failed to add unit type: %s", e, ut);
        }
    }

    public void ensureValidSystemParameters(Unittype ut) {
        List<UnittypeParameter> utpList = new ArrayList<>();
        for (Map.Entry<String, UnittypeParameterFlag> entry :
                SystemParameters.commonParameters.entrySet()) {
            UnittypeParameter utp = getUnittypeParameterByUnitTypeParameName(ut.getId(), entry.getKey());
            UnittypeParameterFlag newFlag = entry.getValue();
            if (utp == null) {
                utpList.add(new UnittypeParameter(ut, entry.getKey(), entry.getValue()));
            } else if (!utp.getFlag().isSystem()) {
                utp.setFlag(newFlag);
                utpList.add(utp);
            }
        }
        addOrChangeUnittypeParameters(ut, utpList);
    }

    public void addOrChangeUnittypeParameters(Unittype ut, List<UnittypeParameter> utpList) {
        try (var connection = new WriteConnectionWrapper(dataSource.getConnection())) {
            for (UnittypeParameter utp : utpList) {
                if (utp.getId() == null) {
                    // Perform insert operation
                    try (var statement = new DynamicStatementWrapper(connection, "INSERT INTO unit_type_param (unit_type_id, name, flags) VALUES (?, ?, ?)", ut.getId(), utp.getName(), utp.getFlag().toString());
                         var resultSet = statement.getPreparedStatement().executeQuery()) {
                        if (!resultSet.next()) {
                            throw new AcsException("Failed to insert unit type parameter: %", utp.getName());
                        }
                    }
                } else {
                    // Perform update operation
                    try (var statement = new DynamicStatementWrapper(connection, "UPDATE unit_type_param SET flags = ? WHERE unit_type_id = ? AND name = ?", utp.getFlag().toString(), ut.getId(), utp.getName());
                         var resultSet = statement.getPreparedStatement().executeQuery()) {
                        if (!resultSet.next()) {
                            throw new AcsException("Failed to update unit type parameter: %s", utp.getName());
                        }
                    }
                }
            }
            connection.getConnection().commit();
        } catch (SQLException e) {
            throw new AcsException("Failed to add or change unit type parameters", e);
        }
    }
}
