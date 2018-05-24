package com.github.freeacs.dao;

import com.github.freeacs.dbi.Unittype;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class UnittypeDaoTest extends BaseDaoTest {

    @Test
    public void test() {
        UnittypeDao unittypeDao = jdbi.onDemand(UnittypeDao.class);

        // Given:
        UnittypeVO unittype = createUnittypeVO();

        // When:
        Long id = unittypeDao.add(unittype);
        unittype.setUnitTypeId(id);

        // Then:
        UnittypeVO unittypeFromDB = unittypeDao.get(id).get();
        checkEquals(unittype, unittypeFromDB);

        // When:
        unittype.setProtocol(Unittype.ProvisioningProtocol.HTTP);
        unittypeDao.update(unittype);

        // Then:
        unittypeFromDB = unittypeDao.get(id).get();
        checkEquals(unittype, unittypeFromDB);

        // When:
        unittypeDao.delete(id);

        // Then:
        assertFalse(unittypeDao.get(id).isPresent());
    }

    public static UnittypeVO createUnittypeVO() {
        UnittypeVO unittype = new UnittypeVO();
        unittype.setVendorName("Vendor");
        unittype.setDescription("Description");
        unittype.setUnitTypeName("Name");
        unittype.setMatcherId("MatcherId");
        unittype.setProtocol(Unittype.ProvisioningProtocol.TR069);
        return unittype;
    }

    private void checkEquals(UnittypeVO unittype, UnittypeVO unittypeFromDB) {
        assertEquals(unittype.getProtocol(), unittypeFromDB.getProtocol());
        assertEquals(unittype.getDescription(), unittypeFromDB.getDescription());
        assertEquals(unittype.getVendorName(), unittypeFromDB.getVendorName());
        assertEquals(unittype.getUnitTypeName(), unittypeFromDB.getUnitTypeName());
        assertEquals(unittype.getUnitTypeId(), unittypeFromDB.getUnitTypeId());
    }
}
