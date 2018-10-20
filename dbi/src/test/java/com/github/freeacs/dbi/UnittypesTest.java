package com.github.freeacs.dbi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.sql.SQLException;
import java.util.HashMap;
import org.junit.Test;

public class UnittypesTest extends BaseDBITest {

  @Test
  public void createUnittype() throws SQLException {
    // When:
    Unittypes unittypes = new Unittypes(new HashMap<>(), new HashMap<>());
    Unittype unittype = TestUtils.createUnittype("Name", unittypes, acs);

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
    Unittypes unittypes = new Unittypes(new HashMap<>(), new HashMap<>());
    Unittype unittype = TestUtils.createUnittype(unittypeName, unittypes, acs);

    // When:
    unittypes.deleteUnittype(unittype, acs, true);

    // Then:
    Unittype byName = unittypes.getByName(unittypeName);
    assertNull(byName);
  }

  @Test
  public void updateUnittype() throws SQLException {
    // Given:
    String unittypeName = "Name";
    Unittypes unittypes = new Unittypes(new HashMap<>(), new HashMap<>());
    Unittype unittype = TestUtils.createUnittype(unittypeName, unittypes, acs);
    String newUnittypeName = "New name";
    unittype.setName(newUnittypeName);

    // When:
    unittypes.addOrChangeUnittype(unittype, acs);

    // Then:
    Unittype byName = unittypes.getByName(newUnittypeName);
    assertNotNull(byName);
    assertEquals(newUnittypeName, byName.getName());
    assertNull(unittypes.getByName(unittypeName));
  }

  @Test(expected = IllegalArgumentException.class)
  public void invalidProtocolFails() {
    // When:
    new Unittype("Name", "Name", "Name", null);
  }
}
