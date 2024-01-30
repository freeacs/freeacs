package com.github.freeacs.dbi;

import java.sql.SQLException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UnittypesTest extends BaseDBITest {

  @Test
  public void createUnittype() throws SQLException {
    // When:
    Unittype unittype = TestUtils.createUnittype("Name 1", acs);

    // Then:
    assertNotNull(unittype.getId());
    assertEquals("Name 1", unittype.getName());
    assertEquals(1, unittype.getProfiles().getProfiles().length);
    assertEquals("Default", unittype.getProfiles().getProfiles()[0].getName());
  }

  @Test
  public void deleteUnittype() throws SQLException {
    // Given:
    String unittypeName = "Name 2";
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
    String unittypeName = "Name 3";
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

  @Test
  public void invalidProtocolFails() {
    // When:
    assertThrows(IllegalArgumentException.class, () -> new Unittype("Name 4", "Name", "Name",  null));
  }
}
