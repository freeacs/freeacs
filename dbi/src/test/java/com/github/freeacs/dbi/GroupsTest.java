package com.github.freeacs.dbi;

import static org.junit.Assert.*;

import java.sql.SQLException;
import org.junit.Test;

public class GroupsTest extends BaseDBITest {

  @Test
  public void addOrChangeGroup() throws SQLException {
    // Given:
    String unittypeName = "Test unittype";
    String groupName = "Group name";
    String profileName = "Default";
    Unittype unittype = TestUtils.createUnittype(unittypeName, acs);

    // When:
    Group group = TestUtils.createGroupAndVerify(groupName, profileName, unittype, acs);

    // Then:
    assertEquals(groupName, group.getName());
    assertEquals(profileName, group.getProfile().getName());
    assertEquals(unittypeName, group.getUnittype().getName());
  }

  @Test
  public void deleteGroup() throws SQLException {
    // Given:
    String unittypeName = "Test unittype";
    String groupName = "Group name";
    String profileName = "Default";
    Unittype unittype = TestUtils.createUnittype(unittypeName, acs);
    Group group = TestUtils.createGroupAndVerify(groupName, profileName, unittype, acs);

    // When:
    unittype.getGroups().deleteGroup(group, acs);

    // Then:
    assertEquals(0, unittype.getGroups().getGroups().length);
  }
}
