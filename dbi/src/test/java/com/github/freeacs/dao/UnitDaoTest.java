package com.github.freeacs.dao;

import com.github.freeacs.dbi.BaseDBITest;
import io.vavr.collection.List;
import org.jdbi.v3.core.Jdbi;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UnitDaoTest extends BaseDBITest {

    @Test
    public void testCRUDDao() {
        Jdbi jdbi = Jdbi.create(acs.getDataSource()).installPlugins();
        UnitTypeDao unitTypeDao = jdbi.onDemand(UnitTypeDao.class);
        ProfileDao profileDao = jdbi.onDemand(ProfileDao.class);
        UnitDao unitDao = jdbi.onDemand(UnitDao.class);

        Long unitTypeId = UnitTypeDaoTest.createUnitType(unitTypeDao, "Test 1", 1);
        Long profileId = ProfileDaoTest.createProfile(profileDao, unitTypeId, "Test profile 1", 1);

        createUnit(unitDao, unitTypeId, profileId, "123");
        createUnit(unitDao, unitTypeId, profileId, "321");

        List<Unit> units = unitDao.getUnits();
        assertEquals(2, units.size());

        Integer deleted = unitDao.deleteUnit("123");
        assertEquals(1, deleted.intValue());

        units = unitDao.getUnits();
        assertEquals(1, units.size());
        assertEquals("321", units.get(0).getUnitId());
    }

    public static void createUnit(UnitDao unitDao, Long unitTypeId, Long profileId, String s) {
        Unit unit1 = Unit.builder()
                .unitId(s)
                .unitTypeId(unitTypeId)
                .profileId(profileId)
                .build();
        unitDao.createUnit(unit1);
    }
}
