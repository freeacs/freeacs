package com.github.freeacs.dbi;

import com.github.freeacs.common.cache.ACSCacheManager;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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

    private final DataSource dataSource;
    private final ACSCacheManager acsCacheManager;

    public ACSDao(DataSource dataSource, ACSCacheManager acsCacheManager) {
        this.dataSource = dataSource;
        this.acsCacheManager = acsCacheManager;
    }

    public Unittype getCachedUnittypeByUnitTypeId(Integer unitTypeId) {
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

    private Unittype getUnitTypeById(Integer unitTypeId) {
        if (unitTypeId == null) {
            throw new IllegalArgumentException("unitTypeId cannot be null");
        }
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(GET_UNITTYPE_BY_ID)) {
            statement.setInt(1, unitTypeId);
            try (ResultSet resultSet = statement.executeQuery()) {
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
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Unittype getUnitTypeByName(String unitTypeName) {
        if (unitTypeName == null) {
            throw new IllegalArgumentException("unitTypeName cannot be null");
        }
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(GET_UNITTYPE_BY_NANE)) {
            statement.setString(1, unitTypeName);
            try (ResultSet resultSet = statement.executeQuery()) {
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
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private List<UnittypeParameter> getUnittypeParametersByUnitTypeId(Integer unitTypeId) {
        if (unitTypeId == null) {
            throw new IllegalArgumentException("unitTypeId cannot be null");
        }
        var unittype = getUnitTypeById(unitTypeId);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(GET_UNITTYPE_PARAMETERS_BY_UNITTYPE_ID)) {
            statement.setInt(1, unitTypeId);
            try (ResultSet resultSet = statement.executeQuery()) {
                var unittypeParameters = new ArrayList<UnittypeParameter>();
                while (resultSet.next()) {
                    var name = resultSet.getString("name");
                    var param = new UnittypeParameterFlag(resultSet.getString("flags"), true);
                    var unitTypeParam = new UnittypeParameter(unittype, name, param);
                    unitTypeParam.setId(resultSet.getInt("unit_type_param_id"));
                    unittypeParameters.add(unitTypeParam);
                }
                return unittypeParameters;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Group getGroup(Integer groupId) {
        // TODO: Implement this method
        return null;
    }

    private Profile getProfile(Integer profileId) {
        // TODO: Implement this method
        return null;
    }

    private Job getJob(Integer jobId) {
        // TODO: Implement this method
        return null;
    }

    private List<GroupParameter> getGroupParametersByGroupId(Integer groupId) {
        if (groupId == null) {
            throw new IllegalArgumentException("groupId cannot be null");
        }
        var group = getCachedGroup(groupId);
        try(Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(GET_GROUP_PARAMETERS_BY_GROUP_ID)) {
            statement.setInt(1, groupId);
            try(ResultSet resultSet = statement.executeQuery()) {
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
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private List<ProfileParameter> getProfileParametersByProfileId(Integer profileId) {
        if (profileId == null) {
            throw new IllegalArgumentException("profileId cannot be null");
        }
        var profile = getCachedProfile(profileId);
        try(Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(GET_PROFILE_PARAMETERS_BY_PROFILE_ID)) {
            statement.setInt(1, profileId);
            try(ResultSet resultSet = statement.executeQuery()) {
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
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private List<JobParameter> getJobParametersByJobId(Integer jobId) {
        try(Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(GET_JOB_PARAMETERS_BY_JOB_ID)) {
            statement.setInt(1, jobId);
            try(ResultSet resultSet = statement.executeQuery()) {
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
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
