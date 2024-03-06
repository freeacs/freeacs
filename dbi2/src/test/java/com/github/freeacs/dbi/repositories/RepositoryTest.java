package com.github.freeacs.dbi.repositories;

import com.github.freeacs.common.util.AbstractMySqlIntegrationTest;
import com.github.freeacs.dbi.domain.Profile;
import com.github.freeacs.dbi.domain.Unit;
import com.github.freeacs.dbi.domain.UnitType;
import com.github.freeacs.dbi.domain.UnitTypeProvisioningProtocol;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RepositoryTest implements AbstractMySqlIntegrationTest  {

    private static Jdbi jdbi;

    @BeforeAll
    public static void localBeforeAll() {
        jdbi = Jdbi.create(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword());
        jdbi.installPlugin(new SqlObjectPlugin());
    }

    @Test
    public void canListUnits() {
        var unitRepository = jdbi.onDemand(UnitRepository.class);
        var result = unitRepository.listUnits();
        assertNotNull(result);
        assertTrue(result.isEmpty());
        var unitType = createUnitType();
        var profile = createProfile(unitType);
        var insertedUnits = createUnit(profile);
        assertEquals(1, insertedUnits);
    }

    private static int createUnit(Profile profile) {
        var unit = new Unit("1234", profile);
        return jdbi.onDemand(UnitRepository.class).insertUnit(unit);
    }

    private static Profile createProfile(UnitType unitType) {
        var profile = new Profile(null, "test", unitType);
        var profileId = jdbi.onDemand(ProfileRepository.class).insertProfile(profile);
        profile = profile.withId(profileId);
        return profile;
    }

    private static UnitType createUnitType() {
        var unitType = new UnitType(null, "test", "vendor", "description", UnitTypeProvisioningProtocol.TR069);
        var unitTypeId = jdbi.onDemand(UnitTypeRepository.class).insertUnitType(unitType);
        unitType = unitType.withId(unitTypeId);
        return unitType;
    }

    @Test
    public void canListUsers() {
        var result = jdbi.onDemand(UserRepository.class).listUsers();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("admin", result.get(0).getUsername());
        assertTrue(result.get(0).getAdmin());
    }

    @Test
    public void canListUnitTypes() {
        var result = jdbi.onDemand(UnitTypeRepository.class).listUnitTypes();
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    public void canListProfiles() {
        var result = jdbi.onDemand(ProfileRepository.class).listProfiles();
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

}
