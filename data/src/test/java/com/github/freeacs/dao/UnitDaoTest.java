package com.github.freeacs.dao;

import io.vavr.collection.List;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UnitDaoTest extends BaseDaoTest {

    @Test
    public void testCRUDDao() {
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
