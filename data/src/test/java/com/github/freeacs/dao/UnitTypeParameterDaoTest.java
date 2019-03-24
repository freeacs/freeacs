package com.github.freeacs.dao;

import io.vavr.collection.List;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UnitTypeParameterDaoTest extends BaseDaoTest {

    @Test
    public void testCRUDDao() {
        List<UnitTypeParameter> unitTypeParameters = unitTypeParameterDao.getUnitTypeParameters();
        assertEquals(0, unitTypeParameters.size());

        Long unitTypeId = UnitTypeDaoTest.createUnitType(unitTypeDao, "Test 1", 1);

        Long unitTypeParameterId1 = createUnitTypeParameter(unitTypeParameterDao, unitTypeId, "Test.Param1", 1L);
        Long unitTypeParameterId2 = createUnitTypeParameter(unitTypeParameterDao, unitTypeId, "Test.Param2", 2L);

        unitTypeParameters = unitTypeParameterDao.getUnitTypeParameters();
        assertEquals(2, unitTypeParameters.size());

        int deleted = unitTypeParameterDao.deleteUnitTypeParameter(unitTypeParameterId1);
        assertEquals(1, deleted);

        unitTypeParameters = unitTypeParameterDao.getUnitTypeParameters();
        assertEquals(1, unitTypeParameters.size());
        assertEquals(unitTypeParameterId2, unitTypeParameters.get(0).getId());
        assertEquals("Test.Param2", unitTypeParameters.get(0).getName());
    }

    public static Long createUnitTypeParameter(UnitTypeParameterDao unitTypeParameterDao, Long unitTypeId, String name, long id) {
        UnitTypeParameter unitTypeParameter = UnitTypeParameter.builder()
                .name(name)
                .flags("X")
                .unitTypeId(unitTypeId)
                .build();
        Long newId = unitTypeParameterDao.createUnitTypeParameter(unitTypeParameter);
        assertEquals(id, newId.longValue());
        return newId;
    }
}
