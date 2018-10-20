package com.github.freeacs.dbi;

import static org.junit.Assert.assertEquals;

import java.sql.SQLException;
import org.junit.Test;

public class UnittypesTest extends BaseDBITest {

  @Test
  public void addOrChangeUnittype() throws SQLException {
    // When:
    Unittype unittype = TestUtils.createUnittype("Name", acs);

    // Then:
    assertEquals(1, unittype.getId().intValue());
    assertEquals("Name", unittype.getName());
    assertEquals(1, unittype.getProfiles().getProfiles().length);
    assertEquals("Default", unittype.getProfiles().getProfiles()[0].getName());
  }

  @Test(expected = IllegalArgumentException.class)
  public void invalidProtocolFails() {
    // When:
    new Unittype("Name", "Name", "Name", null);
  }
}
