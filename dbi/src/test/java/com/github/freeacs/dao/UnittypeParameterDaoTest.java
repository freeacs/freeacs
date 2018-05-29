package com.github.freeacs.dao;

import com.github.freeacs.dbi.UnittypeParameterFlag;
import com.github.freeacs.vo.UnittypeParameterVO;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class UnittypeParameterDaoTest extends BaseDaoTest {

    @Test
    public void test() {
        // Given:
        UnittypeDao unittypeDao = jdbi.onDemand(UnittypeDao.class);
        Long unitTypeId = unittypeDao.add(UnittypeDaoTest.createUnittypeVO());
        UnittypeParameterDao unittypeParameterDao = jdbi.onDemand(UnittypeParameterDao.class);
        UnittypeParameterVO unittypeParameter = new UnittypeParameterVO();
        unittypeParameter.setFlags(new UnittypeParameterFlag("X"));
        unittypeParameter.setName("System.X-FREEACS.Test");
        unittypeParameter.setUnitTypeId(unitTypeId);

        // When:
        Long paramId = unittypeParameterDao.add(unittypeParameter);
        unittypeParameter.setUnitTypeParamId(paramId);

        // Then:
        UnittypeParameterVO unittypeParameterFromDB = unittypeParameterDao.get(paramId).get();
        checkEquals(unittypeParameter, unittypeParameterFromDB);

        // When:
        unittypeParameter.setName("System.Not.Cool");
        unittypeParameterDao.update(unittypeParameter);

        // Then:
        unittypeParameterFromDB = unittypeParameterDao.get(paramId).get();
        checkEquals(unittypeParameter, unittypeParameterFromDB);

        // When:
        unittypeParameterDao.delete(paramId);

        // Then:
        assertFalse(unittypeParameterDao.get(paramId).isPresent());

    }

    private void checkEquals(UnittypeParameterVO unittypeParameter, UnittypeParameterVO unittypeParameterFromDB) {
        assertEquals(unittypeParameter.getFlags().getFlag(), unittypeParameterFromDB.getFlags().getFlag());
        assertEquals(unittypeParameter.getName(), unittypeParameterFromDB.getName());
        assertEquals(unittypeParameter.getUnitTypeId(), unittypeParameterFromDB.getUnitTypeId());
        assertEquals(unittypeParameter.getUnitTypeParamId(), unittypeParameterFromDB.getUnitTypeParamId());
    }
}
