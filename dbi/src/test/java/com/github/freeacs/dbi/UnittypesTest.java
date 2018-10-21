package com.github.freeacs.dbi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.sql.SQLException;
import org.junit.Test;

public class UnittypesTest extends BaseDBITest {

  @Test
  public void createUnittype() throws SQLException {
    // When:
    Unittype unittype = TestUtils.createUnittype("Name", acs);

    // Then:
    assertEquals(1, unittype.getId().intValue());
    assertEquals("Name", unittype.getName());
    assertEquals(1, unittype.getProfiles().getProfiles().length);
    assertEquals("Default", unittype.getProfiles().getProfiles()[0].getName());
  }

  @Test
  public void deleteUnittype() throws SQLException {
    // Given:
    String unittypeName = "Name";
    Unittype unittype = TestUtils.createUnittype(unittypeName, acs);

    // When:
    acs.getUnittypes().deleteUnittype(unittype, acs, true);

    // Then:
    Unittype byName = acs.getUnittypes().getByName(unittypeName);
    assertNull(byName);
  }

  @Test
  public void updateUnittype() throws SQLException {
    // Given:
    String unittypeName = "Name";
    Unittype unittype = TestUtils.createUnittype(unittypeName, acs);
    String newUnittypeName = "New name";
    unittype.setName(newUnittypeName);

    // When:
    acs.getUnittypes().addOrChangeUnittype(unittype, acs);

    // Then:
    Unittype byName = acs.getUnittypes().getByName(newUnittypeName);
    assertNotNull(byName);
    assertEquals(newUnittypeName, byName.getName());
    assertNull(acs.getUnittypes().getByName(unittypeName));
  }

  @Test(expected = IllegalArgumentException.class)
  public void invalidProtocolFails() {
    // When:
    new Unittype("Name", "Name", "Name", null);
  }
}
