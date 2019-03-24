package com.github.freeacs.dao;

import com.github.freeacs.shared.Protocol;
import io.vavr.collection.List;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UnitTypeDaoTest extends BaseDaoTest {

    @Test
    public void testCRUDDao() {

        List<UnitType> unitTypes = unitTypeDao.getUnitTypes();
        assertEquals(0, unitTypes.size());

        Long firstId = createUnitType(unitTypeDao, "Test 1", 1);
        Long secondId = createUnitType(unitTypeDao, "Test 2", 2);

        unitTypes = unitTypeDao.getUnitTypes();
        assertEquals(2, unitTypes.size());

        int deleted = unitTypeDao.deleteUnitType(firstId);
        assertEquals(1, deleted);

        unitTypes = unitTypeDao.getUnitTypes();
        assertEquals(1, unitTypes.size());
        assertEquals(secondId, unitTypes.get(0).getId());
        assertEquals("Test 2", unitTypes.get(0).getName());

    }

    public static Long createUnitType(UnitTypeDao unitTypeDao, String s, int i) {
        UnitType unitType1 = UnitType.builder()
                .name(s)
                .vendor("Vendor")
                .description("Desc")
                .protocol(Protocol.TR069)
                .build();
        Long firstId = unitTypeDao.createUnitType(unitType1);
        assertEquals(i, firstId.longValue());
        return firstId;
    }
}
