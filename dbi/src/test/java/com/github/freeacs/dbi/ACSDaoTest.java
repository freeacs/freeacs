package com.github.freeacs.dbi;

import com.github.freeacs.common.cache.InMemoryACSCacheManager;
import com.hazelcast.core.HazelcastInstance;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

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
        Unittype result1 = acs.getCachedUnittype(1);
        Unittype result2 = acs.getCachedUnittype(1);

        // Then:
        assertNotNull(result1);
        assertNotNull(result2);
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
        assertNotNull(result1);
        assertNotNull(result2);
        assertSame(result1, result2);
    }

    @Test
    @Disabled("Work in progress")
    public void testGetprofile() {
        // Given:
        ACSDao acs = new ACSDao(dataSource, new InMemoryACSCacheManager());

        // When:
        Profile result1 = acs.getCachedProfile(1);
        Profile result2 = acs.getCachedProfile(1);

        // Then:
        assertNotNull(result1);
        assertNotNull(result2);
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
        assertNotNull(result1);
        assertNotNull(result2);
        assertSame(result1, result2);
    }

    @Test
    @Disabled("Work in progress")
    public void testGetGroup() {
        // Given:
        ACSDao acs = new ACSDao(dataSource, new InMemoryACSCacheManager());

        // When:
        Group result1 = acs.getCachedGroup(1);
        Group result2 = acs.getCachedGroup(1);

        // Then:
        assertNotNull(result1);
        assertNotNull(result2);
        assertSame(result1, result2);
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
    @Disabled("Work in progress")
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
