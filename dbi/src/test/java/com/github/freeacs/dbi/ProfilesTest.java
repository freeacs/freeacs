package com.github.freeacs.dbi;

import java.sql.SQLException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ProfilesTest extends BaseDBITest {

  @Test
  public void createProfile() throws SQLException {
    // When:
    Unittype unittype = TestUtils.createUnittype("Test unittype 1", acs);
    Profile profile = TestUtils.createProfile("Test profile", unittype, acs);

    // Then:
    Assertions.assertNotNull(profile.getId());
    Assertions.assertEquals("Test profile", profile.getName());
    Assertions.assertEquals(2, profile.getUnittype().getProfiles().getProfiles().length);
    Assertions.assertEquals("Default", profile.getUnittype().getProfiles().getProfiles()[0].getName());
    Assertions.assertEquals("Test profile", profile.getUnittype().getProfiles().getProfiles()[1].getName());
  }

  @Test
  public void deleteProfile() throws SQLException {
    // Given:
    Unittype unittype = TestUtils.createUnittype("Test unittype 2", acs);
    Profile profile = TestUtils.createProfile("Test profile", unittype, acs);

    // When:
    unittype.getProfiles().deleteProfile(profile, acs, true);

    // Then:
    Profile byName = unittype.getProfiles().getByName("Test profile");
    Assertions.assertNull(byName);
  }

  @Test
  public void updateProfile() throws SQLException {
    // Given:
    String profileName = "Test profile";
    Unittype unittype = TestUtils.createUnittype("Test unittype 3", acs);
    Profile profile = TestUtils.createProfile(profileName, unittype, acs);
    String newProfileName = "New profile name";
    profile.setName(newProfileName);

    // When:
    unittype.getProfiles().addOrChangeProfile(profile, acs);

    // Then:
    Profile byName = unittype.getProfiles().getByName(newProfileName);
    Assertions.assertNotNull(byName);
    Assertions.assertEquals(newProfileName, byName.getName());
    Assertions.assertNull(unittype.getProfiles().getByName(profileName));
  }
}
