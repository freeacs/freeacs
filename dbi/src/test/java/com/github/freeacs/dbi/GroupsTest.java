package com.github.freeacs.dbi;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

public class GroupsTest extends BaseDBITest {

  @Test
  public void addOrChangeGroup() throws SQLException {
    // Given:
    String unittypeName = "Test unittype 1";
    String groupName = "Group name";
    String profileName = "Default";
    Unittype unittype = TestUtils.createUnittype(unittypeName, acs);

    // When:
    Group group = TestUtils.createGroupAndVerify(groupName, profileName, unittype, acs);

    // Then:
    Assertions.assertEquals(groupName, group.getName());
    Assertions.assertEquals(profileName, group.getProfile().getName());
    Assertions.assertEquals(unittypeName, group.getUnittype().getName());
  }

  @Test
  public void deleteGroup() throws SQLException {
    // Given:
    String unittypeName = "Test unittype 2";
    String groupName = "Group name";
    String profileName = "Default";
    Unittype unittype = TestUtils.createUnittype(unittypeName, acs);
    Group group = TestUtils.createGroupAndVerify(groupName, profileName, unittype, acs);

    // When:
    unittype.getGroups().deleteGroup(group, acs);

    // Then:
    Assertions.assertEquals(0, unittype.getGroups().getGroups().length);
  }
}
