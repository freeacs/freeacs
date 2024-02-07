package com.github.freeacs.dbi;

import com.github.freeacs.common.cache.ACSCacheManager;
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
            description,
            protocol
        FROM unit_type
        WHERE unit_type_id = ?
    """;

    private static final String GET_UNITTYPE_BY_NANE = """
        SELECT
            unit_type_id,
            unit_type_name,
            vendor_name,
            description,
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
            group_id,
            unit_type_id,
            group_name,
            description,
            parent_group_id,
            profile_id,
            count
          FROM group_
          WHERE group_id = ?
          UNION ALL
          SELECT
            i.group_id,
            i.unit_type_id,
            i.group_name,
            i.description,
            i.parent_group_id,
            i.profile_id,
            i.count
          FROM group_ i
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
            profile_id,
            unit_type_id,
            profile_name
        FROM profile
        WHERE profile_id = ?
    """;

    private final DataSource dataSource;
    private final ACSCacheManager acsCacheManager;

    public ACSDao(DataSource dataSource, ACSCacheManager acsCacheManager) {
        this.dataSource = dataSource;
        this.acsCacheManager = acsCacheManager;
    }

    public Unittype getCachedUnittypeByUnitTypeId(int unitTypeId) {
        if (unitTypeId == 0) {
            throw new IllegalArgumentException("unitTypeId cannot be null");
        }
        Unittype cache = acsCacheManager.get("unittype-byId-%d".formatted(unitTypeId), Unittype.class);
        if (cache != null) {
            return cache;
        }
        Unittype unittype = getUnitTypeById(unitTypeId);
        acsCacheManager.put("unittype-byId-%d".formatted(unitTypeId), unittype);
        return unittype;
    }

    public Unittype getCachedUnittypeByUnitTypeName(String unitTypeName) {
        Unittype cache = acsCacheManager.get("unittype-byName-%s".formatted(unitTypeName), Unittype.class);
        if (cache != null) {
            return cache;
        }
        Unittype unittype = getUnitTypeByName(unitTypeName);
        acsCacheManager.put("unittype-byName-%s".formatted(unitTypeName), unittype);
        return unittype;
    }


    public List<UnittypeParameter> getCachedUnittypeParameters(Integer unitTypeId) {
        List<UnittypeParameter> cache = acsCacheManager.getList("unittype-byId-%d-params".formatted(unitTypeId), UnittypeParameter.class);
        if (cache != null) {
            return cache;
        }
        List<UnittypeParameter> unittypeParameters = getUnittypeParametersByUnitTypeId(unitTypeId);
        acsCacheManager.put("unittype-byId-%d-params".formatted(unitTypeId), unittypeParameters);
        return unittypeParameters;
    }

    public Profile getCachedProfile(Integer profileId) {
        Profile cache = acsCacheManager.get("profile-byId-%d".formatted(profileId), Profile.class);
        if (cache != null) {
            return cache;
        }
        Profile profile = getProfile(profileId);
        acsCacheManager.put("profile-byId-%d".formatted(profileId), profile);
        return profile;
    }

    public List<ProfileParameter> getCachedProfileParameters(Integer profileId) {
        List<ProfileParameter> cache = acsCacheManager.getList("profile-byId-%d-params".formatted(profileId), ProfileParameter.class);
        if (cache != null) {
            return cache;
        }
        List<ProfileParameter> profile = getProfileParametersByProfileId(profileId);
        acsCacheManager.put("profile-byId-%d-params".formatted(profileId), profile);
        return profile;
    }

    public Group getCachedGroup(Integer groupId) {
        Group cache = acsCacheManager.get("group-byId-%s".formatted(groupId), Group.class);
        if (cache != null) {
            return cache;
        }
        Group group = getGroup(groupId);
        acsCacheManager.put("group-byId-%s".formatted(groupId), group);
        return group;
    }

    public List<GroupParameter> getCachedGroupParameters(Integer groupId) {
        List<GroupParameter> cache = acsCacheManager.getList("group-byId-%d-params".formatted(groupId), GroupParameter.class);
        if (cache != null) {
            return cache;
        }
        List<GroupParameter> groupParameters = getGroupParametersByGroupId(groupId);
        acsCacheManager.put("group-byId-%d-params".formatted(groupId), groupParameters);
        return groupParameters;
    }

    public Job getCachedJob(Integer jobId) {
        Job cache = acsCacheManager.get("job-byId-%s".formatted(jobId), Job.class);
        if (cache != null) {
            return cache;
        }
        Job group = getJob(jobId);
        acsCacheManager.put("job-byId-%s".formatted(jobId), group);
        return group;
    }

    public List<JobParameter> getCachedJobParameters(Integer jobId) {
        List<JobParameter> cache = acsCacheManager.getList("job-byId-%d-params".formatted(jobId), JobParameter.class);
        if (cache != null) {
            return cache;
        }
        List<JobParameter> jobParameters = getJobParametersByJobId(jobId);
        acsCacheManager.put("job-byId-%d-params".formatted(jobId), jobParameters);
        return jobParameters;
    }

    public UnittypeParameter getCachedUnittypeParameterById(Integer unitTypeId, Integer unitTypeParamId) {
        UnittypeParameter cache = acsCacheManager.get("unit-type-byId-%d-param-byId-%d".formatted(unitTypeId, unitTypeParamId), UnittypeParameter.class);
        if (cache != null) {
            return cache;
        }
        UnittypeParameter unittypeParameter = getCachedUnittypeParameters(unitTypeId)
            .stream()
            .filter(p -> p.getId().equals(unitTypeParamId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("No UnittypeParameter found with id " + unitTypeParamId));
        acsCacheManager.put("unit-type-byId-%d-param-byId-%d".formatted(unitTypeId, unitTypeParamId), unittypeParameter);
        return unittypeParameter;
    }

    private Unittype getUnitTypeById(int unitTypeId) {
        if (unitTypeId == 0) {
            throw new IllegalArgumentException("unitTypeId cannot be null");
        }
        try (var connection = new AutoCommitResettingConnectionWrapper(dataSource.getConnection(), false);
             var statement = new DynamicStatementWrapper(connection, GET_UNITTYPE_BY_ID, unitTypeId);
             var resultSet = statement.getPreparedStatement().executeQuery()) {
            if (resultSet.next()) {
                var unittype = new Unittype(
                        resultSet.getString("unit_type_name"),
                        resultSet.getString("vendor_name"),
                        resultSet.getString("description"),
                        Unittype.ProvisioningProtocol.valueOf(resultSet.getString("protocol"))
                );
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

    private Unittype getUnitTypeByName(String unitTypeName) {
        if (unitTypeName == null) {
            throw new IllegalArgumentException("unitTypeName cannot be null");
        }
        try (var connection = new AutoCommitResettingConnectionWrapper(dataSource.getConnection(), false);
             var statement = new DynamicStatementWrapper(connection, GET_UNITTYPE_BY_NANE, unitTypeName);
             var resultSet = statement.getPreparedStatement().executeQuery()) {
            if (resultSet.next()) {
                var unittype = new Unittype(
                        resultSet.getString("unit_type_name"),
                        resultSet.getString("vendor_name"),
                        resultSet.getString("description"),
                        Unittype.ProvisioningProtocol.valueOf(resultSet.getString("protocol"))
                );
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

    private List<UnittypeParameter> getUnittypeParametersByUnitTypeId(int unitTypeId) {
        if (unitTypeId == 0) {
            throw new IllegalArgumentException("unitTypeId cannot be null");
        }
        var unittype = getUnitTypeById(unitTypeId);
        try (var connection = new AutoCommitResettingConnectionWrapper(dataSource.getConnection(), false);
             var statement = new DynamicStatementWrapper(connection, GET_UNITTYPE_PARAMETERS_BY_UNITTYPE_ID, unitTypeId);
             var resultSet = statement.getPreparedStatement().executeQuery()) {
            var unittypeParameters = new ArrayList<UnittypeParameter>();
            while (resultSet.next()) {
                var name = resultSet.getString("name");
                var param = new UnittypeParameterFlag(resultSet.getString("flags"), true);
                var unitTypeParam = new UnittypeParameter(unittype, name, param);
                unitTypeParam.setId(resultSet.getInt("unit_type_param_id"));
                unittypeParameters.add(unitTypeParam);
            }
            return unittypeParameters;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Group getGroup(int groupId) {
        if (groupId == 0) {
            throw new IllegalArgumentException("groupId cannot be null");
        }
        try(var connection = new AutoCommitResettingConnectionWrapper(dataSource.getConnection(), false);
            var statement = new DynamicStatementWrapper(connection, GET_GROUP_BY_ID, groupId);
            var resultSet = statement.getPreparedStatement().executeQuery()) {
            var groupCache = new HashMap<Integer, Group>();
            if (resultSet.next()) {
                return parseGroup(resultSet, groupCache);
            } else {
                log.warn("No group found with id {}", groupId);
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Group parseGroup(ResultSet resultSet, HashMap<Integer, Group> groupCache) throws SQLException {
        var unitTypeId = resultSet.getInt("unit_type_id");
        var unitType = getCachedUnittypeByUnitTypeId(unitTypeId);
        var group = new Group(resultSet.getInt("group_id"));
        group.setName(resultSet.getString("group_name"));
        group.setDescription(resultSet.getString("description"));
        group.setUnittype(unitType);
        var parentGroupId = resultSet.getInt("parent_group_id");
        if (parentGroupId != 0) {
            var parentGroup = getCachedGroup(parentGroupId);
            group.setParent(parentGroup);
        }
        var profileId = resultSet.getInt("profile_id");
        if (profileId != 0) {
            var profile = getCachedProfile(profileId);
            group.setProfile(profile);
        }
        group.setCount(resultSet.getInt("count"));
        groupCache.put(group.getId(), group);
        if (resultSet.next()) {
            var parentGroup = parseGroup(resultSet, groupCache);
            group.setParent(parentGroup);
        }
        return group;
    }

    private Profile getProfile(int profileId) {
        if (profileId == 0) {
            throw new IllegalArgumentException("profileId cannot be null");
        }
        try(var connection = new AutoCommitResettingConnectionWrapper(dataSource.getConnection(), false);
            var statement = new DynamicStatementWrapper(connection, GET_PROFILE_BY_ID, profileId);
            var resultSet = statement.getPreparedStatement().executeQuery()) {
            if (resultSet.next()) {
                var unitTypeId = resultSet.getInt("unit_type_id");
                var unitType = getCachedUnittypeByUnitTypeId(unitTypeId);
                var profile = new Profile(resultSet.getString("profile_name"), unitType);
                profile.setId(profileId);
                return profile;
            } else {
                log.warn("No profile found with id {}", profileId);
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Job getJob(int jobId) {
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
                    var group = getCachedGroup(groupId);
                    job.setGroup(group);
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
                    var jobDependency = getCachedJob(jobIdDependency);
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

    private List<GroupParameter> getGroupParametersByGroupId(int groupId) {
        if (groupId == 0) {
            throw new IllegalArgumentException("groupId cannot be null");
        }
        var group = getCachedGroup(groupId);
        try(var connection = new AutoCommitResettingConnectionWrapper(dataSource.getConnection(), false);
            var statement = new DynamicStatementWrapper(connection, GET_GROUP_PARAMETERS_BY_GROUP_ID, groupId);
            var resultSet = statement.getPreparedStatement().executeQuery()) {
            var groupParameters = new ArrayList<GroupParameter>();
            while (resultSet.next()) {
                var unit_type_id = resultSet.getInt("unit_type_id");
                var unit_type_param_id = resultSet.getInt("unit_type_param_id");
                UnittypeParameter unit_type_param = getCachedUnittypeParameterById(unit_type_id, unit_type_param_id);
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

    private List<ProfileParameter> getProfileParametersByProfileId(int profileId) {
        if (profileId == 0) {
            throw new IllegalArgumentException("profileId cannot be null");
        }
        var profile = getCachedProfile(profileId);
        try(var connection = new AutoCommitResettingConnectionWrapper(dataSource.getConnection(), false);
            var statement = new DynamicStatementWrapper(connection, GET_PROFILE_PARAMETERS_BY_PROFILE_ID, profileId);
            var resultSet = statement.getPreparedStatement().executeQuery()) {
            var profileParameters = new ArrayList<ProfileParameter>();
            while (resultSet.next()) {
                var unit_type_id = resultSet.getInt("unit_type_id");
                var unit_type_param_id = resultSet.getInt("unit_type_param_id");
                UnittypeParameter unit_type_param = getCachedUnittypeParameterById(unit_type_id, unit_type_param_id);
                var value = resultSet.getString("value");
                var profileParameter = new ProfileParameter(profile, unit_type_param, value);
                profileParameters.add(profileParameter);
            }
            return profileParameters;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private List<JobParameter> getJobParametersByJobId(int jobId) {
        if (jobId == 0) {
            throw new IllegalArgumentException("jobId cannot be null");
        }
        try(var connection = new AutoCommitResettingConnectionWrapper(dataSource.getConnection(), false);
            var statement = new DynamicStatementWrapper(connection, GET_JOB_PARAMETERS_BY_JOB_ID, jobId);
            var resultSet = statement.getPreparedStatement().executeQuery()) {
            var jobParameters = new ArrayList<JobParameter>();
            while (resultSet.next()) {
                var unit_type_id = resultSet.getInt("unit_type_id");
                var unit_type_param_id = resultSet.getInt("unit_type_param_id");
                UnittypeParameter unit_type_param = getCachedUnittypeParameterById(unit_type_id, unit_type_param_id);
                var job = getCachedJob(resultSet.getInt("job_id"));
                var param = new Parameter(unit_type_param,resultSet.getString("value"));
                var jobParameter = new JobParameter(job, Job.ANY_UNIT_IN_GROUP, param);
                jobParameters.add(jobParameter);
            }
            return jobParameters;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
