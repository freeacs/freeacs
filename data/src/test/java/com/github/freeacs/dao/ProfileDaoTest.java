package com.github.freeacs.dao;

import io.vavr.collection.List;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ProfileDaoTest extends BaseDaoTest {

    @Test
    public void testCRUDDao() {
        List<Profile> profiles = profileDao.getProfiles(1L);
        assertEquals(0, profiles.size());

        Long unitTypeId = UnitTypeDaoTest.createUnitType(unitTypeDao, "Test 1", 1);
        Long firstId = createProfile(profileDao, unitTypeId, "Test profile 1", 1);
        Long secondId = createProfile(profileDao, unitTypeId, "Test profile 2", 2);

        profiles = profileDao.getProfiles(unitTypeId);
        assertEquals(2, profiles.size());

        int deleted = profileDao.deleteProfile(firstId);
        assertEquals(1, deleted);

        profiles = profileDao.getProfiles(unitTypeId);
        assertEquals(1, profiles.size());
        assertEquals(secondId, profiles.get(0).getId());
        assertEquals("Test profile 2", profiles.get(0).getName());

    }

    public static Long createProfile(ProfileDao profileDao, Long unitTypeId, String s, int i) {
        Profile profile1 = Profile.builder()
                .name(s)
                .unitTypeId(unitTypeId)
                .build();
        Long firstId = profileDao.createProfile(profile1);
        assertEquals(i, firstId.longValue());
        return firstId;
    }
}
