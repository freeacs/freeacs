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

    private static final String GET_UNITTYPE_BY_ID = "SELECT unit_type_id, matcher_id, unit_type_name, vendor_name, description, protocol FROM unit_type WHERE unit_type_id = ?";

    private static final String GET_UNITTYPE_PARAMETERS_BY_UNITTYPE_ID = "SELECT unit_type_param_id, unit_type_id, name, value, flags FROM unit_type_param WHERE unit_type_id = ?";

    private static final String GET_PROFILE_PARAMETERS_BY_PROFILE_ID = "SELECT profile_id, unit_type_param_id, value FROM profile_param WHERE profile_id = ?";

    private static final String GET_JOB_PARAMETERS_BY_JOB_ID = "SELECT job_id, unit_type_param_id, value FROM job_param WHERE job_id = ?";

    private static final String GET_JOB_PARAMETERS_BY_UNIT_ID = "SELECT job_id, unit_type_param_id, value FROM job_param WHERE job_id = ?";

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
        Unittype profile = getUnitTypeById(unitTypeId);
        acsCacheManager.put("unittype-%s".formatted(unitTypeId), profile);
        return profile;
    }

    public List<UnittypeParameter> getCachedUnittypeParameters(Integer unitTypeId) {
        List<UnittypeParameter> cache = acsCacheManager.getList("unittype-params-%s".formatted(unitTypeId));
        if (cache != null) {
            return cache;
        }
        List<UnittypeParameter> profile = getUnittypeParametersByUnitTypeId(unitTypeId);
        acsCacheManager.put("unittype-params-%s".formatted(unitTypeId), profile);
        return profile;
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
        List<GroupParameter> profile = getGroupParametersByGroupId(groupId);
        acsCacheManager.put("group-params-%s".formatted(groupId), profile);
        return profile;
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
        List<JobParameter> profile = getJobParametersByJobId(jobId);
        acsCacheManager.put("job-params-%s".formatted(jobId), profile);
        return profile;
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
        // TODO: Implement this method
        return null;
    }

    private List<ProfileParameter> getProfileParametersByProfileId(Integer profileId) {
        // TODO: Implement this method
        return null;
    }

    private List<JobParameter> getJobParametersByJobId(Integer jobId) {
        // TODO: Implement this method
        return null;
    }
}
