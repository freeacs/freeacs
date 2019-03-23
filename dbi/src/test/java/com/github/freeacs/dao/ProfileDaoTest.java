package com.github.freeacs.dao;

import com.github.freeacs.dbi.BaseDBITest;
import io.vavr.collection.List;
import org.jdbi.v3.core.Jdbi;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ProfileDaoTest extends BaseDBITest {

    @Test
    public void testCRUDDao() {
        Jdbi jdbi = Jdbi.create(acs.getDataSource()).installPlugins();
        UnitTypeDao unitTypeDao = jdbi.onDemand(UnitTypeDao.class);
        ProfileDao profileDao = jdbi.onDemand(ProfileDao.class);

        List<Profile> profiles = profileDao.getProfiles();
        assertEquals(0, profiles.size());

        Long unitTypeId = UnitTypeDaoTest.createUnitType(unitTypeDao, "Test 1", 1);
        Long firstId = createProfile(profileDao, unitTypeId, "Test profile 1", 1);
        Long secondId = createProfile(profileDao, unitTypeId, "Test profile 2", 2);

        profiles = profileDao.getProfiles();
        assertEquals(2, profiles.size());

        int deleted = profileDao.deleteProfile(firstId);
        assertEquals(1, deleted);

        profiles = profileDao.getProfiles();
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
