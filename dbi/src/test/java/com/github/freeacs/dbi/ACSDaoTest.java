package com.github.freeacs.dbi;

import com.github.freeacs.common.cache.InMemoryACSCacheManager;
import com.hazelcast.core.HazelcastInstance;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ACSDaoTest extends BaseDBITest {

    @Test
    public void canUseHazelcast() {
        HazelcastInstance hazelcastInstance = createHazelcastInstance();
        hazelcastInstance.getList("test").add("test");
    }

    @Test
    public void testGetUnitById() {
        // Given:
        ACSDao acs = new ACSDao(dataSource, new InMemoryACSCacheManager());

        // When:
        Unittype result1 = acs.getCachedUnittypeByUnitTypeId(1);
        Unittype result2 = acs.getCachedUnittypeByUnitTypeId(1);

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
        ACSDao acs = new ACSDao(dataSource, new InMemoryACSCacheManager());

        // When:
        List<UnittypeParameter> result1 = acs.getCachedUnittypeParameters(1);
        List<UnittypeParameter> result2 = acs.getCachedUnittypeParameters(1);

        // Then:
        assertEquals(result1.size(), 1);
        assertEquals(result1.get(0).getName(), "Test param name");
        assertEquals(result1.get(0).getFlag().getFlag(), "RW");
        assertSame(result1, result2);
    }

    @Test
    public void testGetProfile() {
        // Given:
        ACSDao acs = new ACSDao(dataSource, new InMemoryACSCacheManager());

        // When:
        Profile result1 = acs.getCachedProfile(1);
        Profile result2 = acs.getCachedProfile(1);

        // Then:
        assertEquals(result1.getId(), 1);
        assertEquals(result1.getName(), "Test profile name");
        assertEquals(result1.getUnittype().getId(), 1);
        assertSame(result1, result2);
    }

    @Test
    public void testGetProfileParameters() {
        // Given:
        ACSDao acs = new ACSDao(dataSource, new InMemoryACSCacheManager());

        // When:
        List<ProfileParameter> result1 = acs.getCachedProfileParameters(1);
        List<ProfileParameter> result2 = acs.getCachedProfileParameters(1);

        // Then:
        assertEquals(result1.size(), 1);
        assertEquals(result1.get(0).getUnittypeParameter().getId(), 1);
        assertEquals(result1.get(0).getValue(), "Test value");
        assertSame(result1, result2);
    }

    @Test
    public void testGetGroup() {
        // Given:
        ACSDao acs = new ACSDao(dataSource, new InMemoryACSCacheManager());

        // When:
        Group result1 = acs.getCachedGroup(1);
        Group result2 = acs.getCachedGroup(1);
        Group result3 = acs.getCachedGroup(2);
        Group result4 = acs.getCachedGroup(2);

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
        // for now, we don't have a profile for this group,
        // because the profile must be one of the parent group's profile
        // and the seed.sql file doesn't have a proper setup
        assertNull(result3.getProfile());
        assertEquals(result3.getParent().getId(), result1.getId());
        assertSame(result3, result4);
    }

    @Test
    public void testGetGroupParameters() {
        // Given:
        ACSDao acs = new ACSDao(dataSource, new InMemoryACSCacheManager());

        // When:
        List<GroupParameter> result1 = acs.getCachedGroupParameters(1);
        List<GroupParameter> result2 = acs.getCachedGroupParameters(1);

        // Then:
        assertNotNull(result1);
        assertNotNull(result2);
        assertSame(result1, result2);
    }

    @Test
    public void testGetJob() {
        // Given:
        ACSDao acs = new ACSDao(dataSource, new InMemoryACSCacheManager());

        // When:
        Job result1 = acs.getCachedJob(1);
        Job result2 = acs.getCachedJob(1);

        // Then:
        assertNotNull(result1);
        assertNotNull(result2);
        assertSame(result1, result2);
    }

    @Test
    public void testGetJobParameters() {
        // Given:
        ACSDao acs = new ACSDao(dataSource, new InMemoryACSCacheManager());

        // When:
        List<JobParameter> result1 = acs.getCachedJobParameters(1);
        List<JobParameter> result2 = acs.getCachedJobParameters(1);

        // Then:
        assertNotNull(result1);
        assertNotNull(result2);
        assertSame(result1, result2);
    }
}
