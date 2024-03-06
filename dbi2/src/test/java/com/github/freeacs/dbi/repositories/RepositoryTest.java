package com.github.freeacs.dbi.repositories;

import com.github.freeacs.common.util.AbstractMySqlIntegrationTest;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RepositoryTest implements AbstractMySqlIntegrationTest  {

    private static Jdbi jdbi;
    private static TestDataFactory testDataFactory;

    @BeforeAll
    public static void localBeforeAll() {
        jdbi = Jdbi.create(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword());
        jdbi.installPlugin(new SqlObjectPlugin());
        testDataFactory = new TestDataFactory(jdbi);
    }

    @Test
    public void canCreateListAndDeleteUnits() {
        // List units
        var result = jdbi.onDemand(UnitRepository.class).listUnits();
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // create and update a unit type
        var unitType = testDataFactory.createUnitType();
        assertEquals(1, jdbi.onDemand(UnitTypeRepository.class)
                .updateUnitType(unitType.withName("New unitType name")));
        // create and update a profile
        var profile = testDataFactory.createProfile(unitType);
        assertEquals(1, jdbi.onDemand(ProfileRepository.class)
                .updateProfile(profile.withName("New profile name")));
        // create a unit
        var insertedUnit = testDataFactory.createUnit(profile);
        assertNotNull(insertedUnit);

        // List units again
        result = jdbi.onDemand(UnitRepository.class).listUnits();
        assertNotNull(result);
        assertEquals(1, result.size());

        // delete the unit and profile and unitType
        assertEquals(1, jdbi.onDemand(UnitRepository.class).
                deleteUnit("1234"));
        assertEquals(1, jdbi.onDemand(ProfileRepository.class)
                .deleteProfile(profile.getId()));
        assertEquals(1, jdbi.onDemand(UnitTypeRepository.class)
                .deleteUnitType(unitType.getId()));

        result = jdbi.onDemand(UnitRepository.class).listUnits();
        assertNotNull(result);
        assertEquals(0, result.size());
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
        assertTrue(result.isEmpty());
    }

    @Test
    public void canListProfiles() {
        var result = jdbi.onDemand(ProfileRepository.class).listProfiles();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

}
