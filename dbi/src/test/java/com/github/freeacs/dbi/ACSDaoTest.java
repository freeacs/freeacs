package com.github.freeacs.dbi;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ACSDaoTest extends BaseDBITest {

    @Test
    public void testGetUnittypeById() {
        // Given:
        ACSDao acs = new ACSDao(dataSource);

        // When:
        Unittype result1 = acs.getUnitTypeById(1);
        Unittype result2 = acs.getUnitTypeById(1);

        // Then:
        assertEquals(result1.getId(), 1);
        assertEquals(result1.getName(), "Test unit type");
        assertEquals(result1.getVendor(), "Test vendor name");
        assertEquals(result1.getDescription(), "Test description");
        assertEquals(result1.getProtocol(), Unittype.ProvisioningProtocol.TR069);
        assertSame(result1, result2);
    }

    @Test
    public void testGetUnittypeByName() {
        // Given:
        ACSDao acs = new ACSDao(dataSource);

        // When:
        Unittype result1 = acs.getUnitTypeByName("Test unit type");
        Unittype result2 = acs.getUnitTypeByName("Test unit type");

        // Then:
        assertEquals(result1.getId(), 1);
        assertEquals(result1.getName(), "Test unit type");
        assertEquals(result1.getVendor(), "Test vendor name");
        assertEquals(result1.getDescription(), "Test description");
        assertEquals(result1.getProtocol(), Unittype.ProvisioningProtocol.TR069);
        assertSame(result1, result2);
    }

    @Test
    public void testGetUnitTypeParameters() {
        // Given:
        ACSDao acs = new ACSDao(dataSource);

        // When:
        List<UnittypeParameter> result1 = acs.getUnittypeParametersByUnitTypeId(1);
        List<UnittypeParameter> result2 = acs.getUnittypeParametersByUnitTypeId(1);

        // Then:
        assertEquals(result1.size(), 1);
        assertEquals(result1.get(0).getName(), "Test param name");
        assertEquals(result1.get(0).getFlag().getFlag(), "RW");
        assertSame(result1, result2);
    }

    @Test
    public void testGetProfile() {
        // Given:
        ACSDao acs = new ACSDao(dataSource);

        // When:
        Profile result1 = acs.getProfileById(1);
        Profile result2 = acs.getProfileById(1);

        // Then:
        assertEquals(result1.getId(), 1);
        assertEquals(result1.getName(), "Test profile name");
        assertEquals(result1.getUnittype().getId(), 1);
        assertSame(result1, result2);
    }

    @Test
    public void testGetProfileParameters() {
        // Given:
        ACSDao acs = new ACSDao(dataSource);

        // When:
        List<ProfileParameter> result1 = acs.getProfileParametersByProfileId(1);
        List<ProfileParameter> result2 = acs.getProfileParametersByProfileId(1);

        // Then:
        assertEquals(result1.size(), 1);
        assertEquals(result1.get(0).getUnittypeParameter().getId(), 1);
        assertEquals(result1.get(0).getValue(), "Test value");
        assertSame(result1, result2);
    }

    @Test
    public void testGetGroup() {
        // Given:
        ACSDao acs = new ACSDao(dataSource);

        // When:
        Group result1 = acs.getGroupById(1);
        Group result2 = acs.getGroupById(1);
        Group result3 = acs.getGroupById(2);
        Group result4 = acs.getGroupById(2);

        // Then:
        assertEquals(result1.getId(), 1);
        assertEquals(result1.getName(), "Test group name 1");
        assertEquals(result1.getDescription(), "Test description 1");
        assertEquals(result1.getUnittype().getId(), 1);
        assertEquals(result1.getProfile().getId(), 1);
        assertNull(result1.getParent());
        assertSame(result1, result2);

        assertEquals(result3.getId(), 2);
        assertEquals(result3.getName(), "Test group name 2");
        assertEquals(result3.getDescription(), "Test description 2");
        assertEquals(result3.getUnittype().getId(), 1);
        assertEquals(result3.getProfile().getId(), 1);
        assertEquals(result3.getParent().getId(), result1.getId());
        assertSame(result3, result4);
    }

    @Test
    public void testGetGroupParameters() {
        // Given:
        ACSDao acs = new ACSDao(dataSource);

        // When:
        List<GroupParameter> result1 = acs.getGroupParametersByGroupId(1);
        List<GroupParameter> result2 = acs.getGroupParametersByGroupId(1);

        // Then:
        assertNotNull(result1);
        assertNotNull(result2);
        assertSame(result1, result2);
    }

    @Test
    public void testGetJob() {
        // Given:
        ACSDao acs = new ACSDao(dataSource);

        // When:
        Job result1 = acs.getJobById(1);
        Job result2 = acs.getJobById(1);

        // Then:
        assertNotNull(result1);
        assertNotNull(result2);
        assertSame(result1, result2);
    }

    @Test
    public void testGetJobParameters() {
        // Given:
        ACSDao acs = new ACSDao(dataSource);

        // When:
        List<JobParameter> result1 = acs.getJobParametersByJobId(1);
        List<JobParameter> result2 = acs.getJobParametersByJobId(1);

        // Then:
        assertNotNull(result1);
        assertNotNull(result2);
        assertSame(result1, result2);
    }
}
