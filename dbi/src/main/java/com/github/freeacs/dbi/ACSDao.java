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

    private static final String GET_UNIT_PARAMETERS_BY_UNIT_ID = "SELECT unit_id, unit_type_param_id, value FROM unit_param WHERE unit_id = ?";

    private final DataSource dataSource;
    private final ACSCacheManager acsCacheManager;

    public ACSDao(DataSource dataSource, ACSCacheManager acsCacheManager) {
        this.dataSource = dataSource;
        this.acsCacheManager = acsCacheManager;
    }


    // TODO add methods to get unittype, profile, job, and unit parameters, similar to the methods in ACS.java, but without the map memoization
    // instead use the ACSCacheManager to memoize the results

    public Unittype getUnitTypeById(Integer unitTypeId) {
        if (unitTypeId == null) {
            throw new IllegalArgumentException("unitTypeId cannot be null");
        }
        Unittype cache = acsCacheManager.get("unittype-%s".formatted(unitTypeId), Unittype.class);
        if (cache != null) {
            return cache;
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
                    acsCacheManager.put("unittype-%s".formatted(unitTypeId), unittype);
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

    public List<UnittypeParameter> getUnitTypeParametersByUnitTypeId(Integer unitTypeId) {
        if (unitTypeId == null) {
            throw new IllegalArgumentException("unitTypeId cannot be null");
        }
        List<UnittypeParameter> cache = acsCacheManager.getList("unittype-params-%s".formatted(unitTypeId));
        if (cache != null) {
            return cache;
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
                acsCacheManager.put("unittype-params-%s".formatted(unitTypeId), unittypeParameters);
                return unittypeParameters;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
