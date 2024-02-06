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

    private static final String GET_UNITTYPE_PARAMETERS_BY_UNITTYPE_ID = """
        SELECT 
            unit_type_param_id, 
            unit_type_id, 
            name, 
            value, 
            flags
        FROM unit_type_param
        WHERE unit_type_id = ?
    """;

    private static final String GET_PROFILE_PARAMETERS_BY_PROFILE_ID = """
        SELECT
            pp.profile_param_id, 
            ut.unit_type_id, 
            pp.unit_type_param_id, 
            pp.value
        FROM profile_parameters AS pp
        JOIN unittype AS ut ON pp.unittype_id = ut.id
        JOIN unittype_parameters AS utp ON ut.id = utp.unittype_id
        WHERE pp.profile_id = ?
    """;

    private static final String GET_JOB_PARAMETERS_BY_JOB_ID = """
        SELECT 
            jp.job_id,
            jp.job_param_id,
            ut.unit_type_id, 
            jp.unit_type_param_id, 
            jp.value
        FROM job_parameters AS jp
        JOIN unittype AS ut ON jp.unittype_id = ut.id
        JOIN unittype_parameters AS utp ON ut.id = utp.unittype_id 
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
        FROM group_parameters AS gp
        JOIN unittype AS ut ON gp.unittype_id = ut.id
        JOIN unittype_parameters AS utp ON ut.id = utp.unittype_id
        WHERE gp.group_id = ?
    """;

    private final DataSource dataSource;
    private final ACSCacheManager acsCacheManager;

    public ACSDao(DataSource dataSource, ACSCacheManager acsCacheManager) {
        this.dataSource = dataSource;
        this.acsCacheManager = acsCacheManager;
    }

    public Unittype getCachedUnittype(Integer unitTypeId) {
        Unittype cache = acsCacheManager.get("unittype-%s".formatted(unitTypeId), Unittype.class);
        if (cache != null) {
            return cache;
        }
        Unittype unittype = getUnitTypeById(unitTypeId);
        acsCacheManager.put("unittype-%s".formatted(unitTypeId), unittype);
        return unittype;
    }

    public List<UnittypeParameter> getCachedUnittypeParameters(Integer unitTypeId) {
        List<UnittypeParameter> cache = acsCacheManager.getList("unittype-params-%s".formatted(unitTypeId));
        if (cache != null) {
            return cache;
        }
        List<UnittypeParameter> unittypeParameters = getUnittypeParametersByUnitTypeId(unitTypeId);
        acsCacheManager.put("unittype-params-%s".formatted(unitTypeId), unittypeParameters);
        return unittypeParameters;
    }

    public Profile getCachedProfile(Integer profileId) {
        Profile cache = acsCacheManager.get("profile-%s".formatted(profileId), Profile.class);
        if (cache != null) {
            return cache;
        }
        Profile profile = getProfile(profileId);
        acsCacheManager.put("profile-%s".formatted(profileId), profile);
        return profile;
    }

    public List<ProfileParameter> getCachedProfileParameters(Integer profileId) {
        List<ProfileParameter> cache = acsCacheManager.getList("profile-params-%s".formatted(profileId));
        if (cache != null) {
            return cache;
        }
        List<ProfileParameter> profile = getProfileParametersByProfileId(profileId);
        acsCacheManager.put("profile-params-%s".formatted(profileId), profile);
        return profile;
    }

    public Group getCachedGroup(Integer groupId) {
        Group cache = acsCacheManager.get("group-%s".formatted(groupId), Group.class);
        if (cache != null) {
            return cache;
        }
        Group group = getGroup(groupId);
        acsCacheManager.put("group-%s".formatted(groupId), group);
        return group;
    }

    public List<GroupParameter> getCachedGroupParameters(Integer groupId) {
        List<GroupParameter> cache = acsCacheManager.getList("group-params-%s".formatted(groupId));
        if (cache != null) {
            return cache;
        }
        List<GroupParameter> groupParameters = getGroupParametersByGroupId(groupId);
        acsCacheManager.put("group-params-%s".formatted(groupId), groupParameters);
        return groupParameters;
    }

    public Job getCachedJob(Integer jobId) {
        Job cache = acsCacheManager.get("job-%s".formatted(jobId), Job.class);
        if (cache != null) {
            return cache;
        }
        Job group = getJob(jobId);
        acsCacheManager.put("job-%s".formatted(jobId), group);
        return group;
    }

    public List<JobParameter> getCachedJobParameters(Integer jobId) {
        List<JobParameter> cache = acsCacheManager.getList("job-params-%s".formatted(jobId));
        if (cache != null) {
            return cache;
        }
        List<JobParameter> jobParameters = getJobParametersByJobId(jobId);
        acsCacheManager.put("job-params-%s".formatted(jobId), jobParameters);
        return jobParameters;
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
                    var unitTypeParam = new UnittypeParameter(
                            unittype,
                            resultSet.getString("name"),
                            new UnittypeParameterFlag(resultSet.getString("flags"), true)
                    );
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
                    Integer unit_type_param_id = resultSet.getInt("gp.unit_type_param_id");
                    UnittypeParameter utp = 
                        getCachedUnittypeParameters(resultSet.getInt("unit_type_id"))
                            .stream()
                            .filter(p -> p.getId() == unit_type_param_id)
                            .findFirst()
                            .orElseThrow(() -> new IllegalArgumentException("No UnittypeParameter found with id " + unit_type_param_id));
                    String value = resultSet.getString("gp.value");
                    Parameter.Operator op = Parameter.Operator.getOperator(resultSet.getString("operator"));
                    Parameter.ParameterDataType pdt = Parameter.ParameterDataType.getDataType(resultSet.getString("data_type"));
                    Parameter parameter = new Parameter(utp, value, op, pdt);
                    GroupParameter groupParameter = new GroupParameter(parameter, group);
                    groupParameter.setId(resultSet.getInt("gp.id"));
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
                    var unit_type_param_id = resultSet.getInt("unit_type_param_id");
                    UnittypeParameter unit_type_param = 
                        getCachedUnittypeParameters(resultSet.getInt("unit_type_id"))
                            .stream()
                            .filter(p -> p.getId() == unit_type_param_id)
                            .findFirst()
                            .orElseThrow();
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
                    var unit_type_param_id = resultSet.getInt("unit_type_param_id");
                    UnittypeParameter unit_type_param = 
                        getCachedUnittypeParameters(resultSet.getInt("unit_type_id"))
                            .stream()
                            .filter(p -> p.getId() == unit_type_param_id)
                            .findFirst()
                            .orElseThrow();
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
