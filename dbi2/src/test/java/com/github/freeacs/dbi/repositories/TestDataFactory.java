package com.github.freeacs.dbi.repositories;

import com.github.freeacs.dbi.domain.*;
import org.jdbi.v3.core.Jdbi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestDataFactory {

    private final Jdbi jdbi;

    public TestDataFactory(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    public UnitType createUnitType(String name) {
        var unitType = new UnitType(null, name, "vendor", "description", UnitTypeProvisioningProtocol.TR069);
        var unitTypeId = jdbi.onDemand(UnitTypeRepository.class).insertUnitType(unitType);
        assertNotNull(unitTypeId);
        return unitType.withId(unitTypeId);
    }

    public Profile createProfile(UnitType unitType) {
        var profile = new Profile(null, "test", unitType);
        var profileId = jdbi.onDemand(ProfileRepository.class).insertProfile(profile);
        assertNotNull(profileId);
        return profile.withId(profileId);
    }

    public Unit createUnit(Profile profile) {
        var unit = new Unit("1234", profile.getUnitType().getId(), profile.getId());
        var insertedUnits = jdbi.onDemand(UnitRepository.class).insertUnit(unit);
        assertEquals(1, insertedUnits);
        return unit;
    }

    public User createUser(String username) {
        var user = new User(null, username, "secretfoobar", "Test User", "", false);
        var insertedUnitId = jdbi.onDemand(UserRepository.class).insertUser(user);
        assertNotNull(insertedUnitId);
        return user.withId(insertedUnitId);
    }

    public User createUser() {
        return createUser("testuser");
    }

    public UnitTypeParameter createUnitTypeParameter(Integer unitTypeId) {
        var parameter = new UnitTypeParameter(null, "test", "RS", unitTypeId);
        var parameterId = jdbi.onDemand(UnitTypeParameterRepository.class).insertUnitTypeParameter(parameter);
        assertNotNull(parameterId);
        return parameter.withId(parameterId);
    }

    public UnitType createUnitType() {
        return createUnitType("test");
    }
}