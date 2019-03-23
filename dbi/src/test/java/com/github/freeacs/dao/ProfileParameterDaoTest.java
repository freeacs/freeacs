package com.github.freeacs.dao;

import com.github.freeacs.dbi.BaseDBITest;
import io.vavr.collection.List;
import org.jdbi.v3.core.Jdbi;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ProfileParameterDaoTest extends BaseDBITest {

    @Test
    public void testCRUDDao() {
        Jdbi jdbi = Jdbi.create(acs.getDataSource()).installPlugins();
        UnitTypeDao unitTypeDao = jdbi.onDemand(UnitTypeDao.class);
        ProfileDao profileDao = jdbi.onDemand(ProfileDao.class);
        UnitTypeParameterDao unitTypeParameterDao = jdbi.onDemand(UnitTypeParameterDao.class);
        ProfileParameterDao profileParameterDao = jdbi.onDemand(ProfileParameterDao.class);

        Long unitTypeId = UnitTypeDaoTest.createUnitType(unitTypeDao, "Test 1", 1);
        Long profileId = ProfileDaoTest.createProfile(profileDao, unitTypeId, "Test profile 1", 1);
        Long unitTypeParameterId1 = UnitTypeParameterDaoTest.createUnitTypeParameter(unitTypeParameterDao, unitTypeId, "Test.Param1", 1L);
        Long unitTypeParameterId2 = UnitTypeParameterDaoTest.createUnitTypeParameter(unitTypeParameterDao, unitTypeId, "Test.Param2", 2L);

        List<ProfileParameter> profileParameters = profileParameterDao.getProfileParameters();
        assertEquals(0, profileParameters.size());

        createProfileParameter(profileParameterDao, profileId, unitTypeParameterId1, "test 1");
        createProfileParameter(profileParameterDao, profileId, unitTypeParameterId2, "test 2");

        profileParameters = profileParameterDao.getProfileParameters();
        assertEquals(2, profileParameters.size());

        int deleted = profileParameterDao.deleteProfileParameter(unitTypeParameterId1, profileId);
        assertEquals(1, deleted);

        profileParameters = profileParameterDao.getProfileParameters();
        assertEquals(1, profileParameters.size());
        assertEquals(unitTypeParameterId2, profileParameters.get(0).getUnitTypeParamId());
        assertEquals("test 2", profileParameters.get(0).getValue());
    }

    public static void createProfileParameter(ProfileParameterDao profileParameterDao, Long profileId, Long unitTypeParameterId, String s) {
        ProfileParameter profileParameter1 = ProfileParameter.builder()
                .profileId(profileId)
                .unitTypeParamId(unitTypeParameterId)
                .value(s)
                .build();
        profileParameterDao.createProfileParameter(profileParameter1);
    }
}
