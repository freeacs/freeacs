package com.github.freeacs.dbi;

import static org.junit.Assert.assertEquals;

import java.sql.SQLException;
import org.junit.Test;

public class ProfilesTest extends BaseDBITest {

  @Test
  public void addOrChangeProfile() throws SQLException {
    // When:
    Profile profile = TestUtils.createProfile("Test profile", "Unittype", acs);

    // Then:
    assertEquals(2, profile.getId().intValue());
    assertEquals("Test profile", profile.getName());
    assertEquals(2, profile.getUnittype().getProfiles().getProfiles().length);
    assertEquals("Default", profile.getUnittype().getProfiles().getProfiles()[0].getName());
    assertEquals("Test profile", profile.getUnittype().getProfiles().getProfiles()[1].getName());
  }
}
