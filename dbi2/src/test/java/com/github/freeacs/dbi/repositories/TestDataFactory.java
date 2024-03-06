package com.github.freeacs.dbi.repositories;

import com.github.freeacs.dbi.domain.Profile;
import com.github.freeacs.dbi.domain.Unit;
import com.github.freeacs.dbi.domain.UnitType;
import com.github.freeacs.dbi.domain.UnitTypeProvisioningProtocol;
import org.jdbi.v3.core.Jdbi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestDataFactory {

    private final Jdbi jdbi;

    public TestDataFactory(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    public UnitType createUnitType() {
        var unitType = new UnitType(null, "test", "vendor", "description", UnitTypeProvisioningProtocol.TR069);
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
        var unit = new Unit("1234", profile);
        var insertedUnits = jdbi.onDemand(UnitRepository.class).insertUnit(unit);
        assertEquals(1, insertedUnits);
        return unit;
    }
}