package com.github.freeacs.dbi;

import com.github.freeacs.dbi.sql.AutoCommitResettingConnectionWrapper;
import com.github.freeacs.dbi.sql.DynamicStatementWrapper;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
public class ACSDao {

    private static final String GET_UNITTYPE_BY_ID = """
        SELECT
            unit_type_id,
            unit_type_name,
            vendor_name,
            description as unit_type_description,
            protocol
        FROM unit_type
        WHERE unit_type_id = ?
    """;

    private static final String GET_UNITTYPE_BY_NANE = """
        SELECT
            unit_type_id,
            unit_type_name,
            vendor_name,
            description as unit_type_description,
            protocol
        FROM unit_type
        WHERE unit_type_name = ?
    """;

    private static final String GET_UNITTYPE_PARAMETERS_BY_UNITTYPE_ID = """
        SELECT
            unit_type_param_id,
            unit_type_id,
            name,
            flags
        FROM unit_type_param
        WHERE unit_type_id = ?
    """;

    private static final String GET_PROFILE_PARAMETERS_BY_PROFILE_ID = """
        SELECT
            ut.unit_type_id,
            pp.unit_type_param_id,
            pp.value
        FROM profile_param AS pp
        JOIN unit_type_param AS utp ON pp.unit_type_param_id = utp.unit_type_param_id
        JOIN unit_type AS ut ON ut.unit_type_id = utp.unit_type_id
        WHERE pp.profile_id = ?
    """;

    private static final String GET_JOB_PARAMETERS_BY_JOB_ID = """
        SELECT
            jp.job_id,
            ut.unit_type_id,
            jp.unit_type_param_id,
            jp.value
        FROM job_param AS jp
        JOIN unit_type_param AS utp ON jp.unit_type_param_id = utp.unit_type_param_id
        JOIN unit_type AS ut ON ut.unit_type_id = utp.unit_type_id
        WHERE jp.job_id = ?
    """;

    private static final String GET_GROUP_PARAMETERS_BY_GROUP_ID = """
        SELECT
            ut.unit_type_id,
            gp.id,
            gp.group_id,
            gp.unit_type_param_id,
            gp.operator,
            gp.data_type,
            gp.value
        FROM group_param AS gp
        JOIN unit_type_param AS utp ON gp.unit_type_param_id = utp.unit_type_param_id
        JOIN unit_type AS ut ON ut.unit_type_id = utp.unit_type_id
        WHERE gp.group_id = ?
    """;

    private static final String GET_GROUP_BY_ID = """
        WITH RECURSIVE ancestry AS (
          SELECT
            g.group_id,
            g.unit_type_id,
            g.group_name,
            g.description as group_description,
            g.parent_group_id,
            g.profile_id,
            g.count,
            u.unit_type_name,
            u.vendor_name,
            u.description as unit_type_description,
            u.protocol
          FROM group_ g
          JOIN unit_type u ON g.unit_type_id = u.unit_type_id
          WHERE g.group_id = ?
          UNION ALL
          SELECT
            i.group_id,
            i.unit_type_id,
            i.group_name,
            i.description as group_description,
            i.parent_group_id,
            i.profile_id,
            i.count,
            u.unit_type_name,
            u.vendor_name,
            u.description as unit_type_description,
            u.protocol
          FROM group_ i
          JOIN unit_type u ON i.unit_type_id = u.unit_type_id
          JOIN ancestry a ON i.group_id = a.parent_group_id
        )
        SELECT * FROM ancestry;
    """;

    public static String GET_JOB_BY_ID = """
        SELECT
            job_id,
            job_name,
            description,
            group_id,
            unconfirmed_timeout,
            stop_rules,
            status,
            completed_no_failure,
            completed_had_failure,
            confirmed_failed,
            unconfirmed_failed,
            start_timestamp,
            end_timestamp,
            job_id_dependency,
            repeat_count,
            repeat_interval
        FROM job
        WHERE job_id = ?
    """;

    private static final String GET_PROFILE_BY_ID = """
        SELECT
            p.profile_id,
            p.unit_type_id,
            p.profile_name,
            u.unit_type_name,
            u.vendor_name,
            u.description as unit_type_description,
            u.protocol
        FROM profile p
        JOIN unit_type u ON p.unit_type_id = u.unit_type_id
        WHERE p.profile_id = ?
    """;


    private final DataSource dataSource;

