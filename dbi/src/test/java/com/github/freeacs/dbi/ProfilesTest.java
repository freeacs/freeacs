package com.github.freeacs.dbi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.sql.SQLException;
import org.junit.Test;

public class ProfilesTest extends BaseDBITest {

  @Test
  public void createProfile() throws SQLException {
    // When:
    Unittype unittype = TestUtils.createUnittype("Test unittype", acs);
    Profile profile = TestUtils.createProfile("Test profile", unittype, acs);

    // Then:
    assertEquals(2, profile.getId().intValue());
    assertEquals("Test profile", profile.getName());
    assertEquals(2, profile.getUnittype().getProfiles().getProfiles().length);
    assertEquals("Default", profile.getUnittype().getProfiles().getProfiles()[0].getName());
    assertEquals("Test profile", profile.getUnittype().getProfiles().getProfiles()[1].getName());
  }

  @Test
  public void deleteProfile() throws SQLException {
    // Given:
    Unittype unittype = TestUtils.createUnittype("Test unittype", acs);
    Profile profile = TestUtils.createProfile("Test profile", unittype, acs);

    // When:
    unittype.getProfiles().deleteProfile(profile, acs, true);

    // Then:
    Profile byName = unittype.getProfiles().getByName("Test profile");
    assertNull(byName);
  }

  @Test
  public void updateProfile() throws SQLException {
    // Given:
    String profileName = "Test profile";
    Unittype unittype = TestUtils.createUnittype("Test unittype", acs);
    Profile profile = TestUtils.createProfile(profileName, unittype, acs);
    String newProfileName = "New profile name";
    profile.setName(newProfileName);

    // When:
    unittype.getProfiles().addOrChangeProfile(profile, acs);

    // Then:
    Profile byName = unittype.getProfiles().getByName(newProfileName);
    assertNotNull(byName);
    assertEquals(newProfileName, byName.getName());
    assertNull(unittype.getProfiles().getByName(profileName));
  }
}