    public ACSDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Unittype getUnitTypeById(int unitTypeId) {
        if (unitTypeId == 0) {
            throw new IllegalArgumentException("unitTypeId cannot be null");
        }
        try (var connection = new AutoCommitResettingConnectionWrapper(dataSource.getConnection(), false);
             var statement = new DynamicStatementWrapper(connection, GET_UNITTYPE_BY_ID, unitTypeId);
             var resultSet = statement.getPreparedStatement().executeQuery()) {
            if (resultSet.next()) {
                var unittype = parseUnittype(resultSet);
                unittype.setId(unitTypeId);
                return unittype;
            } else {
                log.warn("No unittype found with id {}", unitTypeId);
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static Unittype parseUnittype(ResultSet resultSet) throws SQLException {
        var unittype = new Unittype(
                resultSet.getString("unit_type_name"),
                resultSet.getString("vendor_name"),
                resultSet.getString("unit_type_description"),
                Unittype.ProvisioningProtocol.valueOf(resultSet.getString("protocol"))
        );
        var unitTypeId = resultSet.getInt("unit_type_id");
        unittype.setId(unitTypeId);
        return unittype;
    }

    public Unittype getUnitTypeByName(String unitTypeName) {
        if (unitTypeName == null) {
            throw new IllegalArgumentException("unitTypeName cannot be null");
        }
        try (var connection = new AutoCommitResettingConnectionWrapper(dataSource.getConnection(), false);
             var statement = new DynamicStatementWrapper(connection, GET_UNITTYPE_BY_NANE, unitTypeName);
             var resultSet = statement.getPreparedStatement().executeQuery()) {
            if (resultSet.next()) {
                var unittype = parseUnittype(resultSet);
                unittype.setId(resultSet.getInt("unit_type_id"));
                return unittype;
            } else {
                log.warn("No unittype found with id {}", unitTypeName);
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<UnittypeParameter> getUnittypeParametersByUnitTypeId(int unitTypeId) {
        if (unitTypeId == 0) {
            throw new IllegalArgumentException("unitTypeId cannot be null");
        }
        var unittype = getUnitTypeById(unitTypeId);
        try (var connection = new AutoCommitResettingConnectionWrapper(dataSource.getConnection(), false);
             var statement = new DynamicStatementWrapper(connection, GET_UNITTYPE_PARAMETERS_BY_UNITTYPE_ID, unitTypeId);
             var resultSet = statement.getPreparedStatement().executeQuery()) {
            var unittypeParameters = new ArrayList<UnittypeParameter>();
            while (resultSet.next()) {
                var unitTypeParam = parseUnittypeParameter(resultSet, unittype);
                unittypeParameters.add(unitTypeParam);
            }
            return unittypeParameters;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static UnittypeParameter parseUnittypeParameter(ResultSet resultSet, Unittype unittype) throws SQLException {
        var name = resultSet.getString("name");
        var param = new UnittypeParameterFlag(resultSet.getString("flags"), true);
        var unitTypeParam = new UnittypeParameter(unittype, name, param);
        unitTypeParam.setId(resultSet.getInt("unit_type_param_id"));
        return unitTypeParam;
    }

    public Group getGroupById(int groupId) {
        if (groupId == 0) {
            throw new IllegalArgumentException("groupId cannot be null");
        }
        try(var connection = new AutoCommitResettingConnectionWrapper(dataSource.getConnection(), false);
            var statement = new DynamicStatementWrapper(connection, GET_GROUP_BY_ID, groupId);
            var resultSet = statement.getPreparedStatement().executeQuery()) {
            if (resultSet.next()) {
                return parseGroup(resultSet, new HashMap<>());
            } else {
                log.warn("No group found with id {}", groupId);
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Group parseGroup(ResultSet resultSet, HashMap<Integer, Group> groupCache) throws SQLException {
        var group = new Group(resultSet.getInt("group_id"));
        group.setName(resultSet.getString("group_name"));
        group.setDescription(resultSet.getString("group_description"));
        group.setUnittype(parseUnittype(resultSet));
        var profileId = resultSet.getInt("profile_id");
        if (profileId != 0) {
            var profile = getProfileById(profileId);
            group.setProfile(profile);
        }
        group.setCount(resultSet.getInt("count"));
        groupCache.put(group.getId(), group);
        if (resultSet.next()) {
            int parentGroupId = resultSet.getInt("parent_group_id");
            var parentGroup = groupCache.containsKey(parentGroupId)
                    ? groupCache.get(parentGroupId)
                    : groupCache.put(parentGroupId, parseGroup(resultSet, groupCache));
            group.setParent(parentGroup);
        }
        return group;
    }

    public Profile getProfileById(int profileId) {
        if (profileId == 0) {
            throw new IllegalArgumentException("profileId cannot be null");
        }
        try(var connection = new AutoCommitResettingConnectionWrapper(dataSource.getConnection(), false);
            var statement = new DynamicStatementWrapper(connection, GET_PROFILE_BY_ID, profileId);
            var resultSet = statement.getPreparedStatement().executeQuery()) {
            if (resultSet.next()) {
                return parseProfile(profileId, resultSet);
            } else {
                log.warn("No profile found with id {}", profileId);
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static Profile parseProfile(int profileId, ResultSet resultSet) throws SQLException {
        var unitType = parseUnittype(resultSet);
        var profile = new Profile(resultSet.getString("profile_name"), unitType);
        profile.setId(profileId);
        return profile;
    }

    public Job getJobById(int jobId) {
        if (jobId == 0) {
            throw new IllegalArgumentException("jobId cannot be null");
        }
        try(var connection = new AutoCommitResettingConnectionWrapper(dataSource.getConnection(), false);
            var statement = new DynamicStatementWrapper(connection, GET_JOB_BY_ID, jobId);
            var resultSet = statement.getPreparedStatement().executeQuery()) {
            if (resultSet.next()) {
                var job = new Job();
                job.setName(resultSet.getString("job_name"));
                job.setId(resultSet.getInt("job_id"));
                job.setDescription(resultSet.getString("description"));
                var groupId = resultSet.getInt("group_id");
                if (groupId != 0) {
                    var group = getGroupById(groupId);
                    job.setGroup(group);
                    job.setUnittype(group.getUnittype());
                }
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
                job.setRepeatCount(resultSet.getInt("repeat_count"));
                job.setRepeatInterval(resultSet.getInt("repeat_interval"));
                return job;
            } else {
                log.warn("No job found with id {}", jobId);
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<GroupParameter> getGroupParametersByGroupId(int groupId) {
        if (groupId == 0) {
            throw new IllegalArgumentException("groupId cannot be null");
        }
        var group = getGroupById(groupId);
        try(var connection = new AutoCommitResettingConnectionWrapper(dataSource.getConnection(), false);
            var statement = new DynamicStatementWrapper(connection, GET_GROUP_PARAMETERS_BY_GROUP_ID, groupId);
            var resultSet = statement.getPreparedStatement().executeQuery()) {
            var groupParameters = new ArrayList<GroupParameter>();
            var unitTypeParameters = getUnittypeParametersByUnitTypeId(group.getUnittype().getId());
            UnittypeParameter unit_type_param = null;
            while (resultSet.next()) {
                if (unit_type_param == null) {
                    var unit_type_param_id = resultSet.getInt("unit_type_param_id");
                    unit_type_param = unitTypeParameters
                            .stream()
                            .filter(utp -> utp.getUnittype().getId().equals(unit_type_param_id))
                            .findFirst()
                            .orElseThrow(() -> new IllegalArgumentException("Unittype parameter with id %s not found".formatted(unit_type_param_id)));
                }
                String value = resultSet.getString("value");
                Parameter.Operator op = Parameter.Operator.getOperator(resultSet.getString("operator"));
                Parameter.ParameterDataType pdt = Parameter.ParameterDataType.getDataType(resultSet.getString("data_type"));
                Parameter parameter = new Parameter(unit_type_param, value, op, pdt);
                GroupParameter groupParameter = new GroupParameter(parameter, group);
                groupParameter.setId(resultSet.getInt("id"));
                groupParameters.add(groupParameter);
            }
            return groupParameters;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<ProfileParameter> getProfileParametersByProfileId(int profileId) {
        if (profileId == 0) {
            throw new IllegalArgumentException("profileId cannot be null");
        }
        var profile = getProfileById(profileId);
        try(var connection = new AutoCommitResettingConnectionWrapper(dataSource.getConnection(), false);
            var statement = new DynamicStatementWrapper(connection, GET_PROFILE_PARAMETERS_BY_PROFILE_ID, profileId);
            var resultSet = statement.getPreparedStatement().executeQuery()) {
            var profileParameters = new ArrayList<ProfileParameter>();
            var unitTypeParameters = getUnittypeParametersByUnitTypeId(profile.getUnittype().getId());
            UnittypeParameter unittypeParameter = null;
            while (resultSet.next()) {
                if (unittypeParameter == null) {
                    var unit_type_param_id = resultSet.getInt("unit_type_param_id");
                    unittypeParameter = unitTypeParameters
                            .stream()
                            .filter(utp -> utp.getUnittype().getId().equals(unit_type_param_id))
                            .findFirst()
                            .orElseThrow(() -> new IllegalArgumentException("Unittype parameter with id %s not found".formatted(unit_type_param_id)));
                }
                var value = resultSet.getString("value");
                var profileParameter = new ProfileParameter(profile, unittypeParameter, value);
                profileParameters.add(profileParameter);
            }
            return profileParameters;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<JobParameter> getJobParametersByJobId(int jobId) {
        if (jobId == 0) {
            throw new IllegalArgumentException("jobId cannot be null");
        }
        var job = getJobById(jobId);
        try(var connection = new AutoCommitResettingConnectionWrapper(dataSource.getConnection(), false);
            var statement = new DynamicStatementWrapper(connection, GET_JOB_PARAMETERS_BY_JOB_ID, jobId);
            var resultSet = statement.getPreparedStatement().executeQuery()) {
            var jobParameters = new ArrayList<JobParameter>();
            var unitTypeParameters = getUnittypeParametersByUnitTypeId(job.getUnittype().getId());
            UnittypeParameter unittypeParameter = null;
            while (resultSet.next()) {
                if (unittypeParameter == null) {
                    var unit_type_param_id = resultSet.getInt("unit_type_param_id");
                    unittypeParameter = unitTypeParameters
                            .stream()
                            .filter(utp -> utp.getUnittype().getId().equals(unit_type_param_id))
                            .findFirst()
                            .orElseThrow(() -> new IllegalArgumentException("Unittype parameter with id %s not found".formatted(unit_type_param_id)));
                }
                var param = new Parameter(unittypeParameter, resultSet.getString("value"));
                var jobParameter = new JobParameter(job, Job.ANY_UNIT_IN_GROUP, param);
                jobParameters.add(jobParameter);
            }
            return jobParameters;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
